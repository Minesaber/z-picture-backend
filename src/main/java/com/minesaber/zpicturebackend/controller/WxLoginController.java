package com.minesaber.zpicturebackend.controller;

import com.minesaber.zpicturebackend.constants.UserConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.helpers.WxLoginHelper;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.model.vo.user.CodeRefreshVO;
import com.minesaber.zpicturebackend.utils.ResultUtils;
import com.minesaber.zpicturebackend.utils.SystemStatusUtil;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import java.util.Base64;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/wx")
// todo 需要防刷
public class WxLoginController {
  @Resource private WxLoginHelper wxLoginHelper;

  /**
   * 基于当前JSESSIONID建立sse连接（响应内容以事件流的格式传输）
   *
   * <p>适用于首次连接或断开重连，总是返回重置的sse连接
   *
   * @return sse连接
   */
  @GetMapping(
      path = "/subscribe",
      produces = {org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE})
  public SseEmitter subscribe(HttpServletRequest servletRequest) {
    return wxLoginHelper.subscribe(servletRequest);
  }

  /**
   * 基于当前JSESSIONID再次获取标识码
   *
   * <p>成功返回success-cIdCode?xxx，失败则返回fail-fetch
   *
   * @return 标识码
   */
  @GetMapping("/login/fetch")
  public Response<String> resendCode(HttpServletRequest servletRequest) {
    ThrowUtils.throwIf(SystemStatusUtil.isClosed(), ErrorCode.MAINTENANCE_ERROR);
    return ResultUtils.success(wxLoginHelper.resend(servletRequest));
  }

  /**
   * 基于当前JSESSIONID刷新标识码
   *
   * @return 登录视图
   */
  @GetMapping("/login/refresh")
  public Response<CodeRefreshVO> refresh(HttpServletRequest servletRequest) {
    ThrowUtils.throwIf(SystemStatusUtil.isClosed(), ErrorCode.MAINTENANCE_ERROR);
    CodeRefreshVO codeRefreshVO = new CodeRefreshVO();
    String result = wxLoginHelper.refreshCode(servletRequest);
    if (result.equals("fail-refresh")) {
      codeRefreshVO.setReconnect(true);
    } else {
      codeRefreshVO.setCIdCode(result);
    }
    return ResultUtils.success(codeRefreshVO);
  }

  @GetMapping("/login")
  public Response<Boolean> login(HttpServletRequest servletRequest) {
    ThrowUtils.throwIf(SystemStatusUtil.isClosed(), ErrorCode.MAINTENANCE_ERROR);
    String jSessionId = servletRequest.getSession().getId();
    jSessionId = Base64.getEncoder().encodeToString(jSessionId.getBytes());
    User user = wxLoginHelper.login(jSessionId);
    ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR, "会话无效或已过期，请尝试刷新页面重新登录");
    servletRequest.getSession().setAttribute(UserConstant.LOGIN_USER_STATE, user);
    return ResultUtils.success(true);
  }
}
