package com.minesaber.zpicturebackend.enums;

import cn.hutool.core.util.ObjUtil;
import com.minesaber.zpicturebackend.constants.FileConstant;
import lombok.Getter;

/** 空间级别枚举 */
@Getter
public enum SpaceLevel {
  COMMON("普通版", 0, 200, 128 * FileConstant.MB),
  PROFESSIONAL("专业版", 1, 1000, 512 * FileConstant.MB),
  FLAGSHIP("旗舰版", 2, 2000, FileConstant.GB);

  private final String text;

  private final int value;

  private final long maxCount;

  private final long maxSize;

  /**
   * @param text 文本
   * @param value 值
   * @param maxSize 最大图片总大小
   * @param maxCount 最大图片总数量
   */
  SpaceLevel(String text, int value, long maxCount, long maxSize) {
    this.text = text;
    this.value = value;
    this.maxCount = maxCount;
    this.maxSize = maxSize;
  }

  /** 根据 value 获取枚举 */
  public static SpaceLevel getEnumByValue(Integer value) {
    if (ObjUtil.isEmpty(value)) {
      return null;
    }
    for (SpaceLevel spaceLevel : SpaceLevel.values()) {
      if (spaceLevel.value == value) {
        return spaceLevel;
      }
    }
    return null;
  }
}
