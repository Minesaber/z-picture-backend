package com.minesaber.zpicturebackend.model.dto.space.analyze;

import java.io.Serializable;
import lombok.Data;

/** 通用空间分析请求 */
@Data
public class SpaceAnalyzeRequest implements Serializable {
  /** 空间 ID */
  private Long spaceId;

  /** 是否查询公共图库 */
  private boolean queryPublic;

  /** 是否全空间分析 */
  private boolean queryAll;
}
