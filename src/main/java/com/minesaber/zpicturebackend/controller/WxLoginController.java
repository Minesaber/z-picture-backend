package com.minesaber.zpicturebackend.controller;

import com.minesaber.zpicturebackend.helpers.WxLoginHelper;
import com.minesaber.zpicturebackend.model.vo.user.CodeRefreshVO;
import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
  @ResponseBody
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
  @ResponseBody
  public Response<String> resendCode(HttpServletRequest servletRequest) {
    return ResultUtils.success(wxLoginHelper.resend(servletRequest));
  }

  /**
   * 基于当前JSESSIONID刷新标识码
   *
   * @return 登录视图
   */
  @GetMapping("/login/refresh")
  @ResponseBody
  public Response<CodeRefreshVO> refresh(HttpServletRequest servletRequest) {
    CodeRefreshVO codeRefreshVO = new CodeRefreshVO();
    String result = wxLoginHelper.refreshCode(servletRequest);
    if (result.equals("fail-refresh")) {
      codeRefreshVO.setReconnect(true);
    } else {
      codeRefreshVO.setCIdCode(result);
    }
    return ResultUtils.success(codeRefreshVO);
  }
}
