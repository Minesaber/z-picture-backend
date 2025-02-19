package com.minesaber.zpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minesaber.zpicturebackend.model.dto.picture.*;
import com.minesaber.zpicturebackend.model.po.picture.Picture;
import com.minesaber.zpicturebackend.model.po.user.User;
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
   * 清理图片文件
   *
   * @param oldPicture 待删除图片
   */
  void cleanOldPicture(Picture oldPicture);
}
