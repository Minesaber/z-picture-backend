package com.minesaber.zpicturebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 全局跨域配置 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowCredentials(true)
        .allowedOriginPatterns("*")
        .allowedHeaders("*")
        .exposedHeaders("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
  }
}
