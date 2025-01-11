package com.minesaber.zpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minesaber.zpicturebackend.model.dto.user.UserQueryRequest;
import com.minesaber.zpicturebackend.model.entity.User;
import com.minesaber.zpicturebackend.model.vo.UserLoginVO;
import com.minesaber.zpicturebackend.model.vo.UserVO;

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
  UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

  /**
   * 获取加密后的密码
   *
   * @param password 原始密码
   * @return 加密后的密码
   */
  String getEncryptPassword(String password);

  /**
   * 获取当前用户
   *
   * @param request request
   * @return 当前用户
   */
  User getLoginUser(HttpServletRequest request);

  /**
   * 获取已登录用户视图（脱敏）
   *
   * @param user 用户
   * @return 已登录用户视图
   */
  UserLoginVO getLoginUserVO(User user);

  /**
   * 用户注销
   *
   * @param request request
   */
  void userLogout(HttpServletRequest request);

  /**
   * 获取用户视图（脱敏）
   *
   * @param user 用户
   * @return 用户视图
   */
  UserVO getUserVO(User user);

  /**
   * 获取用户视图列表（脱敏）
   *
   * @param userList 用户列表
   * @return 用户视图列表
   */
  List<UserVO> getUserVOList(List<User> userList);

  /**
   * 获取QueryWrapper
   *
   * @param request request
   * @return queryWrapper
   */
  QueryWrapper<User> getQueryWrapper(UserQueryRequest request);
}
