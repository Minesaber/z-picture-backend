package com.minesaber.zpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/** 用户注册请求 */
@Data
public class UserRegisterRequest implements Serializable {
  private static final long serialVersionUID = -8836680501155035956L;

  /** 账户 */
  private String userAccount;

  /** 密码 */
  private String userPassword;

  /** 确认密码 */
  private String checkPassword;
}
