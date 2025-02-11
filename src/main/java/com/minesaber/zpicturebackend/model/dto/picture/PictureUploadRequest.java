package com.minesaber.zpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/** 图片上传请求 */
@Data
public class PictureUploadRequest implements Serializable {
  private static final long serialVersionUID = 8777545650252256499L;

  /** id */
  private Long id;
}
