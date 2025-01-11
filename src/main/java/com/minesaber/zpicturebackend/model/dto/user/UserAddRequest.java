package com.minesaber.zpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/** 用户添加请求 */
@Data
public class UserAddRequest implements Serializable {
  /** 账号 */
  private String userAccount;

  /** 用户角色：user/admin */
  private String userRole;

  /** 用户昵称 */
  private String userName;

  /** 用户头像 */
  private String userAvatar;

  /** 用户简介 */
  private String userProfile;

  private static final long serialVersionURD = 1L;
}
