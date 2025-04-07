package com.minesaber.zpicturebackend.model.vo.space.analyze;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 空间图片分类分析响应 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {
  /** 图片分类 */
  private String category;

  /** 图片数量 */
  private Long count;

  /** 图片总大小 */
  private Long totalSize;
}
