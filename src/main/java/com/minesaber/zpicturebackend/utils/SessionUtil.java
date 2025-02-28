package com.minesaber.zpicturebackend.utils;

import com.minesaber.zpicturebackend.constants.UserConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.servlet.SessionListener;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

public class SessionUtil {
  @Resource private SessionListener sessionListener;

  /**
   * 根据指定 JSESSIONID 获取 session，并更新 session 数据
   *
   * @param sessionId 需要更新的 JSESSIONID
   * @param sessionValue 更新后的值
   */
  public static void updateSessionByJSessionId(String sessionId, Object sessionValue) {
    HttpSession session = SessionListener.getSession(sessionId);
    ThrowUtils.throwIf(session == null, ErrorCode.OPERATION_ERROR, "会话已过期");
    session.setAttribute(UserConstant.LOGIN_USER_STATE, sessionValue);
  }
}
