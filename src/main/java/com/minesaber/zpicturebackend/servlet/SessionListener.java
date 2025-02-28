package com.minesaber.zpicturebackend.servlet;

import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashMap;
import java.util.Map;

@WebListener
@Component
public class SessionListener implements HttpSessionListener {
  private static final Map<String, HttpSession> sessions = new HashMap<>();

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    sessions.put(se.getSession().getId(), se.getSession());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    sessions.remove(se.getSession().getId());
  }

  public static HttpSession getSession(String sessionId) {
    return sessions.get(sessionId);
  }
}
