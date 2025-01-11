package com.minesaber.zpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/** 用户角色枚举 */
@Getter
public enum UserRole {
  USER("用户", "user"),
  ADMIN("管理员", "admin");
  private final String text;
  private final String value;

  UserRole(String text, String value) {
    this.text = text;
    this.value = value;
  }

  public static UserRole getEnumByValue(String value) {
    if (ObjUtil.isEmpty(value)) {
      return null;
    }
    for (UserRole userRole : UserRole.values()) {
      if (userRole.value.equals(value)) {
        return userRole;
      }
    }
    return null;
  }
}
