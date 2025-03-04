package com.minesaber.zpicturebackend.utils;

import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseUtils {
  /**
   * 记录数据库操作时可能发生的异常
   *
   * @param supplier 数据库操作
   * @return 执行结果
   * @param <T> 结果类型
   */
  public static <T> T executeWithExceptionLogging(Supplier<T> supplier) {
    // todo 数据库操作日志待规范
    try {
      return supplier.get();
    } catch (Exception e) {
      log.error("数据库异常", e);
      throw new RuntimeException(e);
    }
  }
}
