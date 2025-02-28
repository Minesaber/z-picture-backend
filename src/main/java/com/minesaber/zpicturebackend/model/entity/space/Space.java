package com.minesaber.zpicturebackend.model.entity.space;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/** 空间 */
@TableName(value = "space")
@Data
//
public class Space implements Serializable {
  @TableField(exist = false)
  private static final long serialVersionUID = 1L;

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

  /** 空间名称 */
  private String spaceName;

  /** 创建用户 id */
  private Long userId;

  /** 空间级别：0-普通版 1-专业版 2-旗舰版 */
  private Integer spaceLevel;

  /** 空间图片的最大总大小 */
  private Long maxSize;

  /** 空间图片的最大数量 */
  private Long maxCount;

  /** 当前空间下图片的总大小 */
  private Long totalSize;

  /** 当前空间下的图片数量 */
  private Long totalCount;
}
