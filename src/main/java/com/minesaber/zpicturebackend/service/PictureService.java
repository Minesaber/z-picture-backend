package com.minesaber.zpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minesaber.zpicturebackend.model.dto.picture.PictureEditRequest;
import com.minesaber.zpicturebackend.model.dto.picture.PictureQueryRequest;
import com.minesaber.zpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.minesaber.zpicturebackend.model.po.picture.Picture;
import com.minesaber.zpicturebackend.model.po.user.User;
import com.minesaber.zpicturebackend.model.vo.picture.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PictureService extends IService<Picture> {
  /**
   * 更新图片或上传新图片
   *
   * @param id id
   * @param multipartFile 图片
   * @param loginUser 当前登录图片
   * @return 视图
   */
  PictureVO uploadPicture(Long id, MultipartFile multipartFile, User loginUser);

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
}
