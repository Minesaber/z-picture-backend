package com.minesaber.zpicturebackend.constants;

/** 用户常量 Key */
public interface UserConstant {
  /** 用户名最小长度 */
  Integer USER_ACCOUNT_MIN_LENGTH = 6;

  /** 用户名最大长度 */
  Integer USER_ACCOUNT_MAX_LENGTH = 20;

  /** 用户密码最小长度 */
  Integer USER_PASSWORD_MIN_LENGTH = 8;

  /** 用户密码最大长度 */
  Integer USER_PASSWORD_MAX_LENGTH = 30;

  /** 登录状态 Key */
  String LOGIN_USER_STATE = "login_user";

  /** 默认角色 Key */
  String DEFAULT_ROLE = "user";

  /** 用户 Key */
  String USER = "user";

  /** 管理员角色 Key */
  String ADMIN_ROLE = "admin";
}
