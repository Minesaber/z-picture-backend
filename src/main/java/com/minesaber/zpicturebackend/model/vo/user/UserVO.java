package com.minesaber.zpicturebackend.model.vo.user;

import cn.hutool.core.bean.BeanUtil;
import com.minesaber.zpicturebackend.model.po.user.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** 用户视图（脱敏） */
@Data
public class UserVO implements Serializable {
  private static final long serialVersionUID = 1570036958556488294L;

  /** id */
  private Long id;

  /** 创建时间 */
  private Date createTime;

  /** 编辑时间 */
  private Date editTime;

  /** 更新时间 */
  private Date updateTime;

  /** 账号 */
  private String userAccount;

  /** 用户角色：user/admin */
  private String userRole;

  /** 用户昵称 */
  private String userName;

  /** 用户头像 */
  private String userAvatar;

  /** 用户简介 */
  private String userProfile;

  /**
   * 获取用户视图（脱敏）
   *
   * @param user 用户
   * @return 用户视图
   */
  public static UserVO convertToUserVO(User user) {
    if (user == null) return null;
    UserVO userVO = new UserVO();
    BeanUtil.copyProperties(user, userVO);
    return userVO;
  }
}
