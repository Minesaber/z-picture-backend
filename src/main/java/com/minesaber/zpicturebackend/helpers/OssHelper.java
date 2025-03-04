package com.minesaber.zpicturebackend.helpers;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.*;
import com.minesaber.zpicturebackend.config.OssClientConfig;
import com.minesaber.zpicturebackend.constants.FileConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.exception.BusinessException;
import com.minesaber.zpicturebackend.utils.SystemStatusUtil;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Formatter;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/** OSS 工具 */
@Component
@Slf4j
public class OssHelper {
  // todo 可能可以考虑使用其他初始化字段的方法
  /** OSS 配置 */
  @Resource private OssClientConfig ossClientConfig;

  /** OSS 客户端 */
  @Resource private OSS ossClient;

  /** Redis 缓存 */
  @Resource private RedisTemplate<String, String> redisTemplate;

  /**
   * 获取基础 URL
   *
   * @return 外网访问基础 URL
   */
  public String getBaseURL() {
    return "https://" + ossClientConfig.getBucket() + "." + ossClientConfig.getEndpoint();
  }

  /**
   * 获取临时存储桶 URL
   *
   * @return 外网访问基础 URL
   */
  public String getTempURL() {
    return "https://" + ossClientConfig.getBucketTemp() + "." + ossClientConfig.getEndpoint();
  }

  /**
   * 上传文件
   *
   * @param key 唯一键
   * @param inputStream 输入流
   * @param process 处理
   * @return putObjectResult
   */
  public PutObjectResult putObject(
      String key, InputStream inputStream, Consumer<PutObjectRequest> process) {
    PutObjectRequest putObjectRequest =
        new PutObjectRequest(ossClientConfig.getBucket(), key, inputStream);
    if (process != null) process.accept(putObjectRequest);
    return executeWithExceptionLogging(() -> ossClient.putObject(putObjectRequest));
  }

  /**
   * 上传文件
   *
   * @param key 唯一键
   * @param file 文件
   * @return putObjectResult
   */
  public PutObjectResult putObject(String key, File file) {
    PutObjectRequest putObjectRequest =
        new PutObjectRequest(ossClientConfig.getBucket(), key, file);
    return executeWithExceptionLogging(() -> ossClient.putObject(putObjectRequest));
  }

  /**
   * 上传到临时存储桶
   *
   * @param key 唯一键
   * @param inputStream 文件流
   * @return 上传结果
   */
  public PutObjectResult putObjectToTempBucket(String key, InputStream inputStream) {
    PutObjectRequest putObjectRequest =
        new PutObjectRequest(ossClientConfig.getBucketTemp(), key, inputStream);
    return executeWithExceptionLogging(() -> ossClient.putObject(putObjectRequest));
  }

  /**
   * 下载文件
   *
   * @param key 唯一键
   * @param process 处理
   * @return OSSObject
   */
  public OSSObject getObject(String key, Consumer<GetObjectRequest> process) {
    GetObjectRequest getObjectRequest = new GetObjectRequest(ossClientConfig.getBucket(), key);
    if (process != null) process.accept(getObjectRequest);
    return executeWithExceptionLogging(() -> ossClient.getObject(getObjectRequest));
  }

  /**
   * 获取图片信息
   *
   * @param key 唯一键
   * @return 图片信息
   */
  public OSSObject getPictureInfo(String key) {
    try {
      return getObject(key, request -> request.setProcess("image/info"));
    } catch (Exception e) {
      deleteObject(key);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片解析错误");
    }
  }

  /**
   * 生成转换后的图片
   *
   * @param key 待处理图片
   * @return 处理结果
   */
  public GenericResult getConvertedImg(String key) {
    String targetImage = StrUtil.subBefore(key, '.', true) + "." + FileConstant.IMG_END_TYPE;
    String convertRule = getProcessRule(FileConstant.STYLE_TYPE, targetImage);
    // todo 配置复用：bucket等
    ProcessObjectRequest request =
        new ProcessObjectRequest(ossClientConfig.getBucket(), key, convertRule);
    return executeWithExceptionLogging(() -> ossClient.processObject(request));
  }

  /**
   * 压缩生成缩略图
   *
   * @param key 待处理图片唯一键
   * @return 处理结果
   */
  public GenericResult getCompressedImg(String key) {
    // 缩略图添加
    String keyPrefix = StrUtil.subBefore(key, '.', true);
    String keySuffix = StrUtil.subAfter(key, '.', true);
    String compressRule =
        getProcessRule(
            FileConstant.COMPRESS_RATIO, keyPrefix + FileConstant.COMPRESS_TAIL + "." + keySuffix);
    ProcessObjectRequest request =
        new ProcessObjectRequest(ossClientConfig.getBucket(), key, compressRule);
    return executeWithExceptionLogging(() -> ossClient.processObject(request));
  }

  /**
   * 获取完整处理规则
   *
   * @param rule 规则
   * @param targetImage 将要生成的对象
   * @return 完整处理规则
   */
  private String getProcessRule(String rule, String targetImage) {
    String bucket = ossClientConfig.getBucket();
    StringBuilder sbStyle = new StringBuilder();
    Formatter styleFormatter = new Formatter(sbStyle);
    styleFormatter.format(
        "%s|sys/saveas,o_%s,b_%s",
        rule,
        BinaryUtil.toBase64String(targetImage.getBytes()),
        BinaryUtil.toBase64String(bucket.getBytes()));
    return sbStyle.toString();
  }

  /**
   * 获取图片的主色调
   *
   * @param key 唯一键
   * @return 查询结果
   */
  public String getPicColor(String key) {
    GetObjectRequest getObjectRequest = new GetObjectRequest(ossClientConfig.getBucket(), key);
    getObjectRequest.setProcess("image/average-hue");
    OSSObject result = ossClient.getObject(getObjectRequest);
    try (InputStream content = result.getResponse().getContent()) {
      String json = IOUtils.readStreamAsString(content, "UTF-8");
      return JSONUtil.parseObj(json).get("RGB", String.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 删除对象
   *
   * @param key 待删除图片唯一键
   */
  public void deleteObject(String key) {
    executeWithExceptionLogging(() -> ossClient.deleteObject(ossClientConfig.getBucket(), key));
  }

  /**
   * 删除临时存储桶中指定文件
   *
   * @param key 待删除文件唯一键
   */
  public void deleteObjectFromTempBucket(String key) {
    executeWithExceptionLogging(() -> ossClient.deleteObject(ossClientConfig.getBucketTemp(), key));
  }

  /**
   * 从URL中获取唯一键
   *
   * @param url URL地址
   * @return 唯一键
   */
  public String getKeyFromUrl(String url) {
    return StrUtil.subAfter(url, "com/", false);
  }

  /**
   * 生成预签名URL
   *
   * @param key 需要临时访问文件的唯一键
   * @param expiration 过期时间（毫秒值），不能超过一小时
   * @return 预签名URL
   */
  public String genPresignedUrl(String key, Long expiration) {
    ThrowUtils.throwIf(SystemStatusUtil.isClosed(), ErrorCode.MAINTENANCE_ERROR);
    expiration = Math.min(expiration, 3600 * 1000L);
    // 首先读缓存
    String cachedPreSignedUrl = redisTemplate.opsForValue().get(key);
    if (cachedPreSignedUrl != null) {
      return cachedPreSignedUrl;
    }
    // 找不到，则生成预签名URL并缓存
    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest(ossClientConfig.getBucket(), key, HttpMethod.GET);
    Date date = new Date(new Date().getTime() + expiration);
    generatePresignedUrlRequest.setExpiration(date);
    String presignedUrl =
        String.valueOf(ossClient.generatePresignedUrl(generatePresignedUrlRequest));
    redisTemplate
        .opsForValue()
        .set(
            key,
            presignedUrl,
            expiration - RandomUtil.randomLong(100, 1000),
            TimeUnit.MILLISECONDS);
    return presignedUrl;
  }

  /**
   * 捕获OSS调用可能发生的异常
   *
   * @param supplier OSS调用
   * @param <T> 结果类型
   * @return 执行结果
   */
  private <T> T executeWithExceptionLogging(Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (OSSException | ClientException e) {
      // OSSException 和 com.aliyun.oss.ClientException 为运行时异常
      log.error("Error Message: {}", e.getMessage());
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "OSS 操作失败");
    }
  }
}
