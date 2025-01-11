package com.minesaber.zpicturebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "system")
@Data
public class SystemConfig {
  /** 盐 */
  private String salt;

  /** 新账号初始密码 */
  private String defaultUserPassword;

  /** 新账号初始昵称 */
  private String defaultUserName;
}
