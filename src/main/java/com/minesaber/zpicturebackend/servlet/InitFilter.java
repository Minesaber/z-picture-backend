package com.minesaber.zpicturebackend.servlet;

import cn.hutool.core.util.StrUtil;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.utils.ThreadInfoUtil;
import com.minesaber.zpicturebackend.model.bo.ClientInfo;
import com.minesaber.zpicturebackend.utils.CookieUtil;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/*", filterName = "initFilter", asyncSupported = true)
public class InitFilter implements Filter {

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    try {
      HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
      String path = httpServletRequest.getRequestURI();
      if (path.equals("/api/health")) {
        initClientInfo((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
      }
      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      // 一个链路请求完毕，清空线程本地变量所关联的值
      ThreadInfoUtil.clear();
    }
  }

  /**
   * 初始化客户端信息
   *
   * @param servletRequest 请求
   * @param servletResponse 响应
   */
  private void initClientInfo(
      HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    Cookie cookie = CookieUtil.getCookie(servletRequest, "SESSION");
    String jsessionid = cookie != null ? cookie.getValue() : null;
    ThrowUtils.throwIf(StrUtil.isBlank(jsessionid), ErrorCode.PARAMS_ERROR, "SESSION 不能为空");
    ClientInfo clientInfo = new ClientInfo();
    clientInfo.setJSessionId(jsessionid);
    servletRequest.getServletContext().setAttribute("clientInfo", clientInfo);
  }
}
