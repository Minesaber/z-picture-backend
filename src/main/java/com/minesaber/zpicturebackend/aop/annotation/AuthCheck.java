package com.minesaber.zpicturebackend.aop.annotation;

import com.minesaber.zpicturebackend.constants.UserConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
  /**
   * 必须具有某个角色
   *
   * @return 角色 Key
   */
  String mustRole() default UserConstant.DEFAULT_ROLE;
}
