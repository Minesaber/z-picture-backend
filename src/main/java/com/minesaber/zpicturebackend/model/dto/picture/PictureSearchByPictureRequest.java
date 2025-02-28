package com.minesaber.zpicturebackend.model.dto.picture;

import java.io.Serializable;
import lombok.Data;

/** 以图搜图请求 */
@Data
public class PictureSearchByPictureRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 图片 id */
  private Long pictureId;
}
