package com.minesaber.zpicturebackend.helpers;

import com.minesaber.zpicturebackend.model.dto.user.WxMsgRequest;
import com.minesaber.zpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class WxAckHelper {
  @Resource private WxLoginHelper wxLoginHelper;
  @Resource private UserService userService;

  public WxMsgRequest buildResponseBody(String fromUser, String eventType, String content) {
    return null;
  }
}
