package com.minesaber.zpicturebackend.model.dto.picture;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/** 图片编辑请求 */
@Data
public class PictureEditRequest implements Serializable {
  private static final long serialVersionUID = 1404114120396656874L;

  /** id */
  private Long id;

  /** 名称 */
  private String name;

  /** 简介 */
  private String profile;

  /** 分类 */
  private String category;

  /** 标签（JSON数组） */
  private List<String> tags;
}
