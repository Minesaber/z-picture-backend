package com.minesaber.zpicturebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 系统配置 */
@Configuration
@ConfigurationProperties(prefix = "system")
@Data
public class SystemConfig {
  // region 用户模块
  /** 盐 */
  private String salt;

  /** 新账号初始密码 */
  private String defaultUserPassword;

  /** 新账号初始昵称 */
  private String defaultUserName;

  // endregion

  // region 图片模块
  /** 最大大小（单位为MB） */
  private Integer maxSize;
  // endregion
}
