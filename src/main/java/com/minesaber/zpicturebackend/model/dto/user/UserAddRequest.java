package com.minesaber.zpicturebackend.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/** 用户添加请求 */
@Data
public class UserAddRequest implements Serializable {
  private static final long serialVersionUID = 1L;

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
}
