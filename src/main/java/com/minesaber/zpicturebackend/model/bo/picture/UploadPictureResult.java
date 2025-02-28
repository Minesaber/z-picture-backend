package com.minesaber.zpicturebackend.model.bo.picture;

import lombok.Builder;
import lombok.Data;

/** 上传图片结果 */
@Data
@Builder
public class UploadPictureResult {
  /** url */
  private String url;

  /** 缩略图 url */
  private String thumbnailUrl;

  /** 名称 */
  private String name;

  /** 大小 */
  private Long picSize;

  /** 格式 */
  private String picFormat;

  /** 宽度 */
  private Integer picWidth;

  /** 高度 */
  private Integer picHeight;

  /** 宽高比 */
  private Double picScale;

  /** 图片主色调 */
  private String picColor;
}
