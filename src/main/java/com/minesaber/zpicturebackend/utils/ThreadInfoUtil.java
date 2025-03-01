package com.minesaber.zpicturebackend.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.model.bo.ClientInfo;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/** 线程本地变量工具 */
// todo 之后考虑使用JWT再采用这个方案
public class ThreadInfoUtil {
  private static final TransmittableThreadLocal<ClientInfo> contexts =
      new TransmittableThreadLocal<>();

  public static void addInfo(ClientInfo info) {
    contexts.set(info);
  }

  public static ClientInfo getInfo() {
    return contexts.get();
  }

  public static void clear() {
    contexts.remove();
  }

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
