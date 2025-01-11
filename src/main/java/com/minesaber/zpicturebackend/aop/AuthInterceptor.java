package com.minesaber.zpicturebackend.aop;

import com.minesaber.zpicturebackend.annotation.AuthCheck;
import com.minesaber.zpicturebackend.exception.ErrorCode;
import com.minesaber.zpicturebackend.exception.ThrowUtils;
import com.minesaber.zpicturebackend.model.entity.User;
import com.minesaber.zpicturebackend.model.enums.UserRole;
import com.minesaber.zpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class AuthInterceptor {
  @Resource private UserService userService;

  /**
   * 执行拦截
   *
   * @param joinPoint 切点
   * @param authCheck 权限校验注解
   * @return object
   * @throws Throwable throwable
   */
  @Around("@annotation(authCheck)")
  public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
    // 1、检查用户登录状态
    ServletRequestAttributes servletRequestAttributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = null;
    if (servletRequestAttributes != null) {
      request = servletRequestAttributes.getRequest();
    }
    User loginUser = userService.getLoginUser(request);
    String userRole = loginUser.getUserRole();
    UserRole userRoleEnum = UserRole.getEnumByValue(userRole);
    // 2、检查注解
    String role = authCheck.mustRole();
    UserRole roleEnum = UserRole.getEnumByValue(role);
    if (roleEnum == null) {
      // todo 使用注解时配置参数错误，可能需要做日志记录
      return joinPoint.proceed();
    }
    // 3、检查是否符合
    ThrowUtils.throwIf(
        UserRole.ADMIN == roleEnum && UserRole.ADMIN != userRoleEnum, ErrorCode.NO_AUTH_ERROR);
    // 4、放行
    return joinPoint.proceed();
  }
}
