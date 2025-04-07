package com.minesaber.zpicturebackend.model.vo.space.analyze;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 空间图片标签分析响应 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeResponse implements Serializable {
  /** 标签名称 */
  private String tag;

  /** 使用次数 */
  private Long count;
}
