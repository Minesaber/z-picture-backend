package com.minesaber.zpicturebackend.model.vo.picture;

import lombok.Data;

import java.util.List;

/** 图片分类和标签视图 */
@Data
public class PictureCategoryTagVO {
  /** 分类列表 */
  private List<String> categoryList;

  /** 标签列表 */
  private List<String> tagList;
}
