package com.minesaber.zpicturebackend.common;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class DatabaseUtils {
  /**
   * 记录数据库操作时可能发生的异常
   *
   * @param supplier 数据库操作
   * @return 执行结果
   * @param <T> 结果类型
   */
  public static <T> T executeWithLog(Supplier<T> supplier) {
    // todo 完善数据库操作的日志
    try {
      return supplier.get();
    } catch (Exception e) {
      log.error("数据库异常", e);
    }
    throw new RuntimeException();
  }

  /**
   * 记录数据库操作时可能发生的异常
   *
   * @param supplier 数据库操作
   * @param msg 异常描述
   * @param params 参数
   * @return 执行结果
   * @param <T> 结果类型
   */
  public static <T> T executeWithLog(Supplier<T> supplier, String msg, Object... params) {
    // todo 完善数据库操作的日志
    try {
      return supplier.get();
    } catch (Exception e) {
      log.error(msg, e, params);
    }
    throw new RuntimeException();
  }
}
