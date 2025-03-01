package com.minesaber.zpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesaber.zpicturebackend.api.ai.aliyun.helpers.PictureAIHelper;
import com.minesaber.zpicturebackend.api.ai.aliyun.model.CreateOutPaintingTaskRequest;
import com.minesaber.zpicturebackend.api.ai.aliyun.model.CreateOutPaintingTaskResponse;
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
import com.minesaber.zpicturebackend.model.entity.picture.Picture;
import com.minesaber.zpicturebackend.model.entity.space.Space;
import com.minesaber.zpicturebackend.model.entity.user.User;
import com.minesaber.zpicturebackend.model.vo.picture.PictureVO;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import com.minesaber.zpicturebackend.service.PictureService;
import com.minesaber.zpicturebackend.service.SpaceService;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.utils.ColorSimilarUtils;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {
  @Resource private UserService userService;
  @Resource private PictureUploadHelper pictureUploadHelper;
  @Resource private UrlUploadHelper urlUploadHelper;
  @Resource private OssHelper ossHelper;
  @Resource private SpaceService spaceService;
  @Resource private TransactionTemplate transactionTemplate;
  @Resource private PictureAIHelper pictureAIHelper;

  // todo 暂不考虑图片在不同空间中移动的情况
  // todo 之后增添适配企业空间的处理逻辑，企业空间更新场景下清理前可能需要做单独备份或版本管理
  // todo 原图仍被保存，且未追踪
  // todo 已支持外层传递图片名称
  // todo Time、UserVO、Url待处理
  // todo 更新场景时，OSS中旧图会失去跟踪，需先清理或做其他处理
  // todo 空间严格限制
  // 说明：result中name默认为解析的originalFilename去除后缀，而依赖外部参数构造的name为pictureUploadByBatchRequest.getNamePrefix()+(uploadCount+1)
  @Override
  public PictureVO uploadPicture(
      PictureUploadRequest pictureUploadRequest, Object inputSource, User loginUser) {
    Long id = pictureUploadRequest.getId();
    Long spaceId;
    Picture oldPicture;
    // 更新
    if (id != null) {
      // 是否存在（原图）
      oldPicture = DatabaseUtils.executeWithExceptionLogging(() -> getById(id));
      ThrowUtils.throwIf(oldPicture == null, ErrorCode.FORBIDDEN_ERROR, "资源不存在，或无权操作");
      spaceId = oldPicture.getSpaceId();
      // 是否有权限（基于原图 spaceId）
      checkPictureAuth(oldPicture, loginUser);
    } else {
      oldPicture = null;
      // 新增（只需检查向私有空间新增）
      spaceId = pictureUploadRequest.getSpaceId();
      if (spaceId != null) {
        // 是否存在（查询 spaceId 的空间信息）
        Space space =
            DatabaseUtils.executeWithExceptionLogging(() -> spaceService.getById(spaceId));
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在，或无权操作");
        // 是否有权限
        ThrowUtils.throwIf(
            !loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "空间不存在，或无权操作");
        // 其他
        ThrowUtils.throwIf(
            space.getTotalCount() >= space.getMaxCount(), ErrorCode.OPERATION_ERROR, "空间条数不足");
        ThrowUtils.throwIf(
            space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR, "空间大小不足");
      }
    }
    // 确定上传参数（路径前缀）
    String pathPrefix;
    if (spaceId == null) {
      pathPrefix = String.format("public/%s", loginUser.getUserAccount());
    } else {
      pathPrefix = String.format("space/%s", spaceId);
    }
    // 确定上传方式，并执行上传（OSS）
    FileUploadTemplate fileUploadTemplate = pictureUploadHelper;
    if (inputSource instanceof String) {
      fileUploadTemplate = urlUploadHelper;
    }
    UploadPictureResult uploadPictureResult =
        fileUploadTemplate.uploadPicture(pathPrefix, inputSource);
    // 确定入库参数（上传结果字段、自定义名称、补充审核参数）
    String url = uploadPictureResult.getUrl();
    String thumbnailUrl = uploadPictureResult.getThumbnailUrl();
    String name = uploadPictureResult.getName();
    Long picSize = uploadPictureResult.getPicSize();
    String picFormat = uploadPictureResult.getPicFormat();
    Integer picWidth = uploadPictureResult.getPicWidth();
    Integer picHeight = uploadPictureResult.getPicHeight();
    Double picScale = uploadPictureResult.getPicScale();
    String picColor = uploadPictureResult.getPicColor();
    /*Picture picture =
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
        .spaceId(spaceId)
        .picColor(picColor)
        .build();*/
    // 改为set方式
    Picture picture = new Picture();
    picture.setUrl(url);
    picture.setThumbnailUrl(thumbnailUrl);
    picture.setName(name);
    picture.setUserId(loginUser.getId());
    picture.setPicSize(picSize);
    picture.setPicFormat(picFormat);
    picture.setPicWidth(picWidth);
    picture.setPicHeight(picHeight);
    picture.setPicScale(picScale);
    picture.setSpaceId(spaceId);
    picture.setPicColor(picColor);
    if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
      picture.setName(pictureUploadRequest.getPicName());
    }
    fillReviewParams(picture, loginUser);
    // 更新场景（补充更新参数、清理原图片）
    if (id != null) {
      picture.setId(id);
      picture.setEditTime(new Date());
      cleanOldPicture(oldPicture);
    }
    // 执行入库（MySQL）
    transactionTemplate.execute(
        status -> {
          boolean picUpResult =
              DatabaseUtils.executeWithExceptionLogging(() -> saveOrUpdate(picture));
          ThrowUtils.throwIf(!picUpResult, ErrorCode.SYSTEM_ERROR, "图片上传失败，数据库异常");
          Long sizeChange = picSize;
          if (id != null) {
            sizeChange -= oldPicture.getPicSize();
          }
          if (spaceId != null) {
            boolean spaceUpResult =
                spaceService
                    .lambdaUpdate()
                    .eq(Space::getId, spaceId)
                    .setSql("totalSize=totalSize+" + sizeChange)
                    .setSql("totalCount=totalCount+1")
                    .update();
            ThrowUtils.throwIf(!spaceUpResult, ErrorCode.SYSTEM_ERROR, "空间额度更新失败，数据库异常");
          }
          // 更新场景，如果新图的大小过小，不会生成缩略图，此时则直接清理原图的缩略图
          if (id != null) {
            if (picSize <= FileConstant.USE_THUMBNAIL_SIZE) {
              // todo 完整的清理，而非仅清除url
              boolean cleanThumbnailUrl =
                  lambdaUpdate().eq(Picture::getId, id).setSql("thumbnailUrl=null").update();
              ThrowUtils.throwIf(!cleanThumbnailUrl, ErrorCode.SYSTEM_ERROR, "缩略图清理失败，数据库异常");
            }
          }
          return true;
        });
    // 返回结果（脱敏）
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
    ThrowUtils.throwIf(
        ObjUtil.isEmpty(div), new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败"));
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

  // todo 根据审核时间查询
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
    // 审核字段、空间字段、时间字段
    Integer reviewStatus = request.getReviewStatus();
    Long reviewerId = request.getReviewerId();
    String reviewMessage = request.getReviewMessage();
    Date reviewTime = request.getReviewTime();
    Long spaceId = request.getSpaceId();
    boolean nullSpaceId = request.isNullSpaceId();
    Date startEditTime = request.getStartEditTime();
    Date endEditTime = request.getEndEditTime();
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
    // 匹配审核字段、空间字段、时间字段
    queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
    queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
    queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
    queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
    queryWrapper.isNull(nullSpaceId, "spaceId");
    queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
    queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
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

  // todo 代码抽取
  // todo 参数传递规范
  @Override
  public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
    // 1. 检查参数
    ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
    // 2. 是否存在，是否有权限
    Space space = spaceService.getById(spaceId);
    ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
    if (!space.getUserId().equals(loginUser.getId())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
    }
    // 3. 查询该空间下的所有图片（必须要有主色调）
    List<Picture> pictureList =
        this.lambdaQuery().eq(Picture::getSpaceId, spaceId).isNotNull(Picture::getPicColor).list();
    // 如果没有图片，直接返回空列表
    if (CollUtil.isEmpty(pictureList)) {
      return new ArrayList<>();
    }
    // 4. 计算相似度并排序
    List<Picture> sortedPictureList =
        pictureList.stream()
            .sorted(
                Comparator.comparingDouble(
                    picture -> {
                      String hexColor = picture.getPicColor();
                      // 没有主色调的图片会默认排序到最后
                      if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                      }
                      // 计算相似度，越大越相似
                      return -ColorSimilarUtils.calculateSimilarity(picColor, hexColor);
                    }))
            // 限制查询数量
            .limit(12)
            .collect(Collectors.toList());
    // 5. 返回结果
    return sortedPictureList.stream().map(PictureVO::convertToVO).collect(Collectors.toList());
  }

  @Override
  public void validEditRequest(PictureEditRequest request) {
    PictureUpdateRequest pictureUpdateRequest = new PictureUpdateRequest();
    BeanUtil.copyProperties(request, pictureUpdateRequest);
    validUpdateRequest(pictureUpdateRequest);
  }

  @Override
  public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
    // 校验参数
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
    // 判断图片是否存在
    Picture oldPicture = getById(id);
    ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
    // 校验审核状态是否重复，已是改状态
    ThrowUtils.throwIf(
        oldPicture.getReviewStatus().equals(reviewStatus), ErrorCode.PARAMS_ERROR, "请勿重复审核");
    // 数据库操作
    Picture updatePicture = new Picture();
    BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
    updatePicture.setReviewerId(loginUser.getId());
    updatePicture.setReviewTime(new Date());
    boolean result = DatabaseUtils.executeWithExceptionLogging(() -> updateById(updatePicture));
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

  // todo 其他controller方法也可抽取
  @Override
  public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
    Long id = pictureEditRequest.getId();
    Picture oldPicture = DatabaseUtils.executeWithExceptionLogging(() -> getById(id));
    ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
    // 是否有权限
    checkPictureAuth(oldPicture, loginUser);
    // 补充编辑时间、审核参数
    Picture picture = new Picture();
    BeanUtils.copyProperties(pictureEditRequest, picture);
    picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
    picture.setEditTime(new Date());
    fillReviewParams(picture, loginUser);
    boolean result = DatabaseUtils.executeWithExceptionLogging(() -> updateById(picture));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
  }

  @Override
  public void editPictureByBatch(
      PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
    // 信息检查
    List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
    Long spaceId = pictureEditByBatchRequest.getSpaceId();
    String category = pictureEditByBatchRequest.getCategory();
    List<String> tags = pictureEditByBatchRequest.getTags();
    ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
    ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
    // 是否有权限
    Space space = DatabaseUtils.executeWithExceptionLogging(() -> spaceService.getById(spaceId));
    ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
    if (!space.getUserId().equals(loginUser.getId())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
    }
    // 查询指定图片（仅选择需要的字段）
    List<Picture> pictureList =
        this.lambdaQuery()
            .select(Picture::getId, Picture::getSpaceId)
            .eq(Picture::getSpaceId, spaceId)
            .in(Picture::getId, pictureIdList)
            .list();
    // 改为使用手动sql查询
    if (pictureList.isEmpty()) {
      return;
    }
    // 更新分类和标签
    pictureList.forEach(
        picture -> {
          if (StrUtil.isNotBlank(category)) {
            picture.setCategory(category);
          }
          if (CollUtil.isNotEmpty(tags)) {
            picture.setTags(JSONUtil.toJsonStr(tags));
          }
        });
    // 批量重命名
    String nameRule = pictureEditByBatchRequest.getNameRule();
    fillPictureWithNameRule(pictureList, nameRule);
    // 操作数据库进行批量更新
    // todo 可能存在事务失效
    boolean result = DatabaseUtils.executeWithExceptionLogging(() -> updateBatchById(pictureList));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "批量编辑失败");
  }

  /**
   * nameRule 格式：图片{序号}
   *
   * @param pictureList 图片列表
   * @param nameRule 名称规则
   */
  private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
    if (StrUtil.isBlank(nameRule) || CollUtil.isEmpty(pictureList)) {
      return;
    }
    long count = 1;
    try {
      for (Picture picture : pictureList) {
        String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
        picture.setName(pictureName);
      }
    } catch (Exception e) {
      log.error("名称解析错误", e);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
    }
  }

  // todo 用户可以离开页面，之后再查看自己的扩图任务
  @Override
  public CreateOutPaintingTaskResponse createPictureOutPaintingTask(
      PictureOutPaintingRequest pictureOutPaintingRequest, User loginUser) {
    // 是否存在
    Long pictureId = pictureOutPaintingRequest.getPictureId();
    Picture picture =
        Optional.ofNullable(this.getById(pictureId))
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
    // 是否有权限
    checkPictureAuth(picture, loginUser);
    // 检查图片是否符合要求
    if (picture.getPicWidth() < 512 || picture.getPicHeight() < 512) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建任务失败，图片宽度或高度低于512像素");
    }
    // 创建扩图任务
    CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
    CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
    input.setImageUrl(picture.getUrl());
    createOutPaintingTaskRequest.setInput(input);
    createOutPaintingTaskRequest.setParameters(pictureOutPaintingRequest.getParameters());
    // 创建任务
    return pictureAIHelper.createOutPaintingTask(createOutPaintingTaskRequest);
  }

  // todo @Async默认使用SimpleAsyncTaskExecutor
  @Async
  @Override
  public void cleanOldPicture(Picture oldPicture) {
    // 判断待删除图片是否被多条记录使用
    String pictureUrl = oldPicture.getUrl();
    // todo 暂时不考虑多条记录引用的情况：有不止一条记录用到了该图片，不清理
    /*long count = lambdaQuery().eq(Picture::getUrl, pictureUrl).count();
    if (count > 1) {
      return;
    }*/
    // 删除图片、缩略图
    // todo 可能也需要考虑缩略图也可能被多条记录使用的情况
    ossHelper.deleteObject(ossHelper.getKeyFromUrl(pictureUrl));
    String thumbnailUrl = oldPicture.getThumbnailUrl();
    if (StrUtil.isNotBlank(thumbnailUrl)) {
      ossHelper.deleteObject(ossHelper.getKeyFromUrl(thumbnailUrl));
    }
  }

  // todo 同上，其他也可抽取
  @Override
  public void deletePicture(long pictureId, User loginUser) {
    Picture oldPicture = DatabaseUtils.executeWithExceptionLogging(() -> getById(pictureId));
    ThrowUtils.throwIf(oldPicture == null, ErrorCode.FORBIDDEN_ERROR, "资源不存在，或无权操作");
    // 是否有权限
    checkPictureAuth(oldPicture, loginUser);
    // 入库
    transactionTemplate.execute(
        status -> {
          Long spaceId = oldPicture.getSpaceId();
          Long picSize = oldPicture.getPicSize();
          boolean result = DatabaseUtils.executeWithExceptionLogging(() -> removeById(pictureId));
          ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "图片删除失败，数据库异常");
          if (spaceId != null) {
            boolean spaceUpResult =
                spaceService
                    .lambdaUpdate()
                    .eq(Space::getId, spaceId)
                    .setSql("totalSize=totalSize-" + picSize)
                    .setSql("totalCount=totalCount-1")
                    .update();
            ThrowUtils.throwIf(!spaceUpResult, ErrorCode.SYSTEM_ERROR, "空间额度更新失败，数据库异常");
          }
          // 删除时，清理图片资源
          cleanOldPicture(oldPicture);
          return true;
        });
  }

  // todo 补充region、endregion
  @Override
  public void checkPictureAuth(Picture picture, User loginUser) {
    Long spaceId = picture.getSpaceId();
    Long loginUserId = loginUser.getId();
    // 是否有权限：公有图库和私有空间
    if (spaceId == null) {
      ThrowUtils.throwIf(
          !userService.isAdmin(loginUser) && !picture.getUserId().equals(loginUserId),
          ErrorCode.FORBIDDEN_ERROR,
          "资源不存在，或无权操作");
    } else {
      ThrowUtils.throwIf(
          !picture.getUserId().equals(loginUserId), ErrorCode.FORBIDDEN_ERROR, "资源不存在，或无权操作");
    }
  }
}
