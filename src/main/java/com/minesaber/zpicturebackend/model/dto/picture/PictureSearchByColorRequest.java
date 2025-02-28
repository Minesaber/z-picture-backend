package com.minesaber.zpicturebackend.model.dto.picture;

import java.io.Serializable;
import lombok.Data;

/** 按照颜色搜索图片请求 */
@Data
public class PictureSearchByColorRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 图片主色调 */
  private String picColor;

  /** 空间 id */
  private Long spaceId;
}
