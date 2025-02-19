package com.minesaber.zpicturebackend.model.dto.user;

import com.minesaber.zpicturebackend.model.dto.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/** 用户查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** id */
  private Long id;

  /** 用户账号 */
  private String userAccount;

  /** 用户角色：user/admin */
  private String userRole;

  /** 用户昵称 */
  private String userName;

  /** 用户简介 */
  private String userProfile;
}
