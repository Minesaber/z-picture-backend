package com.minesaber.zpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.minesaber.zpicturebackend.constants.FileConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.enums.SpaceLevel;
import com.minesaber.zpicturebackend.exception.BusinessException;
import com.minesaber.zpicturebackend.mapper.SpaceMapper;
import com.minesaber.zpicturebackend.model.dto.space.SpaceAddRequest;
import com.minesaber.zpicturebackend.model.dto.space.SpaceQueryRequest;
import com.minesaber.zpicturebackend.model.entity.space.Space;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.space.SpaceVO;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import com.minesaber.zpicturebackend.service.SpaceService;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

  @Resource private UserService userService;

  @Resource private TransactionTemplate transactionTemplate;

  private final Map<Long, Object> lockMap = new ConcurrentHashMap<>();

  @Override
  public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
    // 填充空参数的默认值后检查参数
    Space space = new Space();
    BeanUtils.copyProperties(spaceAddRequest, space);
    if (StrUtil.isBlank(space.getSpaceName())) {
      space.setSpaceName("默认空间");
    }
    if (space.getSpaceLevel() == null) {
      space.setSpaceLevel(SpaceLevel.COMMON.getValue());
    }
    fillSpaceBySpaceLevel(space);
    validSpace(space, true);
    // 非管理员只能创建普通级别的空间
    Long userId = loginUser.getId();
    space.setUserId(userId);
    if (SpaceLevel.COMMON != SpaceLevel.getEnumByValue(space.getSpaceLevel())
        && !userService.isAdmin(loginUser)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权创建指定级别的空间");
    }
    // 保存到数据库
    // todo 可以考虑拓展为分布式锁
    Object lock = lockMap.computeIfAbsent(userId, key -> new Object());
    synchronized (lock) {
      Long newSpaceId;
      try {
        newSpaceId =
            transactionTemplate.execute(
                status -> {
                  // 同一用户只能有一个私有空间
                  boolean exists =
                      DatabaseUtils.executeWithExceptionLogging(
                          () -> lambdaQuery().eq(Space::getUserId, userId).exists());
                  ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能有一个私有空间");
                  boolean result = DatabaseUtils.executeWithExceptionLogging(() -> save(space));
                  ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存空间到数据库失败");
                  return space.getId();
                });
      } finally {
        lockMap.remove(userId);
      }
      return Optional.ofNullable(newSpaceId).orElse(-1L);
    }
  }

  @Override
  public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
    QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
    if (spaceQueryRequest == null) {
      return queryWrapper;
    }
    // 从对象中取值
    Long id = spaceQueryRequest.getId();
    Long userId = spaceQueryRequest.getUserId();
    String spaceName = spaceQueryRequest.getSpaceName();
    Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
    String sortField = spaceQueryRequest.getSortField();
    String sortOrder = spaceQueryRequest.getSortOrder();
    // 拼接查询条件
    queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
    queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
    queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
    queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
    // 排序
    queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
    return queryWrapper;
  }

  @Override
  public SpaceVO convertToSpaceVO(Space space) {
    // 对象转封装类
    SpaceVO spaceVO = SpaceVO.convertToVO(space);
    // 关联查询用户信息
    // todo 重复代码可以抽取，下同
    Long userId = space.getUserId();
    if (userId != null && userId > 0) {
      User user = userService.getById(userId);
      UserVO userVO = UserVO.convertToUserVO(user);
      spaceVO.setUserVO(userVO);
    }
    return spaceVO;
  }

  @Override
  public List<SpaceVO> convertToSpaceVOList(List<Space> spaceList) {
    if (CollUtil.isEmpty(spaceList)) {
      return Collections.emptyList();
    }
    List<SpaceVO> spaceVOList =
        spaceList.stream().map(SpaceVO::convertToVO).collect(Collectors.toList());
    // 补充用户信息
    Map<Long, UserVO> userVOMap =
        DatabaseUtils.executeWithExceptionLogging(
                () ->
                    userService.listByIds(
                        spaceVOList.stream().map(SpaceVO::getId).collect(Collectors.toSet())))
            .stream()
            .map(UserVO::convertToUserVO)
            .collect(
                Collectors.toMap(
                    UserVO::getId, userVO -> userVO, (existing, replacement) -> existing));
    spaceVOList.forEach(spaceVO -> spaceVO.setUserVO(userVOMap.get(spaceVO.getId())));
    return spaceVOList;
  }

  @Override
  public void validSpace(Space space, boolean add) {
    ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
    // 从对象中取值
    String spaceName = space.getSpaceName();
    Integer spaceLevelValue = space.getSpaceLevel();
    SpaceLevel spaceLevel = SpaceLevel.getEnumByValue(spaceLevelValue);
    // 创建时校验
    if (add) {
      if (StrUtil.isBlank(spaceName)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
      }
      if (spaceLevel == null) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
      }
    }
    // 修改数据时，空间名称进行校验
    if (StrUtil.isNotBlank(spaceName) && spaceName.length() > FileConstant.SPACE_NAME_MAX_LENGTH) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
    }
    // 修改数据时，空间级别进行校验
    if (spaceLevelValue != null && spaceLevel == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
    }
  }

  @Override
  public void fillSpaceBySpaceLevel(Space space) {
    SpaceLevel spaceLevel = SpaceLevel.getEnumByValue(space.getSpaceLevel());
    if (spaceLevel != null) {
      long maxSize = spaceLevel.getMaxSize();
      if (space.getMaxSize() == null) {
        space.setMaxSize(maxSize);
      }
      long maxCount = spaceLevel.getMaxCount();
      if (space.getMaxCount() == null) {
        space.setMaxCount(maxCount);
      }
    }
  }
}
