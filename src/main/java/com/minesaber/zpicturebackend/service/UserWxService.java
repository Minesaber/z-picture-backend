package com.minesaber.zpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minesaber.zpicturebackend.model.entity.user.UserWx;

public interface UserWxService extends IService<UserWx> {
  /**
   * 根据openId查找微信用户
   *
   * @param openId 微信用户唯一标识
   * @return 微信用户
   */
  UserWx getByOpenId(String openId);
}
