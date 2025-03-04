package com.minesaber.zpicturebackend.utils;

import java.util.Objects;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/** 系统状态 */
@Component
public class SystemStatusUtil implements ApplicationContextAware {
  private static RedisTemplate<String, String> redisTemplate;

  private static boolean isClosed = false;

  public static void closeAccess() {
    isClosed = true;
    Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
  }

  public static void openAccess() {
    isClosed = false;
  }

  public static boolean isClosed() {
    return isClosed;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    redisTemplate = applicationContext.getBean("stringRedisTemplate", RedisTemplate.class);
  }
}
