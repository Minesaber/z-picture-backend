package com.minesaber.zpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/** 图片上传请求 */
@Data
public class PictureUploadRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** id */
  private Long id;

  /** url */
  private String url;

  // todo 变量名称待规范
  /** 图片名称 */
  private String picName;

  /** 空间 id */
  private Long spaceId;
}
