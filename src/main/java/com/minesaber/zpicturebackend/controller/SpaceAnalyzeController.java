package com.minesaber.zpicturebackend.controller;

import com.minesaber.zpicturebackend.aop.annotation.AuthCheck;
import com.minesaber.zpicturebackend.constants.UserConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.model.dto.space.analyze.*;
import com.minesaber.zpicturebackend.model.entity.space.Space;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.model.vo.space.analyze.*;
import com.minesaber.zpicturebackend.service.SpaceAnalyzeService;
import com.minesaber.zpicturebackend.service.UserService;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.minesaber.zpicturebackend.utils.ResultUtils;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
  @Resource private UserService userService;

  @Resource private SpaceAnalyzeService spaceAnalyzeService;

  /**
   * 获取空间的使用状态
   *
   * @param spaceUsageAnalyzeRequest 空间资源使用分析响应类
   * @param request 空间资源使用分析请求封装类
   * @return Response
   */
  @PostMapping("/usage")
  @AuthCheck
  public Response<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
      @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    SpaceUsageAnalyzeResponse spaceUsageAnalyze =
        spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
    return ResultUtils.success(spaceUsageAnalyze);
  }

  /**
   * 获取空间图片分类分析
   *
   * @param spaceCategoryAnalyzeRequest 空间资源使用分析响应类
   * @param request 空间图片分类分析请求
   * @return Response
   */
  @PostMapping("/category")
  @AuthCheck
  public Response<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
      @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
      HttpServletRequest request) {
    ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyze =
        spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
    return ResultUtils.success(spaceCategoryAnalyze);
  }

  /**
   * 获取空间图片标签分析
   *
   * @param spaceTagAnalyzeRequest 空间图片标签分析请求
   * @param request 空间图片标签分析请求
   * @return Response
   */
  @PostMapping("/tag")
  @AuthCheck
  public Response<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
      @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    List<SpaceTagAnalyzeResponse> spaceTagAnalyze =
        spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
    return ResultUtils.success(spaceTagAnalyze);
  }

  /**
   * 获取空间图片大小分析
   *
   * @param spaceSizeAnalyzeRequest 空间图片大小分析响应
   * @param request 空间图片大小分析请求
   * @return Response
   */
  @PostMapping("/size")
  @AuthCheck
  public Response<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(
      @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    List<SpaceSizeAnalyzeResponse> resultList =
        spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
    return ResultUtils.success(resultList);
  }

  /**
   * 获取空间用户行为分析
   *
   * @param spaceUserAnalyzeRequest 空间用户行为分析响应
   * @param request 空间用户行为分析请求
   * @return Response
   */
  @PostMapping("/user")
  @AuthCheck
  public Response<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(
      @RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    List<SpaceUserAnalyzeResponse> resultList =
        spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
    return ResultUtils.success(resultList);
  }

  /**
   * 获取空间使用排行分析（仅管理员）
   *
   * @param spaceRankAnalyzeRequest 空间使用排行分析响应
   * @param request 空间使用排行分析请求
   * @return Response
   */
  @PostMapping("/rank")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<List<Space>> getSpaceRankAnalyze(
      @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    List<Space> resultList =
        spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
    return ResultUtils.success(resultList);
  }
}
