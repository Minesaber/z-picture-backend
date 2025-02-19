package com.minesaber.zpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesaber.zpicturebackend.constants.FileConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.enums.PictureReviewStatus;
import com.minesaber.zpicturebackend.exception.BusinessException;
import com.minesaber.zpicturebackend.helpers.OssHelper;
import com.minesaber.zpicturebackend.helpers.upload.FileUploadTemplate;
import com.minesaber.zpicturebackend.helpers.upload.PictureUploadHelper;
import com.minesaber.zpicturebackend.helpers.upload.UrlUploadHelper;
import com.minesaber.zpicturebackend.mapper.PictureMapper;
import com.minesaber.zpicturebackend.model.bo.picture.UploadPictureResult;
import com.minesaber.zpicturebackend.model.dto.picture.*;
import com.minesaber.zpicturebackend.model.po.picture.Picture;
import com.minesaber.zpicturebackend.model.po.user.User;
import com.minesaber.zpicturebackend.enums.UserRole;
import com.minesaber.zpicturebackend.model.vo.picture.PictureVO;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import com.minesaber.zpicturebackend.service.PictureService;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {
  @Resource private UserService userService;
  @Resource private PictureUploadHelper pictureUploadHelper;
  @Resource private UrlUploadHelper urlUploadHelper;
  @Autowired private OssHelper ossHelper;

  @Override
  public PictureVO uploadPicture(
      PictureUploadRequest pictureUploadRequest, Object inputSource, User loginUser) {
    Long id = pictureUploadRequest.getId();
    // 更新图片需要做好参数检查
    Picture oldPicture = null;
    if (id != null) {
      // 查询图片是否存在
      oldPicture = getById(id);
      ThrowUtils.throwIf(oldPicture == null, ErrorCode.FORBIDDEN_ERROR, "资源不存在，或无权操作");
      // loginUserId是否与userId相同（或者当前登录用户为管理员）
      Long userId = oldPicture.getUserId();
      Long loginUserId = loginUser.getId();
      UserRole userRole = UserRole.getEnumByValue(loginUser.getUserRole());
      ThrowUtils.throwIf(
          userRole != UserRole.ADMIN && !userId.equals(loginUserId),
          ErrorCode.FORBIDDEN_ERROR,
          "资源不存在，或无权操作");
    }
    // 上传至OSS用户目录
    // todo 更新场景时，OSS中旧图会失去跟踪，需先清理或做其他处理
    String pathPrefix = String.format("public/%s", loginUser.getUserAccount());
    // 根据文件源类型区分工作方式
    FileUploadTemplate fileUploadTemplate = pictureUploadHelper;
    if (inputSource instanceof String) {
      fileUploadTemplate = urlUploadHelper;
    }
    UploadPictureResult uploadPictureResult =
        fileUploadTemplate.uploadPicture(pathPrefix, inputSource);
    // 操作数据库，更新图片记录
    String url = uploadPictureResult.getUrl();
    String thumbnailUrl = uploadPictureResult.getThumbnailUrl();
    // todo 已支持外层传递图片名称
    // result中name默认为解析的originalFilename去除后缀，而依赖外部参数构造的name为pictureUploadByBatchRequest.getNamePrefix()+(uploadCount+1)
    String name = uploadPictureResult.getName();
    if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
      name = pictureUploadRequest.getPicName();
    }
    Long picSize = uploadPictureResult.getPicSize();
    String picFormat = uploadPictureResult.getPicFormat();
    Integer picWidth = uploadPictureResult.getPicWidth();
    Integer picHeight = uploadPictureResult.getPicHeight();
    Double picScale = uploadPictureResult.getPicScale();
    Picture picture =
        Picture.builder()
            .url(url)
            .thumbnailUrl(thumbnailUrl)
            .name(name)
            .userId(loginUser.getId())
            .picSize(picSize)
            .picFormat(picFormat)
            .picWidth(picWidth)
            .picHeight(picHeight)
            .picScale(picScale)
            .build();
    if (id != null) {
      picture.setId(id);
      picture.setEditTime(new Date());
      // 更新时，清理老图片，缩略图如果存在也会被清理
      cleanOldPicture(oldPicture);
      // todo 原图仍被保存，且未追踪
    }
    // 补充审核参数
    fillReviewParams(picture, loginUser);
    Boolean opResult = DatabaseUtils.executeWithExceptionLogging(() -> saveOrUpdate(picture));
    // todo Time、UserVO、Url待处理
    ThrowUtils.throwIf(!opResult, ErrorCode.SYSTEM_ERROR, "图片上传失败，数据库异常");
    // 返回视图
    return PictureVO.convertToVO(picture);
  }

  @Override
  public Integer uploadPictureByBatch(
      PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
    // 校验参数
    String searchText = pictureUploadByBatchRequest.getSearchText();
    Integer count = pictureUploadByBatchRequest.getCount();
    ThrowUtils.throwIf(
        count > FileConstant.IMPORT_MAX_COUNT,
        ErrorCode.PARAMS_ERROR,
        "最多 " + FileConstant.IMPORT_MAX_COUNT + " 条");
    // 前缀默认等于搜索关键词
    String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
    if (StrUtil.isBlank(namePrefix)) {
      namePrefix = searchText;
    }
    // 抓取内容
    // todo 可以考虑支持切换导入引擎以及配置各类参数
    String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
    Document document;
    try {
      // todo 如果是访问单页以获取高清图，可能需要多次请求，补充操作间隔时间
      document = Jsoup.connect(fetchUrl).get();
    } catch (IOException e) {
      log.error("获取页面失败", e);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
    }
    // 解析内容
    Element div = document.getElementsByClass("dgControl").first();
    if (ObjUtil.isEmpty(div)) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
    }
    Elements imgElementList = div.select("img.mimg");
    // 遍历元素，依次处理上传图片
    int uploadCount = 0;
    for (Element imgElement : imgElementList) {
      String fileUrl = imgElement.attr("src");
      if (StrUtil.isBlank(fileUrl)) {
        log.info("当前链接为空，已跳过：{}", fileUrl);
        continue;
      }
      // 处理图片的地址，防止转义或者和对象存储冲突的问题
      int questionMarkIndex = fileUrl.indexOf("?");
      if (questionMarkIndex > -1) {
        fileUrl = fileUrl.substring(0, questionMarkIndex);
      }
      // 上传图片
      PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
      pictureUploadRequest.setUrl(fileUrl);
      pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
      // todo 暂未考虑批量
      try {
        PictureVO pictureVO = uploadPicture(pictureUploadRequest, fileUrl, loginUser);
        log.info("图片上传成功，id = {}", pictureVO.getId());
        uploadCount++;
      } catch (Exception e) {
        log.error("图片上传失败", e);
        continue;
      }
      if (uploadCount >= count) {
        break;
      }
    }
    return uploadCount;
  }

  @Override
  public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest request) {
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    // 1、检查参数
    if (request == null) {
      return queryWrapper;
    }
    // 2、获取查询参数
    Long id = request.getId();
    String name = request.getName();
    Long userId = request.getUserId();
    String profile = request.getProfile();
    String category = request.getCategory();
    Long picSize = request.getPicSize();
    String picFormat = request.getPicFormat();
    Integer picWidth = request.getPicWidth();
    Integer picHeight = request.getPicHeight();
    Double picScale = request.getPicScale();
    // 审核字段
    Integer reviewStatus = request.getReviewStatus();
    Long reviewerId = request.getReviewerId();
    String reviewMessage = request.getReviewMessage();
    Date reviewTime = request.getReviewTime();
    // 3、构建queryWrapper实例
    queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
    queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
    queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
    queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
    queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
    queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
    queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
    queryWrapper.like(StrUtil.isNotBlank(profile), "profile", profile);
    queryWrapper.like(StrUtil.isNotBlank(category), "category", category);
    queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
    // 审核字段筛选
    queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
    queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
    queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
    // tips：需要特殊处理的搜索条件
    List<String> tags = request.getTags();
    String searchText = request.getSearchText();
    queryWrapper.and(
        CollUtil.isNotEmpty(tags), qw -> tags.forEach(tag -> qw.like("tags", "\"" + tag + "\"")));
    queryWrapper.and(
        StrUtil.isNotBlank(searchText),
        qw ->
            qw.like(StrUtil.isNotBlank(searchText), "name", searchText)
                .or()
                .like(StrUtil.isNotBlank(searchText), "profile", searchText));
    // tips：系统采用desc，所以isAsc参数传false
    String sortField = request.getSortField();
    String sortOrder = request.getSortOrder();
    queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
    return queryWrapper;
  }

  @Override
  public PictureVO convertToPictureVO(Picture picture) {
    PictureVO pictureVO = PictureVO.convertToVO(picture);
    Long userId = picture.getUserId();
    if (userId != null && userId > 0) {
      User user = userService.getById(userId);
      UserVO userVO = UserVO.convertToUserVO(user);
      pictureVO.setUserVO(userVO);
    }
    return pictureVO;
  }

  @Override
  public List<PictureVO> convertToPictureVOList(List<Picture> pictureList) {
    if (pictureList == null || pictureList.isEmpty()) {
      return Collections.emptyList();
    }
    // 批量脱敏
    List<PictureVO> pictureVOList =
        pictureList.stream().map(PictureVO::convertToVO).collect(Collectors.toList());
    // 补充用户视图
    Map<Long, UserVO> userVOMap =
        DatabaseUtils.executeWithExceptionLogging(
                () ->
                    userService.listByIds(
                        pictureVOList.stream().map(PictureVO::getId).collect(Collectors.toSet())))
            .stream()
            .map(UserVO::convertToUserVO)
            .collect(
                Collectors.toMap(
                    UserVO::getId, userVO -> userVO, (existing, replacement) -> existing));
    pictureVOList.forEach(pictureVO -> pictureVO.setUserVO(userVOMap.get(pictureVO.getId())));
    return pictureVOList;
  }

  @Override
  public void validUpdateRequest(PictureUpdateRequest request) {
    ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
    // 检查参数
    Long id = request.getId();
    String name = request.getName();
    String profile = request.getProfile();
    String category = request.getCategory();
    List<String> tags = request.getTags();
    ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "id 异常");
    if (StrUtil.isNotBlank(name)) {
      ThrowUtils.throwIf(
          name.length() > FileConstant.FILE_NAME_MAX_LENGTH, ErrorCode.PARAMS_ERROR, "图片名称过长");
    }
    if (StrUtil.isNotBlank(profile)) {
      ThrowUtils.throwIf(
          profile.length() > FileConstant.PROFILE_MAX_LENGTH, ErrorCode.PARAMS_ERROR, "简介过长");
    }
    if (StrUtil.isNotBlank(category)) {
      ThrowUtils.throwIf(
          category.length() > FileConstant.CATEGORY_MAX_LENGTH, ErrorCode.PARAMS_ERROR, "分类字段过长");
    }
    String tagsStr = JSONUtil.toJsonStr(tags);
    if (StrUtil.isNotBlank(tagsStr)) {
      ThrowUtils.throwIf(
          tagsStr.length() > FileConstant.TAGS_MAX_LENGTH, ErrorCode.PARAMS_ERROR, "标签字段过长");
    }
  }

  @Override
  public void validEditRequest(PictureEditRequest request) {
    PictureUpdateRequest pictureUpdateRequest = new PictureUpdateRequest();
    BeanUtil.copyProperties(request, pictureUpdateRequest);
    validUpdateRequest(pictureUpdateRequest);
  }

  @Override
  public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
    // 1. 校验参数
    ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
    Long id = pictureReviewRequest.getId();
    Integer reviewStatus = pictureReviewRequest.getReviewStatus();
    PictureReviewStatus reviewStatusEnum = PictureReviewStatus.getEnumByValue(reviewStatus);
    String reviewMessage = pictureReviewRequest.getReviewMessage();
    // 暂不允许将状态改回待审核
    ThrowUtils.throwIf(
        id == null
            || reviewStatusEnum == null
            || PictureReviewStatus.REVIEWING.equals(reviewStatusEnum),
        new BusinessException(ErrorCode.PARAMS_ERROR));
    // 2. 判断图片是否存在
    Picture oldPicture = this.getById(id);
    ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
    // 3. 校验审核状态是否重复，已是改状态
    ThrowUtils.throwIf(
        oldPicture.getReviewStatus().equals(reviewStatus), ErrorCode.PARAMS_ERROR, "请勿重复审核");
    // 4. 数据库操作
    Picture updatePicture = Picture.builder().build();
    BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
    updatePicture.setReviewerId(loginUser.getId());
    updatePicture.setReviewTime(new Date());
    boolean result = this.updateById(updatePicture);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
  }

  @Override
  public void fillReviewParams(Picture picture, User loginUser) {
    if (userService.isAdmin(loginUser)) {
      picture.setReviewStatus(PictureReviewStatus.PASS.getValue());
      picture.setReviewerId(loginUser.getId());
      picture.setReviewMessage("管理员操作自动过审");
      picture.setReviewTime(new Date());
    } else {
      // 非管理员，创建和编辑都需要等待后续审核
      picture.setReviewStatus(PictureReviewStatus.REVIEWING.getValue());
    }
  }

  // todo @Async默认使用SimpleAsyncTaskExecutor
  @Async
  @Override
  public void cleanOldPicture(Picture oldPicture) {
    // 判断待删除图片是否被多条记录使用
    String pictureUrl = oldPicture.getUrl();
    long count = this.lambdaQuery().eq(Picture::getUrl, pictureUrl).count();
    // 有不止一条记录用到了该图片，不清理
    if (count > 1) {
      return;
    }
    // 删除图片、缩略图
    // todo 需要考虑缩略图也可能被多条记录使用的情况
    ossHelper.deleteObject(pictureUrl);
    String thumbnailUrl = oldPicture.getThumbnailUrl();
    if (StrUtil.isNotBlank(thumbnailUrl)) {
      ossHelper.deleteObject(thumbnailUrl);
    }
  }
}
