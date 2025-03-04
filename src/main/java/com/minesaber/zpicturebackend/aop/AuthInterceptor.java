package com.minesaber.zpicturebackend.aop;

import com.minesaber.zpicturebackend.aop.annotation.AuthCheck;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.enums.UserRole;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
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
    // 1、检查是否已登录
    // todo 用户信息更新后，无法及时反馈
    RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
    User currentUser = userService.getLoginUser(request);
    ThrowUtils.throwIf(
        currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
    // 2、检查注解
    UserRole annoRoleEnum = UserRole.getEnumByValue(authCheck.mustRole());
    ThrowUtils.throwIf(annoRoleEnum == null, ErrorCode.SYSTEM_ERROR);
    // 3、如果仅要求用户权限
    if (UserRole.USER == annoRoleEnum) {
      return joinPoint.proceed();
    }
    // 3、如果要求管理员权限
    UserRole userRoleEnum = UserRole.getEnumByValue(currentUser.getUserRole());
    ThrowUtils.throwIf(
        UserRole.ADMIN == annoRoleEnum && UserRole.ADMIN != userRoleEnum, ErrorCode.NO_AUTH_ERROR);
    // 4、放行
    return joinPoint.proceed();
  }
}
