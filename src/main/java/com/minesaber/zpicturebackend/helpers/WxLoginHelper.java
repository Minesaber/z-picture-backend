package com.minesaber.zpicturebackend.helpers;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.minesaber.zpicturebackend.model.bo.ClientInfo;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import com.minesaber.zpicturebackend.utils.RandomStrGenerateUtil;
import com.minesaber.zpicturebackend.utils.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** 微信登录工具 */
@Slf4j
@Component
public class WxLoginHelper {
  // todo 删除魔数
  /** 默认sse连接超时时间为 5分钟 */
  private static final Long SSE_EXPIRE = 5 * 60 * 1000L;

  /** 标识码 与 sse连接 的映射 */
  private final LoadingCache<String, SseEmitter> sseCache =
      Caffeine.newBuilder().maximumSize(100).expireAfterWrite(3, TimeUnit.MINUTES).build(s -> null);

  /**
   * 缓存 JSESSIONID 与 cIdCode 的映射
   *
   * <p>当缓存不存在或已过期，若调用get()方法，则会自动调用CacheLoader.load()方法加载最新值
   */
  private final LoadingCache<String, String> cIdCodeCache =
      Caffeine.newBuilder()
          .maximumSize(100)
          .expireAfterWrite(3, TimeUnit.MINUTES)
          .build(
              jSessionId -> {
                while (true) {
                  String cIdCode = RandomStrGenerateUtil.generateCode();
                  if (sseCache.getIfPresent(jSessionId) == null) {
                    return cIdCode;
                  }
                }
              });

  /**
   * 基于当前JSESSIONID，新生成一个sse
   *
   * @return sse连接
   */
  public SseEmitter subscribe(HttpServletRequest servletRequest) {
    // 首次连接，生成标识码cIdCode
    ClientInfo clientInfo =
        (ClientInfo) servletRequest.getServletContext().getAttribute("clientInfo");
    String jSessionId = clientInfo.getJSessionId();
    String cIdCode = cIdCodeCache.getIfPresent(jSessionId);
    // 关闭原有的sse连接（因为过期时间需要重置）
    SseEmitter oldSse = cIdCode == null ? null : sseCache.getIfPresent(cIdCode);
    if (oldSse != null) {
      oldSse.complete();
    }
    // cIdCode不存在，则生成新的cIdCode
    if (cIdCode == null) {
      cIdCode = cIdCodeCache.get(jSessionId);
    }
    // 生成新的sse连接，与原有的cIdCode关联后返回
    SseEmitter sseEmitter = new SseEmitter(SSE_EXPIRE);
    String finalCIdCode = cIdCode;
    sseEmitter.onTimeout(
        () -> {
          sseEmitter.complete();
          close(finalCIdCode);
        });
    sseEmitter.onError(
        e -> {
          sseEmitter.complete();
          close(finalCIdCode);
        });
    assert cIdCode != null;
    sseCache.put(cIdCode, sseEmitter);
    return sseEmitter;
  }

  /**
   * 根据code关闭sse
   *
   * @param cIdCode 标识码
   */
  private void close(String cIdCode) {
    if (cIdCode == null) return;
    SseEmitter sseEmitter = sseCache.getIfPresent(cIdCode);
    if (sseEmitter != null) {
      sseEmitter.complete();
      sseCache.invalidate(cIdCode);
    }
  }

  /**
   * 重新发送标识码
   *
   * @return cIdCode
   */
  public String resend(HttpServletRequest servletRequest) {
    ClientInfo clientInfo =
        (ClientInfo) servletRequest.getServletContext().getAttribute("clientInfo");
    String jSessionId = clientInfo.getJSessionId();
    String cIdCode = cIdCodeCache.getIfPresent(jSessionId);
    SseEmitter oldSse = cIdCode == null ? null : sseCache.getIfPresent(cIdCode);
    if (oldSse != null) {
      return "success-cIdCode?" + cIdCode;
    }
    return "fail-fetch";
  }

  /**
   * 刷新标识码
   *
   * @return cIdCode
   */
  public String refreshCode(HttpServletRequest servletRequest) {
    // 确定JSESSIONID与sse连接
    ClientInfo clientInfo =
        (ClientInfo) servletRequest.getServletContext().getAttribute("clientInfo");
    String jSessionId = clientInfo.getJSessionId();
    String oldCode = cIdCodeCache.getIfPresent(jSessionId);
    // 缺省则快速失败
    SseEmitter oldSse = oldCode == null ? null : sseCache.getIfPresent(oldCode);
    if (oldSse == null) return "fail-refresh";
    // 都找得到，则先删除老数据
    cIdCodeCache.invalidate(jSessionId);
    sseCache.invalidate(oldCode);
    // 再重新生成标识码cIdCode，并基于JSESSIONID和oldSse更新缓存
    String cIdCode = cIdCodeCache.get(jSessionId);
    assert cIdCode != null;
    sseCache.put(cIdCode, oldSse);
    return cIdCode;
  }

  /**
   * 登录
   *
   * @param cIdCode 客户端标识码
   * @return 登录结果
   */
  public boolean login(String cIdCode, UserVO user) {
    SseEmitter sseEmitter = sseCache.getIfPresent(cIdCode);
    if (sseEmitter == null) return false;
    try {
      // 登录成功，更新相应会话的session
      String jSessionId = cIdCodeCache.getIfPresent(cIdCode);
      if (jSessionId == null) return false;
      SessionUtil.updateSessionByJSessionId(jSessionId, user);
      sseEmitter.send("success-login");
      return true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      sseEmitter.complete();
      sseCache.invalidate(cIdCode);
    }
  }
}
