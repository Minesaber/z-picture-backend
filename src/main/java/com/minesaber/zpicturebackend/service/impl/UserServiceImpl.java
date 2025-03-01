package com.minesaber.zpicturebackend.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesaber.zpicturebackend.enums.UserRole;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.config.SystemConfig;
import com.minesaber.zpicturebackend.constants.UserConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import com.minesaber.zpicturebackend.mapper.UserMapper;
import com.minesaber.zpicturebackend.model.dto.user.UserQueryRequest;
import com.minesaber.zpicturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
  @Resource private SystemConfig systemConfig;
  private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  private static final Validator validator = factory.getValidator();

  @Override
  public long userRegister(String userAccount, String userPassword, String checkPassword) {
    checkRegisterParams(userAccount, userPassword, checkPassword);
    return register(userAccount, userPassword);
  }

  private static void checkRegisterParams(
      String userAccount, String userPassword, String checkPassword) {
    // 1、检查参数
    ThrowUtils.throwIf(
        StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
    ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
    // todo 前端和后端的校验规则待同步
    ThrowUtils.throwIf(
        userAccount.length() < UserConstant.USER_ACCOUNT_MIN_LENGTH,
        ErrorCode.PARAMS_ERROR,
        "用户账号过短");
    ThrowUtils.throwIf(
        userAccount.length() > UserConstant.USER_ACCOUNT_MAX_LENGTH,
        ErrorCode.PARAMS_ERROR,
        "用户账号过长");
    ThrowUtils.throwIf(
        userPassword.length() < UserConstant.USER_PASSWORD_MIN_LENGTH,
        ErrorCode.PARAMS_ERROR,
        "用户密码过短");
    ThrowUtils.throwIf(
        userPassword.length() > UserConstant.USER_PASSWORD_MAX_LENGTH,
        ErrorCode.PARAMS_ERROR,
        "用户密码过长");
  }

  @Override
  public long register(String userAccount, String userPassword) {
    // todo 系统输入检查待规范
    // 2、检查账号是否已重复
    // todo 对于已经被删除的账号，不支持重新注册
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("userAccount", userAccount);
    Long count =
        DatabaseUtils.executeWithExceptionLogging(() -> baseMapper.selectCount(queryWrapper));
    ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "用户账号已存在");
    // 3、加密
    String encryptPassword = getEncryptPassword(userPassword);
    // 4、初始化对象并存储
    User user = new User();
    user.setUserAccount(userAccount);
    user.setUserPassword(encryptPassword);
    user.setUserRole(UserConstant.DEFAULT_ROLE);
    user.setUserName(systemConfig.getDefaultUserName());
    boolean saveResult = DatabaseUtils.executeWithExceptionLogging(() -> save(user));
    ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败");
    return user.getId();
  }

  @Override
  public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
    User user = new User();
    user.setUserAccount(userAccount);
    user.setUserPassword(userPassword);
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    // todo 之后可考虑隐藏详细信息
    StringBuilder sb = null;
    if (!violations.isEmpty()) {
      sb = new StringBuilder();
      for (ConstraintViolation<User> violation : violations) {
        sb.append(violation.getMessage()).append("\n");
      }
    }
    ThrowUtils.throwIf(!violations.isEmpty(), ErrorCode.PARAMS_ERROR, String.valueOf(sb));
    // todo 已经登录用户直接跳转首页
    // todo 对于已删除用户的处理
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
    User oldUser =
        DatabaseUtils.executeWithExceptionLogging(() -> baseMapper.selectOne(queryWrapper));
    ThrowUtils.throwIf(oldUser == null, ErrorCode.PARAMS_ERROR, "用户账号不存在或密码错误");
    // 4、保存用户的登录状态
    HttpSession session = request.getSession();
    session.setAttribute(UserConstant.LOGIN_USER_STATE, oldUser);
    return UserVO.convertToUserVO(oldUser);
  }

  /**
   * 获取加密后的密码
   *
   * @param password 原始密码
   * @return 加密后的密码
   */
  private String getEncryptPassword(String password) {
    // 加盐后再加密
    final String salt = systemConfig.getSalt();
    return DigestUtils.md5DigestAsHex((salt + password).getBytes());
  }

  @Override
  public void userLogout(HttpServletRequest request) {
    ThrowUtils.throwIf(request == null, ErrorCode.SYSTEM_ERROR);
    // 移除登录状态
    request.getSession().removeAttribute(UserConstant.LOGIN_USER_STATE);
  }

  @Override
  public User getLoginUser(HttpServletRequest request) {
    ThrowUtils.throwIf(request == null, ErrorCode.SYSTEM_ERROR);
    User currentUser = (User) request.getSession().getAttribute(UserConstant.LOGIN_USER_STATE);
    // 与数据库同步
    Long id = currentUser.getId();
    currentUser = DatabaseUtils.executeWithExceptionLogging(() -> getById(id));
    ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
    return currentUser;
  }

  @Override
  public boolean isAdmin(User user) {
    return user != null && UserRole.ADMIN.getValue().equals(user.getUserRole());
  }

  @Override
  public boolean isSuperAdmin(User user) {
    return user.getId() == 0;
  }

  @Override
  public List<UserVO> convertToUserVOList(List<User> userList) {
    if (userList == null) return new ArrayList<>();
    return userList.stream().map(UserVO::convertToUserVO).collect(Collectors.toList());
  }

  @Override
  public QueryWrapper<User> getQueryWrapper(UserQueryRequest request) {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    // 1、检查参数
    if (request == null) {
      return queryWrapper;
    }
    // 2、获取查询参数
    Long id = request.getId();
    String userAccount = request.getUserAccount();
    String userRole = request.getUserRole();
    String userName = request.getUserName();
    String userProfile = request.getUserProfile();
    // 3、构建queryWrapper实例
    queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
    queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
    queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
    queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
    queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
    // tips：系统采用desc，所以isAsc参数传false
    String sortField = request.getSortField();
    String sortOrder = request.getSortOrder();
    queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
    return queryWrapper;
  }
}
