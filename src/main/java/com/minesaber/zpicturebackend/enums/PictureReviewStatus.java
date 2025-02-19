package com.minesaber.zpicturebackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/** 图片审核状态 */
@Getter
public enum PictureReviewStatus {
  REVIEWING("待审核", 0),
  PASS("通过", 1),
  REJECT("拒绝", 2);

  private final String text;

  private final int value;

  PictureReviewStatus(String text, int value) {
    this.text = text;
    this.value = value;
  }

  /**
   * 根据 value 获取枚举
   *
   * @param value 枚举值的 value
   * @return 枚举值
   */
  public static PictureReviewStatus getEnumByValue(Integer value) {
    if (ObjUtil.isEmpty(value)) {
      return null;
    }
    for (PictureReviewStatus pictureReviewStatus : PictureReviewStatus.values()) {
      if (pictureReviewStatus.value == value) {
        return pictureReviewStatus;
      }
    }
    return null;
  }
}
