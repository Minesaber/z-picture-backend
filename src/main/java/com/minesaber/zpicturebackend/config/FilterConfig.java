package com.minesaber.zpicturebackend.config;

import com.minesaber.zpicturebackend.servlet.InitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
  
  @Bean
  public FilterRegistrationBean<InitFilter> loggingFilter() {
    FilterRegistrationBean<InitFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new InitFilter());
    registrationBean.addUrlPatterns("/*");
    return registrationBean;
  }
}
