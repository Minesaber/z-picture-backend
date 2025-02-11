package com.minesaber.zpicturebackend.exception;

import com.minesaber.zpicturebackend.enums.ErrorCode;
import lombok.Getter;

/** 业务异常 */
@Getter
public class BusinessException extends RuntimeException {
  /** 状态码 */
  private final int code;

  /**
   * 业务异常
   *
   * @param code 状态码
   * @param message 描述信息
   */
  public BusinessException(int code, String message) {
    super(message);
    this.code = code;
  }

  /**
   * 业务异常
   *
   * @param errorCode 错误码
   */
  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.code = errorCode.getCode();
  }

  /**
   * 业务异常
   *
   * @param errorCode 错误码
   * @param message 描述信息
   */
  public BusinessException(ErrorCode errorCode, String message) {
    super(message);
    this.code = errorCode.getCode();
  }
}
