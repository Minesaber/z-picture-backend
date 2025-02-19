package com.minesaber.zpicturebackend.constants;

import java.util.Arrays;
import java.util.List;

/** 文件常量 */
public interface FileConstant {

  /** 1MB的字节数 */
  Long MB = (long) (1024 * 1024);

  /** 1KB的字节数 */
  Long KB = (long) 1024;

  /** 最大大小（单位为MB） */
  Integer FILE_MAX_SIZE = 10;

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

  /** 单次导入图片最大数量 */
  Integer IMPORT_MAX_COUNT = 30;

  /** 默认的图片处理方式 */
  String STYLE_TYPE = "image/format,webp";

  /** 默认图片处理后类型 */
  String IMG_END_TYPE = "webp";

  /** 默认缩略图的压缩率 */
  String COMPRESS_RATIO = "image/quality,Q_50";

  /** 缩略图名称尾巴 */
  String COMPRESS_TAIL = "_compressed";

  /** 生成缩略图的阈值，大于这个值则生成缩略图 */
  Long USE_THUMBNAIL_SIZE = 50 * KB;
}
