package com.minesaber.zpicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesaber.zpicturebackend.constants.FileConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.exception.BusinessException;
import com.minesaber.zpicturebackend.mapper.SpaceMapper;
import com.minesaber.zpicturebackend.model.dto.space.analyze.*;
import com.minesaber.zpicturebackend.model.entity.picture.Picture;
import com.minesaber.zpicturebackend.model.entity.space.Space;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.space.analyze.*;
import com.minesaber.zpicturebackend.service.PictureService;
import com.minesaber.zpicturebackend.service.SpaceAnalyzeService;
import com.minesaber.zpicturebackend.service.SpaceService;
import com.minesaber.zpicturebackend.service.UserService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.minesaber.zpicturebackend.utils.ThrowUtils;
import org.springframework.stereotype.Service;

@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceAnalyzeService {
  @Resource private UserService userService;

  @Resource private SpaceService spaceService;

  @Resource private PictureService pictureService;

  @Override
  public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(
      SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
    checkRequestParam(spaceUsageAnalyzeRequest);
    // 场景1：全空间或公共图库（Picture表）
    if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
      checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
      QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
      queryWrapper.select("picSize");
      fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
      List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
      long usedSize = pictureObjList.stream().mapToLong(obj -> (Long) obj).sum();
      long usedCount = pictureObjList.size();
      SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
      // 空间总大小、已使用大小、已使用比例
      spaceUsageAnalyzeResponse.setMaxSize(null);
      spaceUsageAnalyzeResponse.setUsedSize(usedSize);
      spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
      // 空间图片数量、已使用数量、已使用比例
      spaceUsageAnalyzeResponse.setMaxCount(null);
      spaceUsageAnalyzeResponse.setUsedCount(usedCount);
      spaceUsageAnalyzeResponse.setCountUsageRatio(null);
      return spaceUsageAnalyzeResponse;
    } else {
      // 场景2：指定空间（Space表）
      Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
      ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
      Space space = spaceService.getById(spaceId);
      ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
      checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
      SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
      // 空间总大小、已使用大小、已使用比例
      spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
      spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
      double sizeUsageRatio =
          NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
      spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
      // 空间图片数量、已使用数量、已使用比例
      spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
      spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
      double countUsageRatio =
          NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
      spaceUsageAnalyzeResponse.setCountUsageRatio(countUsageRatio);
      return spaceUsageAnalyzeResponse;
    }
  }

  @Override
  public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(
      SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
    checkRequestParam(spaceCategoryAnalyzeRequest);
    checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
    queryWrapper
        .select("category", "count(*) as count", "sum(picSize) as totalSize")
        .groupBy("category");
    return pictureService.getBaseMapper().selectMaps(queryWrapper).stream()
        .map(
            result -> {
              String category = (String) result.get("category");
              if (category == null) {
                category = "未分类";
              }
              Long count = ((Number) result.get("count")).longValue();
              Long totalSize = ((Number) result.get("totalSize")).longValue();
              return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(
      SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
    checkRequestParam(spaceTagAnalyzeRequest);
    checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
    queryWrapper.select("tags");
    List<String> tagsJsonList =
        pictureService.getBaseMapper().selectObjs(queryWrapper).stream()
            .filter(ObjUtil::isNotNull)
            .map(Object::toString)
            .toList();
    Map<String, Long> tagCountMap =
        tagsJsonList.stream()
            .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
            .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
    return tagCountMap.entrySet().stream()
        .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
        .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(
      SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
    checkRequestParam(spaceSizeAnalyzeRequest);
    checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
    queryWrapper.select("picSize");
    List<Long> picSizeList =
        pictureService.getBaseMapper().selectObjs(queryWrapper).stream()
            .map(size -> (Long) size)
            .toList();
    // 图片大小情况：<100KB、100KB-500KB、500KB-1MB、>1MB
    Map<String, Long> sizeRanges = new LinkedHashMap<>();
    sizeRanges.put(
        "<100KB", picSizeList.stream().filter(size -> size < 100 * FileConstant.KB).count());
    sizeRanges.put(
        "100KB-500KB",
        picSizeList.stream()
            .filter(size -> size >= 100 * FileConstant.KB && size < 500 * FileConstant.KB)
            .count());
    sizeRanges.put(
        "500KB-1MB",
        picSizeList.stream()
            .filter(size -> size >= 500 * FileConstant.KB && size < FileConstant.MB)
            .count());
    sizeRanges.put(">1MB", picSizeList.stream().filter(size -> size >= FileConstant.MB).count());
    return sizeRanges.entrySet().stream()
        .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(
      SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
    checkRequestParam(spaceUserAnalyzeRequest);
    checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
    Long userId = spaceUserAnalyzeRequest.getUserId();
    queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
    // 补充分析维度：每日、每周、每月
    String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
    switch (timeDimension) {
      case "day":
        queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') as period", "count(*) as count");
        break;
      case "week":
        queryWrapper.select("YEARWEEK(createTime) as period", "count(*) as count");
        break;
      case "month":
        queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') as period", "count(*) as count");
        break;
      default:
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
    }
    queryWrapper.groupBy("period").orderByAsc("period");
    List<Map<String, Object>> queryResult = pictureService.getBaseMapper().selectMaps(queryWrapper);
    return queryResult.stream()
        .map(
            result -> {
              String period = result.get("period").toString();
              Long count = ((Number) result.get("count")).longValue();
              return new SpaceUserAnalyzeResponse(period, count);
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<Space> getSpaceRankAnalyze(
      SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
    ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
    queryWrapper
        .select("id", "spaceName", "userId", "totalSize")
        .orderByDesc("totalSize")
        .last("limit " + spaceRankAnalyzeRequest.getTopN());
    return spaceService.list(queryWrapper);
  }

  /**
   * 校验空间分析权限
   *
   * @param spaceAnalyzeRequest 通用空间分析请求
   * @param loginUser 登录用户
   */
  private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
    boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
    boolean queryAll = spaceAnalyzeRequest.isQueryAll();
    if (queryAll || queryPublic) {
      ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
    } else {
      Long spaceId = spaceAnalyzeRequest.getSpaceId();
      ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
      Space space = spaceService.getById(spaceId);
      ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
      spaceService.checkSpaceAuth(loginUser, space);
    }
  }

  /**
   * 根据请求对象封装查询条件
   *
   * @param spaceAnalyzeRequest 通用空间分析请求
   * @param queryWrapper 查询条件
   */
  private void fillAnalyzeQueryWrapper(
      SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
    if (spaceAnalyzeRequest.isQueryAll()) return;
    boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
    if (queryPublic) {
      queryWrapper.isNull("spaceId");
      return;
    }
    Long spaceId = spaceAnalyzeRequest.getSpaceId();
    if (spaceId != null) {
      queryWrapper.eq("spaceId", spaceId);
      return;
    }
    throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
  }

  private void checkRequestParam(SpaceAnalyzeRequest spaceAnalyzeRequest) {
    ThrowUtils.throwIf(spaceAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    Long spaceId = spaceAnalyzeRequest.getSpaceId();
    boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
    boolean queryAll = spaceAnalyzeRequest.isQueryAll();
    ThrowUtils.throwIf(
        (spaceId == null && !queryPublic && !queryAll), ErrorCode.PARAMS_ERROR, "未指定查询范围");
  }
}
