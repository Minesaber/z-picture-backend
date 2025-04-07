package com.minesaber.zpicturebackend.model.vo.space.analyze;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 空间用户上传行为分析响应 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeResponse implements Serializable {
  /** 时间区间 */
  private String period;

  /** 上传数量 */
  private Long count;
}
