package com.minesaber.zpicturebackend.model.vo.picture;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/** 图片分类和标签视图 */
@Data
public class PictureCategoryTagVO implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 分类列表 */
  private List<String> categoryList;

  /** 标签列表 */
  private List<String> tagList;
}
