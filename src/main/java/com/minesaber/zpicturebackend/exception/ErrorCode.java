package com.minesaber.zpicturebackend.exception;

import lombok.Getter;

/** 错误码 */
@Getter
public enum ErrorCode {
  // 成功
  SUCCESS(0, "成功"),
  // 请求参数错误
  PARAMS_ERROR(40000, "请求参数错误"),
  // 请求未经授权
  NOT_LOGIN_ERROR(40100, "未登录"),
  NO_AUTH_ERROR(40101, "无权限"),
  // 禁止访问
  FORBIDDEN_ERROR(40300, "禁止访问"),
  // 资源不存在
  NOT_FOUND_ERROR(40400, "资源不存在"),
  // 内部异常
  SYSTEM_ERROR(50000, "系统内部异常"),
  OPERATION_ERROR(50001, "系统操作失败");

  /** 状态码 */
  private final int code;

  /** 描述信息 */
  private final String message;

  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }
}
