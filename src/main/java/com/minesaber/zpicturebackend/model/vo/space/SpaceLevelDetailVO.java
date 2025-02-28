package com.minesaber.zpicturebackend.model.vo.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 空间级别 */
@Data
@AllArgsConstructor
public class SpaceLevelDetailVO {

  /** 值 */
  private int value;

  /** 中文 */
  private String text;

  /** 最大数量 */
  private long maxCount;

  /** 最大容量 */
  private long maxSize;
}
