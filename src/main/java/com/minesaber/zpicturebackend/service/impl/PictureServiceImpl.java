package com.minesaber.zpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesaber.zpicturebackend.constant.FileConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.helpers.FileHelper;
import com.minesaber.zpicturebackend.mapper.PictureMapper;
import com.minesaber.zpicturebackend.model.bo.picture.UploadPictureResult;
import com.minesaber.zpicturebackend.model.dto.picture.PictureEditRequest;
import com.minesaber.zpicturebackend.model.dto.picture.PictureQueryRequest;
import com.minesaber.zpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.minesaber.zpicturebackend.model.po.picture.Picture;
import com.minesaber.zpicturebackend.model.po.user.User;
import com.minesaber.zpicturebackend.enums.UserRole;
import com.minesaber.zpicturebackend.model.vo.picture.PictureVO;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import com.minesaber.zpicturebackend.service.PictureService;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {
  @Resource private UserService userService;
  @Resource private FileHelper fileHelper;

  @Override
  public PictureVO uploadPicture(Long id, MultipartFile multipartFile, User loginUser) {
    // 更新图片需要做好参数检查
    if (id != null) {
      // 查询图片是否存在
      Picture picture = getById(id);
      ThrowUtils.throwIf(picture == null, ErrorCode.FORBIDDEN_ERROR, "资源不存在，或无权操作");
      // loginUserId是否与userId相同（或者当前登录用户为管理员）
      Long userId = picture.getUserId();
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
    UploadPictureResult uploadPictureResult = fileHelper.uploadPicture(pathPrefix, multipartFile);
    // 操作数据库，更新图片记录
    String url = uploadPictureResult.getUrl();
    String name = uploadPictureResult.getName();
    Long picSize = uploadPictureResult.getPicSize();
    String picFormat = uploadPictureResult.getPicFormat();
    Integer picWidth = uploadPictureResult.getPicWidth();
    Integer picHeight = uploadPictureResult.getPicHeight();
    Double picScale = uploadPictureResult.getPicScale();
    Picture picture =
        Picture.builder()
            .url(url)
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
    }
    Boolean opResult = DatabaseUtils.executeWithExceptionLogging(() -> saveOrUpdate(picture));
    // todo Time、UserVO、Url待处理
    ThrowUtils.throwIf(!opResult, ErrorCode.SYSTEM_ERROR, "图片上传失败，数据库异常");
    // 返回视图
    return PictureVO.convertToVO(picture);
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
    // 3、构建queryWrapper实例
    queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
    queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
    queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
    queryWrapper.like(StrUtil.isNotBlank(profile), "profile", profile);
    queryWrapper.like(StrUtil.isNotBlank(category), "category", category);
    queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
    queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
    queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
    queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
    queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
    // tips：需要特殊处理的搜索条件
    List<String> tags = request.getTags();
    String searchText = request.getSearchText();
    queryWrapper.and(
        CollUtil.isNotEmpty(tags), qw -> tags.forEach(tag -> qw.like("tags", "\"" + tag + "\"")));
    queryWrapper.and(
        StrUtil.isNotBlank(searchText),
        qw ->
            qw.like(StrUtil.isNotBlank(searchText), "name", name)
                .or()
                .like(StrUtil.isNotBlank(searchText), "profile", profile));
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
    // 批量脱敏
    List<PictureVO> pictureVOList =
        pictureList.stream().map(PictureVO::convertToVO).collect(Collectors.toList());
    // 补充用户视图
    Map<Long, UserVO> userVOMap =
        userService
            .listByIds(pictureVOList.stream().map(PictureVO::getId).collect(Collectors.toSet()))
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
}
