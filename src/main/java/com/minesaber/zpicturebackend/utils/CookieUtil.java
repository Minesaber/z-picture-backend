package com.minesaber.zpicturebackend.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {
  /**
   * 从 HttpServletRequest 中获取指定名称的 Cookie
   *
   * @param request HttpServletRequest 请求对象
   * @param key Cookie 名称
   * @return 如果找到了匹配的 Cookie，返回该 Cookie；否则返回 null
   */
  public static Cookie getCookie(HttpServletRequest request, String key) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      Optional<Cookie> cookieOptional =
          Arrays.stream(cookies)
              .filter(cookie -> cookie.getName().equals(key))
              .findFirst();
      return cookieOptional.orElse(null);
    }
    return null;
  }
}
