package com.minesaber.zpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesaber.zpicturebackend.common.DatabaseUtils;
import com.minesaber.zpicturebackend.config.SystemConfig;
import com.minesaber.zpicturebackend.constant.UserConstant;
import com.minesaber.zpicturebackend.exception.ErrorCode;
import com.minesaber.zpicturebackend.exception.ThrowUtils;
import com.minesaber.zpicturebackend.mapper.UserMapper;
import com.minesaber.zpicturebackend.model.dto.user.UserQueryRequest;
import com.minesaber.zpicturebackend.model.entity.User;
import com.minesaber.zpicturebackend.model.enums.UserRole;
import com.minesaber.zpicturebackend.model.vo.UserLoginVO;
import com.minesaber.zpicturebackend.model.vo.UserVO;
import com.minesaber.zpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
  @Resource private SystemConfig systemConfig;

  @Override
  public long userRegister(String userAccount, String userPassword, String checkPassword) {
    // 1、检查参数
    ThrowUtils.throwIf(
        StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
    ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
    ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
    ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
    // todo 添加其他检查，如用户账号和密码长度限制，字符类型限制等
    // 2、检查账号是否已重复
    // todo 对于已经被删除的账号，不支持重新注册（其他方案也待完善）
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("userAccount", userAccount);
    Long count = DatabaseUtils.executeWithLog(() -> baseMapper.selectCount(queryWrapper));
    ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "用户账号已存在");
    // 3、加密
    String encryptPassword = getEncryptPassword(userPassword);
    // 4、初始化对象并存储
    User user =
        User.builder()
            .userAccount(userAccount)
            .userPassword(encryptPassword)
            .userRole(UserRole.USER.getValue())
            .userName(systemConfig.getDefaultUserName())
            .build();
    boolean saveResult = DatabaseUtils.executeWithLog(() -> save(user));
    ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败");
    return user.getId();
  }

  @Override
  public UserLoginVO userLogin(
      String userAccount, String userPassword, HttpServletRequest request) {
    // 1、检查参数
    ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
    ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号错误");
    ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");
    // 2、加密
    String encryptPassword = getEncryptPassword(userPassword);
    // 3、检查用户是否存在
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("userAccount", userAccount);
    queryWrapper.eq("userPassword", encryptPassword);
    User user = DatabaseUtils.executeWithLog(() -> baseMapper.selectOne(queryWrapper));
    if (user == null) log.info("用户账号不存在或密码错误");
    ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户账号不存在或密码错误");
    // 4、保存用户的登录状态
    HttpSession session = request.getSession();
    session.setAttribute(UserConstant.USER_LOGIN_STATE, user);
    return getLoginUserVO(user);
  }

  @Override
  public String getEncryptPassword(String password) {
    // 加盐后再加密
    final String salt = systemConfig.getSalt();
    return DigestUtils.md5DigestAsHex((salt + password).getBytes());
  }

  @Override
  public User getLoginUser(HttpServletRequest request) {
    ThrowUtils.throwIf(request == null, ErrorCode.OPERATION_ERROR, "获取用户登录视图失败：request为空");
    // 1、检查是否已登录
    User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    ThrowUtils.throwIf(
        currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
    // 2、与数据库同步
    Long id = currentUser.getId();
    currentUser = DatabaseUtils.executeWithLog(() -> getById(id));
    ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
    return currentUser;
  }

  @Override
  public UserLoginVO getLoginUserVO(User user) {
    if (user == null) return null;
    UserLoginVO userLoginVO = new UserLoginVO();
    BeanUtil.copyProperties(user, userLoginVO);
    return userLoginVO;
  }

  @Override
  public void userLogout(HttpServletRequest request) {
    // 1、检查是否已登录
    Object currentUser = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    ThrowUtils.throwIf(currentUser == null, ErrorCode.OPERATION_ERROR, "未登录");
    // 2、移除登录状态
    request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
  }

  @Override
  public UserVO getUserVO(User user) {
    if (user == null) return null;
    UserVO userVO = new UserVO();
    BeanUtil.copyProperties(user, userVO);
    return userVO;
  }

  @Override
  public List<UserVO> getUserVOList(List<User> userList) {
    if (userList == null) return new ArrayList<>();
    return userList.stream().map(this::getUserVO).collect(Collectors.toList());
  }

  @Override
  public QueryWrapper<User> getQueryWrapper(UserQueryRequest request) {
    // 1、检查参数
    ThrowUtils.throwIf(request == null, ErrorCode.SYSTEM_ERROR, "查询参数为空");
    // 2、获取查询参数
    String sortField = request.getSortField();
    String sortOrder = request.getSortOrder();
    Long id = request.getId();
    String userAccount = request.getUserAccount();
    String userRole = request.getUserRole();
    String userName = request.getUserName();
    String userProfile = request.getUserProfile();
    // 3、构建queryWrapper实例
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
    queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
    queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
    queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
    queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
    // tip：系统采用desc，所以isAsc参数传false
    queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
    return queryWrapper;
  }
}
