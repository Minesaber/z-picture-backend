package com.minesaber.zpicturebackend.model.dto.picture;

import com.minesaber.zpicturebackend.model.dto.base.PageRequest;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 图片查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
  private static final long serialVersionUID = 1L;

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

  /** 审核状态：0-待审核; 1-通过; 2-拒绝 */
  private Integer reviewStatus;

  /** 审核信息 */
  private String reviewMessage;

  /** 审核人 ID */
  private Long reviewerId;

  /** 审核时间 */
  private Date reviewTime;

  /** 空间 id */
  private Long spaceId;

  /** 是否只查询 spaceId 为 null 的数据 */
  private boolean nullSpaceId;

  /** 开始编辑时间 */
  private Date startEditTime;

  /** 结束编辑时间 */
  private Date endEditTime;
}
