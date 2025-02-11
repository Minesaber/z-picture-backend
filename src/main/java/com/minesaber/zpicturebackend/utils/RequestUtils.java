package com.minesaber.zpicturebackend.utils;

import com.minesaber.zpicturebackend.enums.ErrorCode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class RequestUtils {
  /**
   * 获取HttpServletRequest实例
   *
   * @return request实例
   */
  public static HttpServletRequest getHttpServletRequest() {
    ServletRequestAttributes servletRequestAttributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    ThrowUtils.throwIf(servletRequestAttributes == null, ErrorCode.SYSTEM_ERROR);
    return servletRequestAttributes.getRequest();
  }

  /**
   * 获取session中指定attribute的值
   *
   * @param key attribute键
   * @return attribute值
   */
  public static Object getAttribute(String key) {
    HttpServletRequest request = getHttpServletRequest();
    return request.getSession().getAttribute(key);
  }
}
