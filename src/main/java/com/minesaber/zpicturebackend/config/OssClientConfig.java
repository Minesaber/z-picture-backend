package com.minesaber.zpicturebackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyuncs.exceptions.ClientException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OSS 配置 */
@Configuration
@ConfigurationProperties(prefix = "oss.client")
@Data
public class OssClientConfig {
  /** 区域 */
  private String region;

  /** 端点 */
  private String endpoint;

  /** 桶 */
  private String bucket;

  /** 临时存储桶 */
  private String bucketTemp;

  // todo 当前ossClient为单例，可以考虑池化
  @Bean
  public OSS ossClient() {
    EnvironmentVariableCredentialsProvider credentialsProvider;
    try {
      credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
    } catch (ClientException ce) {
      // todo 系统日志使用待规范
      // com.aliyuncs.exceptions.ClientException 为检查时异常
      throw new RuntimeException(ce);
    }
    return OSSClientBuilder.create()
        .credentialsProvider(credentialsProvider)
        .region(region)
        .endpoint(endpoint)
        .build();
  }
}
