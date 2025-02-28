package com.minesaber.zpicturebackend.model.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** 微信用户 */
@TableName(value = "user_wx")
@Data
public class UserWx implements Serializable {
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

  /** 微信用户在当前公众号的唯一标识 */
  private String openId;

  /** 是否仍在关注：0-未关注 1-关注 */
  private Integer isFollow;

  /** 是否绑定了账号：0-未绑定 1-绑定 */
  private Integer isBound;

  /** 绑定的用户id */
  private Long userId;

  /** 首次关注时间 */
  private Date firstSubscribeTime;

  /** 最新关注时间 */
  private Date latestSubscribeTime;
}
