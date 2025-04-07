package com.minesaber.zpicturebackend.model.vo.space.analyze;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 空间图片大小分析响应 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {
  /** 图片大小范围 */
  private String sizeRange;

  /** 图片数量 */
  private Long count;
}
