package com.minesaber.zpicturebackend.model.dto.picture;

import com.minesaber.zpicturebackend.model.dto.base.PageRequest;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 图片查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
  private static final long serialVersionUID = -1587834842723489818L;

  /** id */
  private Long id;

  /** 名称 */
  private String name;

  /** 用户id */
  private Long userId;

  /** 简介 */
  private String profile;

  /** 分类 */
  private String category;

  /** 标签（JSON数组） */
  private List<String> tags;

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

  /** 搜索词（同时搜索名称、简介等） */
  private String searchText;
}
