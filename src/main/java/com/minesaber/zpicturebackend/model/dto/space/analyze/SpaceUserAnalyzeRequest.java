package com.minesaber.zpicturebackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 空间用户上传行为分析请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {
  /** 用户 ID（可选） */
  private Long userId;

  /** 时间维度：day / week / month */
  private String timeDimension;
}
