package com.minesaber.zpicturebackend.model.dto.picture;

import java.io.Serializable;

import com.minesaber.zpicturebackend.api.ai.aliyun.model.CreateOutPaintingTaskRequest;
import lombok.Data;

/** 扩图请求 */
@Data
public class PictureOutPaintingRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 图片 id */
  private Long pictureId;

  /** 扩图参数 */
  private CreateOutPaintingTaskRequest.Parameters parameters;
}
