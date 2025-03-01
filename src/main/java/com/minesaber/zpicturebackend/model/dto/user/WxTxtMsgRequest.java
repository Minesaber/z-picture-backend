package com.minesaber.zpicturebackend.model.dto.user;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文本消息 */
@Data
@EqualsAndHashCode(callSuper = true)
@JacksonXmlRootElement(localName = "xml")
public class WxTxtMsgRequest extends WxMsgRequest {
  /**
   * 描述具体的事件类型（MsgType为event时，此字段才会出现）
   *
   * <p>可能的取值有：subscribe、unsubscribe、SCAN、LOCATION、CLICK
   */
  @JacksonXmlProperty(localName = "Event")
  private String event;

  /** 文本消息的内容（MsgType不为text，此字段也可能会出现） */
  @JacksonXmlProperty(localName = "Content")
  private String content;

  /** 事件的key */
  @JacksonXmlProperty(localName = "EventKey")
  private String eventKey;

  /** 二维码的ticket，可用来换取二维码图片 */
  @JacksonXmlProperty(localName = "Ticket")
  private String ticket;

  /** 消息id（标识非事件消息） */
  @JacksonXmlProperty(localName = "MsgId")
  private String msgId;

  /** 消息数据id，用于多图文时，标识第几条消息 */
  @JacksonXmlProperty(localName = "MsgDataId")
  private String msgDataId;

  /** 消息数据序号，用于多图文时，标识第几条消息 */
  @JacksonXmlProperty(localName = "Idx")
  private String idx;
}
