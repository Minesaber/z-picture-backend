package com.minesaber.zpicturebackend.model.vo.user;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

/** 微信公众号响应 */
@Data
@JacksonXmlRootElement(localName = "xml")
public class WxMsgVO {
  /** 消息发送方，对应发送消息的用户的 openId */
  @JacksonXmlProperty(localName = "FromUserName")
  private String fromUserName;

  /** 消息接收方，一般是公众账号的原始 id */
  @JacksonXmlProperty(localName = "ToUserName")
  private String toUserName;

  /** 消息创建的时间戳（通常是秒级时间戳） */
  @JacksonXmlProperty(localName = "CreateTime")
  private Long createTime;

  /**
   * 消息类型（默认为 text）
   *
   * <p>可能的取值有：text、image、voice、video、location、link、event
   */
  @JacksonXmlProperty(localName = "MsgType")
  private String msgType="text";
}
