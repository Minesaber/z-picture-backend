package com.minesaber.zpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minesaber.zpicturebackend.annotation.AuthCheck;
import com.minesaber.zpicturebackend.common.BaseResponse;
import com.minesaber.zpicturebackend.common.DatabaseUtils;
import com.minesaber.zpicturebackend.common.DeleteRequest;
import com.minesaber.zpicturebackend.common.ResultUtils;
import com.minesaber.zpicturebackend.config.SystemConfig;
import com.minesaber.zpicturebackend.constant.UserConstant;
import com.minesaber.zpicturebackend.exception.ErrorCode;
import com.minesaber.zpicturebackend.exception.ThrowUtils;
import com.minesaber.zpicturebackend.model.dto.user.*;
import com.minesaber.zpicturebackend.model.entity.User;
import com.minesaber.zpicturebackend.model.vo.UserLoginVO;
import com.minesaber.zpicturebackend.model.vo.UserVO;
import com.minesaber.zpicturebackend.service.UserService;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
  @Resource private UserService userService;

  @Resource private SystemConfig systemConfig;

  /**
   * 用户注册
   *
   * @param userRegisterRequest 用户注册请求
   * @return 注册结果：id
   */
  @PostMapping("/register")
  public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
    // 2、注册
    String userAccount = userRegisterRequest.getUserAccount();
    String userPassword = userRegisterRequest.getUserPassword();
    String checkPassword = userRegisterRequest.getCheckPassword();
    Long id = userService.userRegister(userAccount, userPassword, checkPassword);
    return ResultUtils.success(id);
  }

  /**
   * 用户登录
   *
   * @param userLoginRequest 用户登录请求
   * @param request request
   * @return 登录结果：userLoginVO
   */
  @PostMapping("/login")
  public BaseResponse<UserLoginVO> userLogin(
      @RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
    // 1、检查参数
    ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
    // 2、登录
    String userAccount = userLoginRequest.getUserAccount();
    String userPassword = userLoginRequest.getUserPassword();
    UserLoginVO userLoginVO = userService.userLogin(userAccount, userPassword, request);
    return ResultUtils.success(userLoginVO);
  }

  /**
   * 获取当前用户
   *
   * @param request request
   * @return 当前用户
   */
  @GetMapping("/get/login")
  public BaseResponse<UserLoginVO> getLoginUser(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    UserLoginVO loginUserVO = userService.getLoginUserVO(loginUser);
    return ResultUtils.success(loginUserVO);
  }

  /**
   * 用户注销
   *
   * @param request request
   * @return void
   */
  @PostMapping("/logout")
  public BaseResponse<Void> userLogout(HttpServletRequest request) {
    userService.userLogout(request);
    return ResultUtils.success(null);
  }

  /**
   * 添加用户（管理员）
   *
   * @param userAddRequest 用户添加请求
   * @return 注册结果：id
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
    // 2、新账号使用初始密码
    String defaultUserPassword = systemConfig.getDefaultUserPassword();
    String salt = systemConfig.getSalt();
    String encryptPassword = DigestUtils.md5DigestAsHex((salt + defaultUserPassword).getBytes());
    // 3、添加
    User user = User.builder().build();
    BeanUtil.copyProperties(userAddRequest, user);
    user.setUserPassword(encryptPassword);
    boolean result = DatabaseUtils.executeWithLog(() -> userService.save(user));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "用户账号已存在");
    return ResultUtils.success(user.getId());
  }

  /**
   * 根据id删除用户（管理员）
   *
   * @param deleteRequest 删除请求
   * @return void
   */
  @PostMapping("/delete")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Void> deleteUserById(@RequestBody DeleteRequest deleteRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
    long id = deleteRequest.getId();
    ThrowUtils.throwIf(ObjUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR);
    // 2、删除
    DatabaseUtils.executeWithLog(() -> userService.removeById(id));
    return ResultUtils.success(null);
  }

  /**
   * 根据id更新用户（管理员）
   *
   * @param userUpdateRequest 删除用户请求
   * @return void
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Void> updateUserById(@RequestBody UserUpdateRequest userUpdateRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(
        userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
    // 2、更新
    User user = User.builder().build();
    BeanUtil.copyProperties(userUpdateRequest, user);
    boolean result = DatabaseUtils.executeWithLog(() -> userService.updateById(user));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(null);
  }

  /**
   * 分页查询用户（管理员）
   *
   * @param userQueryRequest 用户查询请求
   * @return 用户列表
   */
  @PostMapping("/list/page/vo")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Page<UserVO>> getUserVOByPage(
      @RequestBody UserQueryRequest userQueryRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
    // 2、获取参数
    long current = userQueryRequest.getCurrent();
    long pageSize = userQueryRequest.getPageSize();
    // 3、查询
    Page<User> userPage =
        userService.page(
            new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
    List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
    Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
    userVOPage.setRecords(userVOList);
    return ResultUtils.success(userVOPage);
  }

  /**
   * 根据id获取用户（管理员）
   *
   * @param id id
   * @return user
   */
  @GetMapping("/get")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<User> getUserById(Long id) {
    // 1、检查参数
    ThrowUtils.throwIf(ObjUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR);
    // 2、查询并返回
    User user = DatabaseUtils.executeWithLog(() -> userService.getById(id));
    ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
    return ResultUtils.success(user);
  }

  /**
   * 根据id获取用户视图
   *
   * @param id id
   * @return userVO
   */
  @GetMapping("/get/vo")
  public BaseResponse<UserVO> getUserVOById(Long id) {
    User user = getUserById(id).getData();
    UserVO userVO = DatabaseUtils.executeWithLog(() -> userService.getUserVO(user));
    return ResultUtils.success(userVO);
  }
}
