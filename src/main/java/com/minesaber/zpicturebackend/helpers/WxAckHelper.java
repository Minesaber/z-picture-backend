package com.minesaber.zpicturebackend.helpers;

import com.minesaber.zpicturebackend.constants.SystemConstant;
import com.minesaber.zpicturebackend.model.dto.user.WxMsgRequest;
import com.minesaber.zpicturebackend.model.dto.user.WxTxtMsgRequest;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.entity.user.UserWx;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.service.UserWxService;
import com.minesaber.zpicturebackend.utils.RandomStrGenerateUtil;
import com.minesaber.zpicturebackend.utils.SystemStatusUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class WxAckHelper {
  @Resource private WxLoginHelper wxLoginHelper;
  @Resource private UserService userService;
  @Resource private UserWxService userWxService;
  @Resource private RedisTemplate<String, String> redisTemplate;

  public WxMsgRequest buildResponseBody(String fromUser, String eventType, String content) {
    String answer = "";
    if ("subscribe".equalsIgnoreCase(eventType)) {
      // 欢迎
      answer = "欢迎关注我们的云图库系统[相机]\n" + "在这里，您可以分享和管理您的图片，随时随地访问您的精彩瞬间！[星星]\n" + "如有疑问，请联系管理员[聊天]";
    } else if (RandomStrGenerateUtil.isValidCode(content)) {
      // 登录
      UserWx userWx = userWxService.getByOpenId(fromUser);
      Long userId = userWx.getUserId();
      User user = userService.getById(userId);
      UserVO userVO = UserVO.convertToUserVO(user);
      if (wxLoginHelper.login(content, userVO)) {
        answer = "登录成功！";
      } else {
        answer = "错误：请检查标识码，或尝试点击刷新重新获取";
      }
    } else if (content.startsWith("StopZPicture2025")) {
      SystemStatusUtil.closeAccess();
      answer = "已关闭访问";
    } else if (content.startsWith("RunZPicture2025")) {
      SystemStatusUtil.openAccess();
      answer = "已开放访问";
    } else {
      answer = "如有疑问，请联系管理员[聊天]";
    }
    WxTxtMsgRequest vo = new WxTxtMsgRequest();
    vo.setContent(answer);
    return vo;
  }
}
