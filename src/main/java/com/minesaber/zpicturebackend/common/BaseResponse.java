package com.minesaber.zpicturebackend.common;

import com.minesaber.zpicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局响应封装类
 *
 * @param <T> 数据类型
 */
@Data
public class BaseResponse<T> implements Serializable {
  private final int code;
  private final String message;
  private final T data;

  /**
   * 全局响应
   *
   * @param code 状态码
   * @param message 描述信息
   * @param data 数据
   */
  public BaseResponse(int code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  /**
   * 全局响应
   *
   * @param code 状态码
   * @param message 描述信息
   */
  public BaseResponse(int code, String message) {
    this(code, message, null);
  }

  /**
   * 全局响应
   *
   * @param errorCode 错误码
   */
  public BaseResponse(ErrorCode errorCode) {
    this(errorCode.getCode(), errorCode.getMessage(), null);
  }

  /**
   * 全局响应
   *
   * @param errorCode 错误码
   * @param message 描述信息
   */
  public BaseResponse(ErrorCode errorCode, String message) {
    this(errorCode.getCode(), message, null);
  }

  /**
   * 全局响应
   *
   * @param data 数据
   * @param code 状态码
   */
  public BaseResponse(T data, int code) {
    this(code, "", data);
  }
}
