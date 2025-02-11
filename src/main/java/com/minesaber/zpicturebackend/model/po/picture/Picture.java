package com.minesaber.zpicturebackend.model.po.picture;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** 图片 */
@TableName(value = "picture")
@Data
@Builder
public class Picture implements Serializable {
  private static final long serialVersionUID = -1760072755397802478L;

  /** id */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 创建时间 */
  private Date createTime;

  /** 编辑时间 */
  private Date editTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否已删除 */
  @TableLogic private Integer isDeleted;

  /** url */
  private String url;

  /** 名称 */
  private String name;

  /** 创建用户id */
  private Long userId;

  /** 简介 */
  private String profile;

  /** 分类 */
  private String category;

  /** 标签（JSON数组） */
  private String tags;

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
}
