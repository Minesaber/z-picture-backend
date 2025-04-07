package com.minesaber.zpicturebackend.model.dto.space.analyze;

import java.io.Serializable;
import lombok.Data;

/** 空间使用排行分析请求（仅管理员） */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {
  /** 排名前 N 的空间 */
  private Integer topN = 10;
}
