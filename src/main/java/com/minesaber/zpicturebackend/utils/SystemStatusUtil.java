package com.minesaber.zpicturebackend.utils;

import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Objects;

/** 系统状态 */
public class SystemStatusUtil {
  @Resource private static RedisTemplate<String, String> redisTemplate;

  public static boolean isClosed = false;

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
}
