package com.minesaber.zpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minesaber.zpicturebackend.model.dto.user.UserQueryRequest;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService extends IService<User> {
  /**
   * 用户注册
   *
   * @param userAccount 用户账号
   * @param userPassword 用户密码
   * @param checkPassword 确认密码
   * @return 新用户id
   */
  long userRegister(String userAccount, String userPassword, String checkPassword);

  /**
   * 用户登录
   *
   * @param userAccount 用户账号
   * @param userPassword 用户密码
   * @param request 登录请求
   * @return 已登录用户视图
   */
  UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

  /**
   * 用户注销
   *
   * @param request request
   */
  void userLogout(HttpServletRequest request);

  /**
   * 获取当前登录用户
   *
   * @param request request
   * @return 当前登录用户
   */
  User getLoginUser(HttpServletRequest request);

  /**
   * 判断用户是否为管理员
   *
   * @param user 用户
   * @return 判断结果
   */
  boolean isAdmin(User user);

  /**
   * 获取用户视图列表（脱敏）
   *
   * @param userList 用户列表
   * @return 用户视图列表
   */
  List<UserVO> convertToUserVOList(List<User> userList);

  /**
   * 获取QueryWrapper
   *
   * @param request request
   * @return queryWrapper
   */
  QueryWrapper<User> getQueryWrapper(UserQueryRequest request);
}
