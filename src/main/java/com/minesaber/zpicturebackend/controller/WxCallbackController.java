package com.minesaber.zpicturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.minesaber.zpicturebackend.helpers.WxAckHelper;
import com.minesaber.zpicturebackend.helpers.WxLoginHelper;
import com.minesaber.zpicturebackend.model.dto.user.WxMsgRequest;
import com.minesaber.zpicturebackend.model.dto.user.WxTxtMsgRequest;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.entity.user.UserWx;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.service.UserWxService;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.utils.RandomStrGenerateUtil;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping(path = "/wx/054105fdcfdd4643a46caa388248c39c")
public class WxCallbackController {
  @Resource private WxLoginHelper qrLoginHelper;
  @Resource private WxAckHelper wxHelper;
  @Resource private UserService userService;
  @Resource private UserWxService userWxService;

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
    String event = msg.getEvent();
    String content = msg.getContent();
    // todo 没有关注，则返回未关注，不进行消息处理

    if (event.equals("subscribe")) {
      // 查询微信表，判断是否已注册
      UserWx userWx = userWxService.getByOpenId(openId);
      long userId;
      UserWx userwx = new UserWx();
      userwx.setIsFollow(1);
      userwx.setLatestSubscribeTime(new Date());
      if (userWx != null) {
        // 已注册，更新信息
        userwx.setId(userWx.getId());
      } else {
        // 第一次关注，注册新用户，更新信息
        String key = RandomStrGenerateUtil.generateUUID(12);
        userId = userService.register(key, key);
        userwx.setUserId(userId);
        userwx.setOpenId(openId);
        userwx.setFirstSubscribeTime(new Date());
      }
      userWxService.saveOrUpdate(userwx);
    }
    // todo 取关，更新follow字段
    WxMsgRequest res = wxHelper.buildResponseBody(msg.getFromUserName(), msg.getEvent(), content);
    fillResVo(res, msg);
    return res;
  }

  private void fillResVo(WxMsgRequest res, WxTxtMsgRequest msg) {
    res.setToUserName(msg.getFromUserName());
    res.setFromUserName(msg.getToUserName());
    res.setCreateTime(System.currentTimeMillis() / 1000);
  }
}
