package com.minesaber.zpicturebackend.model.dto.space;

import java.io.Serializable;
import lombok.Data;

/** 空间编辑请求 */
@Data
public class SpaceEditRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 空间 id */
  private Long id;

  /** 空间名称 */
  private String spaceName;
}
