package com.minesaber.zpicturebackend.helpers;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.*;
import com.minesaber.zpicturebackend.config.OssClientConfig;
import com.minesaber.zpicturebackend.constants.FileConstant;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** OSS 工具 */
@Component
@Slf4j
public class OssHelper {
  // todo 可能可以考虑使用其他初始化字段的方法
  /** OSS 配置 */
  @Resource private OssClientConfig ossClientConfig;

  /** OSS 客户端 */
  @Resource private OSS ossClient;

  /**
   * 获取基础 URL
   *
   * @return 外网访问基础 URL
   */
  public String getBaseURL() {
    return "https://" + ossClientConfig.getBucket() + "." + ossClientConfig.getEndpoint();
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

  // tips：已使用本地库获取图片信息
  //  /**
  //   * 获取图片信息
  //   *
  //   * @param key 唯一键
  //   * @return 图片信息
  //   */
  //  public OSSObject getPictureInfo(String key) {
  //    return getObject(key, request -> request.setProcess("image/info"));
  //  }

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
   * 删除对象
   *
   * @param key 待删除图片唯一键
   */
  public void deleteObject(String key) {
    ossClient.deleteObject(ossClientConfig.getBucket(), key);
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
      throw new RuntimeException(e);
    }
  }
}
