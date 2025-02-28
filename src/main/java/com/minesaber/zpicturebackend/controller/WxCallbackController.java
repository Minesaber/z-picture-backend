package com.minesaber.zpicturebackend.controller;

import cn.hutool.core.util.StrUtil;
import com.minesaber.zpicturebackend.helpers.WxAckHelper;
import com.minesaber.zpicturebackend.helpers.WxLoginHelper;
import com.minesaber.zpicturebackend.model.dto.user.WxMsgRequest;
import com.minesaber.zpicturebackend.model.dto.user.WxTxtMsgRequest;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(path = "/wx")
// todo 需要防刷
public class WxCallbackController {
  @Resource private WxLoginHelper qrLoginHelper;
  @Resource private WxAckHelper wxHelper;

  /**
   * 健康检查
   *
   * @param request 微信请求
   * @return 检查字符串
   */
  @GetMapping("/forward")
  public String health(HttpServletRequest request) {
    String echoStr = request.getParameter("echostr");
    ThrowUtils.throwIf(StrUtil.isBlank(echoStr), ErrorCode.SYSTEM_ERROR);
    return echoStr;
  }

  /**
   * 信息处理（接收、返回xml格式的数据）
   *
   * @param msg 公众号信息
   * @return 处理结果
   */
  @PostMapping(
      path = "/forward",
      consumes = {"application/xml", "text/xml"},
      produces = "application/xml;charset=utf-8")
  public WxMsgRequest callBack(@RequestBody WxTxtMsgRequest msg) {
    String openId = msg.getFromUserName();
    String content = msg.getContent();
    String event = msg.getEvent();
    // todo 没有关注，则返回未关注，不进行消息处理
    // 首次关注执行注册
    if (event.equals("subscribe")) {

    }

    WxMsgRequest res = wxHelper.buildResponseBody(msg.getEvent(), content, msg.getFromUserName());
    fillResVo(res, msg);
    return res;
  }

  private void fillResVo(WxMsgRequest res, WxTxtMsgRequest msg) {
    res.setFromUserName(msg.getToUserName());
    res.setToUserName(msg.getFromUserName());
    res.setCreateTime(System.currentTimeMillis() / 1000);
  }
}
