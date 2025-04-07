package com.minesaber.zpicturebackend.model.vo.space.analyze;

import java.io.Serializable;
import lombok.Data;

/** 空间资源使用分析响应类 */
@Data
public class SpaceUsageAnalyzeResponse implements Serializable {
  /** 空间总大小 */
  private Long maxSize;

  /** 已使用大小 */
  private Long usedSize;

  /** 已使用比例 */
  private Double sizeUsageRatio;

  /** 空间图片数量 */
  private Long maxCount;

  /** 已使用数量 */
  private Long usedCount;

  /** 已使用比例 */
  private Double countUsageRatio;
}
