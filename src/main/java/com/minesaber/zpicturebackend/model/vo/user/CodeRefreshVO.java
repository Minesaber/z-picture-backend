package com.minesaber.zpicturebackend.model.vo.user;

import lombok.Data;

/** 标识码视图 */
@Data
public class CodeRefreshVO {
  /** 是否需要重连 */
  private boolean reconnect = false;

  /** 标识码 */
  private String cIdCode;
}
