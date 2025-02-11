package com.minesaber.zpicturebackend.helpers;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.minesaber.zpicturebackend.config.OssClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** OSS 工具 */
@Component
@Slf4j
public class OssHelper {
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
   * @return putObjectResult
   */
  public PutObjectResult putObject(String key, InputStream inputStream) {
    PutObjectRequest putObjectRequest =
        new PutObjectRequest(ossClientConfig.getBucket(), key, inputStream);

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
