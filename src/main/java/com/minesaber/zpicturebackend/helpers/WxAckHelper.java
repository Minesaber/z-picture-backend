package com.minesaber.zpicturebackend.helpers;

import com.minesaber.zpicturebackend.model.dto.user.WxMsgRequest;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.entity.user.UserWx;
import com.minesaber.zpicturebackend.model.vo.user.WxMsgVO;
import com.minesaber.zpicturebackend.model.vo.user.WxTxtMsgVO;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.service.UserWxService;
import com.minesaber.zpicturebackend.utils.RandomStrGenerateUtil;
import com.minesaber.zpicturebackend.utils.SystemStatusUtil;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class WxAckHelper {
  @Resource private WxLoginHelper wxLoginHelper;
  @Resource private UserService userService;
  @Resource private UserWxService userWxService;

  public WxTxtMsgVO buildResponseBody(WxMsgRequest msg, boolean isFirstVisit) {
    String msgType = msg.getMsgType().toLowerCase();
    String event = msg.getEvent();
    String content = msg.getContent();
    String fromUser = msg.getFromUserName();
    String answer = "";
    if (msgType.equals("event")) {
      if ("subscribe".equalsIgnoreCase(event)) {
        answer =
            "欢迎关注 ZPicture云图库社区📷\n" + "在这里，您可以轻松分享与管理图片，捕捉每个精彩瞬间🌟\n" + "目前系统仍在开发阶段，如需帮助请联系管理员💬";
      }
    } else if (msgType.equals("text")) {
      if (content != null) {
        System.out.println("收到来自微信公众号的消息：" + content);
        if (RandomStrGenerateUtil.isValidCode(content)) {
          System.out.println("收到来自微信公众号的标识码：" + content);
          UserWx userWx = userWxService.getByOpenId(fromUser);
          User user = userService.getById(userWx.getUserId());
          try {
            if (wxLoginHelper.wxLogin(content, user)) {
              System.out.println("微信登录成功，已刷新待登录用户缓存");
              answer =
                  isFirstVisit ? "欢迎新用户\uD83C\uDF89 （づ￣3￣）づ╭❤️～" : "登录成功\uD83C\uDF89 欢迎回来ヾ(^▽^*)))";
            } else {
              answer = "⚠️ 请检查标识码，或尝试重新连接";
            }
          } catch (Exception e) {
            answer = "⚠️ 检测到客户端连接已中断，请尝试重新打开微信登录卡片以刷新连接";
          }
        } else if (content.startsWith("StopZPicture2025")) {
          SystemStatusUtil.closeAccess();
          answer = "系统已关闭访问\uD83D\uDD12";
        } else if (content.startsWith("RunZPicture2025")) {
          SystemStatusUtil.openAccess();
          answer = "系统已开放访问\uD83D\uDD13";
        } else {
          answer = "如有疑问，请联系管理员💬";
        }
      }
    }
    WxTxtMsgVO vo = new WxTxtMsgVO();
    // 默认msgType为text
    vo.setContent(answer);
    return vo;
  }
}
