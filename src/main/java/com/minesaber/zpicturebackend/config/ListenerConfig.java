package com.minesaber.zpicturebackend.config;

import com.minesaber.zpicturebackend.servlet.SessionListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListenerConfig {
  @Bean
  public ServletListenerRegistrationBean<SessionListener> listener() {
    return new ServletListenerRegistrationBean<>(new SessionListener());
  }
}
