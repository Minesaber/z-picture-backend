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

  /** 默认头像地址列表 */
  String[] DEFAULT_AVATAR_URL_LIST = {
    "https://s3.uuu.ovh/imgs/2023/06/11/1a2d95dc8b4c178a.png",
    "https://s3.uuu.ovh/imgs/2023/06/11/9cddbe28dc3eee6c.png",
    "https://s3.uuu.ovh/imgs/2023/06/11/ae697d77da6b576f.png"
  };
}
