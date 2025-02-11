package com.minesaber.zpicturebackend.model.vo.base;

import com.minesaber.zpicturebackend.enums.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局响应封装类
 *
 * @param <T> 数据类型
 */
@Data
public class Response<T> implements Serializable {
  private static final long serialVersionUID = 7397148946328208760L;
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
  public Response(int code, String message, T data) {
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
  public Response(int code, String message) {
    this(code, message, null);
  }

  /**
   * 全局响应
   *
   * @param errorCode 错误码
   */
  public Response(ErrorCode errorCode) {
    this(errorCode.getCode(), errorCode.getMessage(), null);
  }

  /**
   * 全局响应
   *
   * @param errorCode 错误码
   * @param message 描述信息
   */
  public Response(ErrorCode errorCode, String message) {
    this(errorCode.getCode(), message, null);
  }

  /**
   * 全局响应
   *
   * @param data 数据
   * @param code 状态码
   */
  public Response(T data, int code) {
    this(code, "", data);
  }
}
