package com.minesaber.zpicturebackend.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Collections;
import java.util.List;

public class RandomStrGenerateUtil {
  /**
   * 随机代码生成
   *
   * <p>包含至少一个数字和一个字母（小写）
   *
   * @return 随机码
   */
  public static String generateCode() {
    StringBuilder sb = new StringBuilder();
    // 生成一位数字（0-9）和一位小写字母（a-z）
    sb.append(RandomUtil.randomInt(0, 10));
    sb.append(RandomUtil.randomString("abcdefghijklmnopqrstuvwxyz", 1));
    // 生成剩余3位，字符来自数字和小写字母的组合
    sb.append(RandomUtil.randomString("abcdefghijklmnopqrstuvwxyz0123456789", 3));
    // 洗牌后返回
    List<String> charList = CollUtil.newArrayList(sb.toString().split(""));
    Collections.shuffle(charList);
    return CollUtil.join(charList, "");
  }
}
