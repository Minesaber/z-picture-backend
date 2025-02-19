package com.minesaber.zpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/** 用户更新请求 */
@Data
public class UserUpdateRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** id */
  private Long id;

  /** 用户角色：user/admin */
  private String userRole;

  /** 用户昵称 */
  private String userName;

  /** 用户头像 */
  private String userAvatar;

  /** 用户简介 */
  private String userProfile;
}
