package com.minesaber.zpicturebackend.utils;

import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.enums.ErrorCode;

/** 响应结果工具 */
public class ResultUtils {
  /**
   * 成功
   *
   * @param data 数据
   * @return 响应
   * @param <T> 数据类型
   */
  public static <T> Response<T> success(T data) {
    return new Response<>(0, "OK", data);
  }

  /**
   * 失败
   *
   * @param code 状态码
   * @param message 描述信息
   * @return 响应
   */
  public static Response<?> error(int code, String message) {
    return new Response<>(code, message);
  }

  /**
   * 失败
   *
   * @param errorCode 错误码
   * @return 响应
   */
  public static Response<?> error(ErrorCode errorCode) {
    return new Response<>(errorCode);
  }

  /**
   * 失败
   *
   * @param errorCode 错误码
   * @return 响应
   */
  public static Response<?> error(ErrorCode errorCode, String message) {
    return new Response<>(errorCode, message);
  }
}
