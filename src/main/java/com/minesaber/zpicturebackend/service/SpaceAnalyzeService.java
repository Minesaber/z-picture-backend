package com.minesaber.zpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minesaber.zpicturebackend.model.dto.space.analyze.*;
import com.minesaber.zpicturebackend.model.entity.space.Space;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.space.analyze.*;
import java.util.List;

public interface SpaceAnalyzeService extends IService<Space> {
  /**
   * 获取空间使用情况分析
   *
   * @param spaceUsageAnalyzeRequest 空间资源使用分析请求
   * @param loginUser 登录用户
   * @return 空间使用情况分析响应
   */
  SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(
      SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

  /**
   * 获取空间图片分类分析
   *
   * @param spaceCategoryAnalyzeRequest 空间图片分类分析请求
   * @param loginUser 登录用户
   * @return 空间图片分类分析响应
   */
  List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(
      SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

  /**
   * 获取空间图片标签分析
   *
   * @param spaceTagAnalyzeRequest 空间图片标签分析请求
   * @param loginUser 登录用户
   * @return 空间图片标签分析响应
   */
  List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(
      SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

  /**
   * 获取空间图片大小分析
   *
   * @param spaceSizeAnalyzeRequest 空间图片大小分析请求
   * @param loginUser 登录用户
   * @return 空间图片大小分析响应
   */
  List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(
      SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

  /**
   * 获取空间用户上传行为分析
   *
   * @param spaceUserAnalyzeRequest 空间用户上传行为分析请求
   * @param loginUser 登录用户
   * @return 空间用户上传行为分析响应
   */
  List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(
      SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

  /**
   * 空间使用排行分析（管理员）
   *
   * @param spaceRankAnalyzeRequest 空间使用排行分析请求
   * @param loginUser 登录用户
   * @return 空间使用排行分析响应
   */
  List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
