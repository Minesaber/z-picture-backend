package com.minesaber.zpicturebackend.common;

import com.minesaber.zpicturebackend.exception.ErrorCode;

/** 响应结果工具类 */
public class ResultUtils {
  /**
   * 成功
   *
   * @param data 数据
   * @return 响应
   * @param <T> 数据类型
   */
  public static <T> BaseResponse<?> success(T data) {
    return new BaseResponse<>(0, "OK", data);
  }

  /**
   * 失败
   *
   * @param code 状态码
   * @param message 描述信息
   * @return 响应
   */
  public static BaseResponse<?> error(int code, String message) {
    return new BaseResponse<>(code, message);
  }

  /**
   * 失败
   *
   * @param errorCode 错误码
   * @return 响应
   */
  public static BaseResponse<?> error(ErrorCode errorCode) {
    return new BaseResponse<>(errorCode);
  }

  /**
   * 失败
   *
   * @param errorCode 错误码
   * @return 响应
   */
  public static BaseResponse<?> error(ErrorCode errorCode, String message) {
    return new BaseResponse<>(errorCode, message);
  }
}
