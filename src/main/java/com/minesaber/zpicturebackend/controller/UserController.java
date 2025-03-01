package com.minesaber.zpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minesaber.zpicturebackend.aop.annotation.AuthCheck;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.model.dto.base.DeleteRequest;
import com.minesaber.zpicturebackend.utils.ResultUtils;
import com.minesaber.zpicturebackend.config.SystemConfig;
import com.minesaber.zpicturebackend.constants.UserConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.utils.SystemStatusUtil;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import com.minesaber.zpicturebackend.model.dto.user.*;
import com.minesaber.zpicturebackend.service.UserService;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
  // todo 补充同登录接口的检查规则
  @PostMapping("/register")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(SystemStatusUtil.isClosed, ErrorCode.FORBIDDEN_ERROR, "系统维护中，请稍后再试");
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
  public Response<UserVO> userLogin(
      @RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
    // 1、检查参数
    ThrowUtils.throwIf(SystemStatusUtil.isClosed, ErrorCode.FORBIDDEN_ERROR, "系统维护中，请稍后再试");
    ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
    // 2、登录
    String userAccount = userLoginRequest.getUserAccount();
    String userPassword = userLoginRequest.getUserPassword();
    UserVO userVO = userService.userLogin(userAccount, userPassword, request);
    return ResultUtils.success(userVO);
  }

  /**
   * 用户注销
   *
   * @param request request
   * @return void
   */
  @PostMapping("/logout")
  @AuthCheck
  public Response<Void> userLogout(HttpServletRequest request) {
    userService.userLogout(request);
    return ResultUtils.success(null);
  }

  /**
   * 获取当前用户
   *
   * @param request request
   * @return 当前用户
   */
  @GetMapping("/get/login")
  @AuthCheck
  public Response<UserVO> getLoginUser(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    UserVO userVO = UserVO.convertToUserVO(loginUser);
    return ResultUtils.success(userVO);
  }

  /**
   * 添加用户（管理员）
   *
   * @param userAddRequest 用户添加请求
   * @return 注册结果：id
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
    // 2、新账号使用初始密码
    String defaultUserPassword = systemConfig.getDefaultUserPassword();
    String salt = systemConfig.getSalt();
    String encryptPassword = DigestUtils.md5DigestAsHex((salt + defaultUserPassword).getBytes());
    // 3、添加
    User user = new User();
    BeanUtil.copyProperties(userAddRequest, user);
    user.setUserPassword(encryptPassword);
    boolean result = DatabaseUtils.executeWithExceptionLogging(() -> userService.save(user));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "用户账号已存在");
    return ResultUtils.success(user.getId());
  }

  /**
   * 根据id获取用户（管理员）
   *
   * @param id id
   * @return user
   */
  @GetMapping("/get")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<User> getUserById(Long id) {
    ThrowUtils.throwIf(ObjUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR);
    User user = DatabaseUtils.executeWithExceptionLogging(() -> userService.getById(id));
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
  @AuthCheck
  public Response<UserVO> getUserVOById(Long id) {
    User user = getUserById(id).getData();
    UserVO userVO = UserVO.convertToUserVO(user);
    return ResultUtils.success(userVO);
  }

  /**
   * 分页查询用户（管理员）
   *
   * @param userQueryRequest 用户查询请求
   * @return 用户列表（脱敏）
   */
  @PostMapping("/list/page/vo")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Page<UserVO>> getUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
    // 2、获取参数
    long current = userQueryRequest.getCurrent();
    long pageSize = userQueryRequest.getPageSize();
    // 3、查询
    Page<User> userPage =
        DatabaseUtils.executeWithExceptionLogging(
            () ->
                userService.page(
                    new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest)));
    long total = userPage.getTotal();
    List<User> records = userPage.getRecords();
    Page<UserVO> userVOPage = new Page<>(current, pageSize, total);
    List<UserVO> voRecords = userService.convertToUserVOList(records);
    userVOPage.setRecords(voRecords);
    return ResultUtils.success(userVOPage);
  }

  /**
   * 根据id更新用户（管理员）
   *
   * @param userUpdateRequest 删除用户请求
   * @return void
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Void> updateUserById(@RequestBody UserUpdateRequest userUpdateRequest) {
    // 1、检查参数
    ThrowUtils.throwIf(
        userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
    // 2、更新
    User user = new User();
    BeanUtil.copyProperties(userUpdateRequest, user);
    boolean result = DatabaseUtils.executeWithExceptionLogging(() -> userService.updateById(user));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(null);
  }

  /**
   * 根据id删除用户（管理员）
   *
   * @param deleteRequest 删除请求
   * @return void
   */
  // todo 不准删除自己
  @PostMapping("/delete")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Void> deleteUserById(
      @RequestBody DeleteRequest deleteRequest, HttpServletRequest servletRequest) {
    // 检查参数
    ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
    long id = deleteRequest.getId();
    ThrowUtils.throwIf(ObjUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR);
    // 只有超级管理员才可以删除账户
    ThrowUtils.throwIf(
        !userService.isSuperAdmin(userService.getLoginUser(servletRequest)),
        ErrorCode.NO_AUTH_ERROR,
        "暂只支持超管删除用户账号");
    // 删除
    DatabaseUtils.executeWithExceptionLogging(() -> userService.removeById(id));
    return ResultUtils.success(null);
  }
}
