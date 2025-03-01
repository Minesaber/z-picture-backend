package com.minesaber.zpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesaber.zpicturebackend.mapper.UserWxMapper;
import com.minesaber.zpicturebackend.model.entity.user.UserWx;
import com.minesaber.zpicturebackend.service.UserWxService;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import org.springframework.stereotype.Service;

@Service
public class UserWxServiceImpl extends ServiceImpl<UserWxMapper, UserWx> implements UserWxService {
  @Override
  public UserWx getByOpenId(String openId) {
    return DatabaseUtils.executeWithExceptionLogging(
        () -> baseMapper.selectOne(new QueryWrapper<UserWx>().eq("openId", openId)));
  }
}
