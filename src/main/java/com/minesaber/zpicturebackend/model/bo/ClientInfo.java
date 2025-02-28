package com.minesaber.zpicturebackend.model.bo;

import lombok.Data;

/** 客户端信息 */
@Data
public class ClientInfo {
  /** JSESSIONID */
  private String jSessionId;

  /** 客户端ip */
  private String clientIp;
}
