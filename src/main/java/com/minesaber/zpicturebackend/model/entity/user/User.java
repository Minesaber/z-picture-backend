package com.minesaber.zpicturebackend.model.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/** 用户 */
@TableName(value = "user")
@Data
// todo 分析
public class User implements Serializable {
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

  /** 账号 */
  @NotBlank(message = "账号不能为空")
  @Size(min = 5, max = 20, message = "账号长度必须在5到20个字符之间")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号只能包含字母、数字和下划线")
  private String userAccount;

  /** 密码 */
  @NotBlank(message = "密码不能为空")
  @Size(min = 8, max = 20, message = "密码长度必须在8到20个字符之间")
  @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
          message = "密码必须包含大写字母、小写字母、数字和特殊字符")
  private String userPassword;

  /** 用户角色：user/admin */
  private String userRole;

  /** 用户昵称 */
  private String userName;

  /** 用户头像 */
  private String userAvatar;

  /** 用户简介 */
  private String userProfile;
}
