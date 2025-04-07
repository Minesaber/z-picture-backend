package com.minesaber.zpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minesaber.zpicturebackend.model.dto.space.SpaceAddRequest;
import com.minesaber.zpicturebackend.model.dto.space.SpaceQueryRequest;
import com.minesaber.zpicturebackend.model.entity.space.Space;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.space.SpaceVO;
import java.util.List;

public interface SpaceService extends IService<Space> {
  /**
   * 创建空间
   *
   * @param spaceAddRequest 创建空间请求
   * @param loginUser 当前登录用户
   * @return 新空间 id
   */
  long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

  /**
   * 获取查询对象
   *
   * @param spaceQueryRequest 空间查询请求
   * @return queryWrapper
   */
  QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

  /**
   * 获取空间包装类
   *
   * @param space 待封装空间
   * @return 空间包装类
   */
  SpaceVO convertToSpaceVO(Space space);

  /**
   * 获取空间包装类（多条）
   *
   * @param spaceList 待封装空间列表
   * @return 空间包装类列表
   */
  List<SpaceVO> convertToSpaceVOList(List<Space> spaceList);

  /**
   * 校验空间
   *
   * @param space 待检查空间
   * @param add 是否为创建时检验
   */
  void validSpace(Space space, boolean add);

  /**
   * 根据空间级别补充参数
   *
   * @param space 空间对象
   */
  void fillSpaceBySpaceLevel(Space space);

  /**
   * 检查用户是否有指定空间权限
   *
   * @param loginUser 当前登录用户
   * @param space 待检查空间
   */
  void checkSpaceAuth(User loginUser, Space space);
}
