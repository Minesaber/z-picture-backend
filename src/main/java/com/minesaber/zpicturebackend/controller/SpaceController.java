package com.minesaber.zpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minesaber.zpicturebackend.aop.annotation.AuthCheck;
import com.minesaber.zpicturebackend.constants.UserConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.enums.SpaceLevel;
import com.minesaber.zpicturebackend.model.dto.base.DeleteRequest;
import com.minesaber.zpicturebackend.model.dto.space.SpaceAddRequest;
import com.minesaber.zpicturebackend.model.dto.space.SpaceEditRequest;
import com.minesaber.zpicturebackend.model.dto.space.SpaceQueryRequest;
import com.minesaber.zpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.minesaber.zpicturebackend.model.entity.space.Space;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.model.vo.space.SpaceLevelDetailVO;
import com.minesaber.zpicturebackend.model.vo.space.SpaceVO;
import com.minesaber.zpicturebackend.service.SpaceService;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.utils.ResultUtils;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

  @Resource private UserService userService;

  @Resource private SpaceService spaceService;

  /**
   * 添加空间
   *
   * @param spaceAddRequest 空间添加请求
   * @param request request
   * @return 新空间 id
   */
  @PostMapping("/add")
  @AuthCheck
  public Response<Long> addSpace(
      @RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    long newId = spaceService.addSpace(spaceAddRequest, loginUser);
    return ResultUtils.success(newId);
  }

  /**
   * 根据 id 获取空间（管理员）
   *
   * @param id 空间 id
   * @return 空间
   */
  @GetMapping("/get")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Space> getSpaceById(long id) {
    ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
    Space space = DatabaseUtils.executeWithExceptionLogging(() -> spaceService.getById(id));
    ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
    return ResultUtils.success(space);
  }

  /**
   * 根据 id 获取空间
   *
   * @param id 空间 id
   * @return 空间视图
   */
  // todo 用户可以通过id查看到其他用户的空间等级等信息
  @GetMapping("/get/vo")
  @AuthCheck
  public Response<SpaceVO> getSpaceVOById(long id) {
    Response<Space> spaceResponse = getSpaceById(id);
    Space space = spaceResponse.getData();
    return ResultUtils.success(spaceService.convertToSpaceVO(space));
  }

  /**
   * 分页获取空间列表（管理员）
   *
   * @param spaceQueryRequest 空间查询请求
   * @return 分页空间列表
   */
  @PostMapping("/list/page")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
    long current = spaceQueryRequest.getCurrent();
    long size = spaceQueryRequest.getPageSize();
    Page<Space> spacePage =
        DatabaseUtils.executeWithExceptionLogging(
            () ->
                spaceService.page(
                    new Page<>(current, size), spaceService.getQueryWrapper(spaceQueryRequest)));
    return ResultUtils.success(spacePage);
  }

  /**
   * 分页获取空间列表
   *
   * @param spaceQueryRequest 空间查询请求
   * @return 分页空间视图列表（脱敏）
   */
  @PostMapping("/list/page/vo")
  @AuthCheck
  public Response<Page<SpaceVO>> listSpaceVOByPage(
      @RequestBody SpaceQueryRequest spaceQueryRequest) {
    long current = spaceQueryRequest.getCurrent();
    long size = spaceQueryRequest.getPageSize();
    // 限制爬虫
    ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
    // 查询数据库
    Response<Page<Space>> pageResponse = listSpaceByPage(spaceQueryRequest);
    Page<Space> spacePage = pageResponse.getData();
    List<Space> records = spacePage.getRecords();
    long total = spacePage.getTotal();
    Page<SpaceVO> spaceVOPage = new Page<>(current, size, total);
    List<SpaceVO> spaceVORecords = spaceService.convertToSpaceVOList(records);
    spaceVOPage.setRecords(spaceVORecords);
    return ResultUtils.success(spaceVOPage);
  }

  /**
   * 获取空间级别列表
   *
   * @return 空间级别详情列表
   */
  @GetMapping("/list/level")
  @AuthCheck
  public Response<List<SpaceLevelDetailVO>> listSpaceLevel() {
    List<SpaceLevelDetailVO> spaceLevelList =
        Arrays.stream(SpaceLevel.values())
            .map(
                spaceLevelEnum ->
                    new SpaceLevelDetailVO(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
            .collect(Collectors.toList());
    return ResultUtils.success(spaceLevelList);
  }

  /**
   * 编辑空间
   *
   * @param spaceEditRequest 空间编辑请求
   * @param request request
   * @return 编辑结果
   */
  @PostMapping("/edit")
  @AuthCheck
  public Response<Boolean> editSpace(
      @RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(
        spaceEditRequest == null || spaceEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
    // 填充参数，补充编辑时间
    Space space = new Space();
    BeanUtils.copyProperties(spaceEditRequest, space);
    spaceService.fillSpaceBySpaceLevel(space);
    space.setEditTime(new Date());
    // 检查参数
    spaceService.validSpace(space, false);
    User loginUser = userService.getLoginUser(request);
    // 判断是否存在，仅本人或管理员可编辑
    long id = spaceEditRequest.getId();
    Space oldSpace = DatabaseUtils.executeWithExceptionLogging(() -> spaceService.getById(id));
    ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
    spaceService.checkSpaceAuth(loginUser, oldSpace);
    // 操作数据库
    boolean result =
        DatabaseUtils.executeWithExceptionLogging(() -> spaceService.updateById(space));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }

  /**
   * 更新空间（管理员）
   *
   * @param spaceUpdateRequest 空间更新请求
   * @return 响应结果
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
    // 信息检查
    ThrowUtils.throwIf(
        spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
    Space space = new Space();
    BeanUtils.copyProperties(spaceUpdateRequest, space);
    spaceService.fillSpaceBySpaceLevel(space);
    spaceService.validSpace(space, false);
    // 判断是否存在
    long id = spaceUpdateRequest.getId();
    Space oldSpace = DatabaseUtils.executeWithExceptionLogging(() -> spaceService.getById(id));
    ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
    // 操作数据库
    boolean result =
        DatabaseUtils.executeWithExceptionLogging(() -> spaceService.updateById(space));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }

  /**
   * 删除空间（管理员、用户）
   *
   * @param deleteRequest 删除空间请求
   * @param request request
   * @return 响应结果
   */
  @PostMapping("/delete")
  @AuthCheck
  public Response<Boolean> deleteSpace(
      @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(
        deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
        ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    Long id = deleteRequest.getId();
    // 判断是否存在
    Space oldSpace = DatabaseUtils.executeWithExceptionLogging(() -> spaceService.getById(id));
    ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
    // 仅本人或者管理员可删除
    spaceService.checkSpaceAuth(loginUser, oldSpace);
    // 操作数据库
    boolean result = DatabaseUtils.executeWithExceptionLogging(() -> spaceService.removeById(id));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }
}
