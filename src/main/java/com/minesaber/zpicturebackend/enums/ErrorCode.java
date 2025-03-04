package com.minesaber.zpicturebackend.enums;

import lombok.Getter;

/** 错误码 */
@Getter
public enum ErrorCode {
  // todo 系统错误码使用待规范
  // todo 系统注解使用待规范
  // 成功
  SUCCESS(0, "成功"),
  // 请求参数错误
  PARAMS_ERROR(40000, "请求参数错误"),
  // 请求未经授权
  NOT_LOGIN_ERROR(40100, "未登录"),
  NO_AUTH_ERROR(40101, "无权限"),
  // 请求未经授权，且消息需要脱敏
  // todo 何时应该隐藏资源状态待规范
  FORBIDDEN_ERROR(40300, "禁止访问"),
  // 资源不存在
  NOT_FOUND_ERROR(40400, "资源不存在"),
  // 内部异常，50000为底层异常，50001为上层异常
  SYSTEM_ERROR(50000, "系统异常"),
  OPERATION_ERROR(50001, "操作失败"),
  MAINTENANCE_ERROR(50300, "系统维护中，请稍后再试或联系管理员");

  /** 状态码 */
  private final int code;

  /** 描述信息 */
  private final String message;

  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }
}
