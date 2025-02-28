package com.minesaber.zpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minesaber.zpicturebackend.api.ai.aliyun.model.CreateOutPaintingTaskResponse;
import com.minesaber.zpicturebackend.model.dto.picture.*;
import com.minesaber.zpicturebackend.model.entity.picture.Picture;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.picture.PictureVO;

import java.util.List;

public interface PictureService extends IService<Picture> {
  /**
   * 更新图片或上传新图片
   *
   * @param pictureUploadRequest 图片上传请求
   * @param inputSource 文件源
   * @param loginUser 当前登录图片
   * @return 视图
   */
  PictureVO uploadPicture(
      PictureUploadRequest pictureUploadRequest, Object inputSource, User loginUser);

  /**
   * 批量抓取和创建图片
   *
   * @param pictureUploadByBatchRequest 批量导入图片请求
   * @param loginUser 当前登录用户
   * @return 成功创建的图片数
   */
  // todo 已经定义了获取登录用户的工具
  Integer uploadPictureByBatch(
      PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

  /**
   * 获取QueryWrapper
   *
   * @param request request
   * @return queryWrapper
   */
  QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest request);

  /**
   * 获取图片视图（脱敏）
   *
   * @param picture 图片
   * @return 图片视图
   */
  PictureVO convertToPictureVO(Picture picture);

  /**
   * 获取图片视图列表（脱敏）
   *
   * @param pictureList 图片列表
   * @return 图片视图列表
   */
  List<PictureVO> convertToPictureVOList(List<Picture> pictureList);

  /**
   * 根据颜色搜索图片
   *
   * @param spaceId 图片id
   * @param picColor 主色调
   * @param loginUser 当前登录用户
   * @return 图片视图
   */
  List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

  /**
   * 更新图片记录时，检查参数
   *
   * @param request 更新图片请求
   */
  void validUpdateRequest(PictureUpdateRequest request);

  /**
   * 编辑图片记录时，检查参数
   *
   * @param request 编辑图片请求
   */
  void validEditRequest(PictureEditRequest request);

  /**
   * 审核图片
   *
   * @param pictureReviewRequest 图片审核请求
   * @param loginUser 当前登录用户
   */
  void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

  /**
   * 补充审核参数
   *
   * @param picture 图片
   * @param loginUser 当前登录用户
   */
  void fillReviewParams(Picture picture, User loginUser);

  /**
   * 编辑图片
   *
   * @param pictureEditRequest 图片编辑请求
   * @param loginUser 当前登录用户
   */
  void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

  /**
   * 批量编辑图片
   *
   * @param pictureEditByBatchRequest 图片批量编辑请求
   * @param loginUser 当前登录用户
   */
  void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

  /**
   * 创建扩图任务
   *
   * @param pictureOutPaintingRequest 扩图请求
   * @param loginUser 当前登录用户
   * @return 创建扩图任务响应
   */
  CreateOutPaintingTaskResponse createPictureOutPaintingTask(
      PictureOutPaintingRequest pictureOutPaintingRequest, User loginUser);

  /**
   * 清理图片文件
   *
   * @param oldPicture 待删除图片
   */
  void cleanOldPicture(Picture oldPicture);

  /**
   * 删除图片
   *
   * @param pictureId 待删除图片id
   * @param loginUser 当前登录用户
   */
  void deletePicture(long pictureId, User loginUser);

  /**
   * 更新、删除的权限检查
   *
   * @param picture 待检查权限图片
   * @param loginUser 当前登录用户
   */
  void checkPictureAuth(Picture picture, User loginUser);
}
