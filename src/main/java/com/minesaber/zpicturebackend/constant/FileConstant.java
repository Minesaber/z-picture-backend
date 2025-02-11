package com.minesaber.zpicturebackend.constant;

import java.util.Arrays;
import java.util.List;

/** 文件常量 */
public interface FileConstant {
  /** 1MB的字节数 */
  Long MB = (long) (1024 * 1024);

  /** 允许上传的图片类型 */
  List<String> ALLOW_FORMAT_LIST = Arrays.asList("png", "jpg", "jpeg", "webp");

  /** 随机字符串长度 */
  Integer UUID_LENGTH = 16;

  /** 文件名最大长度 */
  Integer FILE_NAME_MAX_LENGTH = 128;

  /** 简介最大长度 */
  Integer PROFILE_MAX_LENGTH = 512;

  /** 分类最大长度 */
  Integer CATEGORY_MAX_LENGTH = 64;

  /** 标签最大长度 */
  Integer TAGS_MAX_LENGTH = 512;

  /** 单词请求最大记录条数 */
  Integer RECORDS_MAX_COUNT = 20;
}
