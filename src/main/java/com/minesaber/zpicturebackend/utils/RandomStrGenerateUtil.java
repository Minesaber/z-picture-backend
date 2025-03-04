package com.minesaber.zpicturebackend.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RandomStrGenerateUtil {
  /**
   * 随机代码生成
   *
   * <p>包含至少一个数字和一个字母（小写）
   *
   * @return 随机码
   */
  public static String generateCode() {
    // 生成一位数字（0-9）和一位小写字母（a-z）
    String string =
        RandomUtil.randomInt(0, 10)
            + RandomUtil.randomString("abcdefghijklmnopqrstuvwxyz", 1)
            + RandomUtil.randomString("abcdefghijklmnopqrstuvwxyz0123456789", 3);
    // 洗牌后返回
    List<String> charList = CollUtil.newArrayList(string.split(""));
    Collections.shuffle(charList);
    return CollUtil.join(charList, "");
  }

  /**
   * 判断是否满足随机码规则
   *
   * @param code 待验证的随机码
   * @return 是否满足规则
   */
  public static boolean isValidCode(String code) {
    if (code.length() != 5) {
      return false;
    }
    code = code.toLowerCase();
    boolean hasDigit = false;
    boolean hasLowerCaseLetter = false;
    for (char c : code.toCharArray()) {
      if (Character.isDigit(c)) {
        hasDigit = true;
      } else if (Character.isLowerCase(c)) {
        hasLowerCaseLetter = true;
      }
    }
    return hasDigit && hasLowerCaseLetter && code.matches("[a-z0-9]+");
  }

  /**
   * 生成uuid
   *
   * @param length 限制长度，如果为null表示使用原始uuid
   * @return uuid
   */
  public static String generateUUID(Integer length) {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    if (length != null) {
      return uuid.substring(0, length);
    }
    return uuid;
  }
}
