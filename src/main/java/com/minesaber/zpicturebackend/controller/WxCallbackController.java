package com.minesaber.zpicturebackend.controller;

import cn.hutool.core.util.StrUtil;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.helpers.WxAckHelper;
import com.minesaber.zpicturebackend.model.dto.user.WxMsgRequest;
import com.minesaber.zpicturebackend.model.entity.user.UserWx;
import com.minesaber.zpicturebackend.model.vo.user.WxMsgVO;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.service.UserWxService;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.utils.RandomStrGenerateUtil;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import java.util.Date;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("${secret.path}")
public class WxCallbackController {
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
   * @param request 公众号信息
   * @return 处理结果
   */
  @PostMapping(
      path = "/forward",
      consumes = {"application/xml", "text/xml"},
      produces = "application/xml;charset=utf-8")
  public WxMsgVO callBack(@RequestBody WxMsgRequest request) {
    String msgType = request.getMsgType().toLowerCase();
    String openId = request.getFromUserName();
    String event = request.getEvent();
    boolean isFirstVisit = false;
    if (msgType.equals("event")) {
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
          isFirstVisit = true;
          String key = RandomStrGenerateUtil.generateUUID(12);
          userId = userService.register(key, key);
          userwx.setUserId(userId);
          userwx.setOpenId(openId);
          userwx.setFirstSubscribeTime(new Date());
        }
        DatabaseUtils.executeWithExceptionLogging(() -> userWxService.saveOrUpdate(userwx));
      } else if (event.equals("unsubscribe")) {
        // 取关，更新follow字段
        UserWx userWx = userWxService.getByOpenId(openId);
        if (userWx != null) {
          userWx.setIsFollow(0);
          DatabaseUtils.executeWithExceptionLogging(() -> userWxService.saveOrUpdate(userWx));
        }
      }
    }
    WxMsgVO vo = wxHelper.buildResponseBody(request, isFirstVisit);
    fillResVo(vo, request);
    return vo;
  }

  private void fillResVo(WxMsgVO vo, WxMsgRequest request) {
    vo.setToUserName(request.getFromUserName());
    vo.setFromUserName(request.getToUserName());
    vo.setCreateTime(System.currentTimeMillis() / 1000);
  }
}
