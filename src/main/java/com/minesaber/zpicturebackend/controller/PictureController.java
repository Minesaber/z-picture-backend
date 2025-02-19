package com.minesaber.zpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.minesaber.zpicturebackend.aop.annotation.AuthCheck;
import com.minesaber.zpicturebackend.constants.FileConstant;
import com.minesaber.zpicturebackend.constants.UserConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.enums.PictureReviewStatus;
import com.minesaber.zpicturebackend.enums.UserRole;
import com.minesaber.zpicturebackend.model.dto.base.DeleteRequest;
import com.minesaber.zpicturebackend.model.dto.picture.*;
import com.minesaber.zpicturebackend.model.po.picture.Picture;
import com.minesaber.zpicturebackend.model.po.user.User;
import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.model.vo.picture.PictureCategoryTagVO;
import com.minesaber.zpicturebackend.model.vo.picture.PictureVO;
import com.minesaber.zpicturebackend.service.PictureService;
import com.minesaber.zpicturebackend.service.UserService;
import com.minesaber.zpicturebackend.utils.DatabaseUtils;
import com.minesaber.zpicturebackend.utils.ResultUtils;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {
  @Resource private PictureService pictureService;
  @Resource private UserService userService;
  @Resource private StringRedisTemplate stringRedisTemplate;

  // todo 可以考虑使用单独的service提供缓存服务
  /** 本地缓存 */
  private final Cache<String, String> LOCAL_CACHE =
      Caffeine.newBuilder()
          .initialCapacity(1024)
          // 最大 10000 条
          .maximumSize(10_000L)
          // 缓存 5 分钟后移除
          .expireAfterWrite(Duration.ofMinutes(5))
          .build();

  /**
   * 上传或更新图片
   *
   * @param multipartFile 文件
   * @param pictureUploadRequest 上传图片请求
   */
  @PostMapping("/upload")
  @AuthCheck
  // todo 参数顺序待规范
  public Response<PictureVO> uploadPicture(
      @RequestPart("file") MultipartFile multipartFile,
      PictureUploadRequest pictureUploadRequest,
      HttpServletRequest request) {
    ThrowUtils.throwIf(
        multipartFile == null || pictureUploadRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    PictureVO pictureVO =
        pictureService.uploadPicture(pictureUploadRequest, multipartFile, loginUser);
    return ResultUtils.success(pictureVO);
  }

  /**
   * 通过 URL 上传图片
   *
   * @param pictureUploadRequest 上传图片请求
   * @param request request
   */
  @PostMapping("/upload/url")
  @AuthCheck
  // todo 参数顺序待规范
  public Response<PictureVO> uploadPictureByUrl(
      @RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
    String fileUrl = null;
    ThrowUtils.throwIf(
        pictureUploadRequest == null || (StrUtil.isBlank(fileUrl = pictureUploadRequest.getUrl())),
        ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    PictureVO pictureVO = pictureService.uploadPicture(pictureUploadRequest, fileUrl, loginUser);
    return ResultUtils.success(pictureVO);
  }

  /** 批量抓取并创建图片 */
  @PostMapping("/upload/batch")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Integer> uploadPictureByBatch(
      @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
      HttpServletRequest request) {
    ThrowUtils.throwIf(
        pictureUploadByBatchRequest == null
            || StrUtil.isBlank(pictureUploadByBatchRequest.getSearchText()),
        ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
    return ResultUtils.success(uploadCount);
  }

  /**
   * 根据id查询图片（管理员）
   *
   * @param id id
   * @return 图片
   */
  @GetMapping("/get")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Picture> getPictureById(Long id) {
    ThrowUtils.throwIf(ObjUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR);
    Picture picture = DatabaseUtils.executeWithExceptionLogging(() -> pictureService.getById(id));
    ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
    return ResultUtils.success(picture);
  }

  /**
   * 根据id查询图片
   *
   * @param id id
   * @return 图片（脱敏）
   */
  @GetMapping("/get/vo")
  @AuthCheck
  public Response<PictureVO> getPictureVOById(Long id) {
    // todo 此次用户可以通过id查看未过审图片
    Picture picture = getPictureById(id).getData();
    PictureVO pictureVO = pictureService.convertToPictureVO(picture);
    return ResultUtils.success(pictureVO);
  }

  /**
   * 分页查询图片（管理员）
   *
   * @param request 图片查询请求
   * @return 图片列表
   */
  // todo 各类业务方法命名待规范，VOS/VOList
  @PostMapping("/list/page")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Page<Picture>> getPictureByPage(@RequestBody PictureQueryRequest request) {
    ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
    int current = request.getCurrent();
    int pageSize = request.getPageSize();
    Page<Picture> picturePage =
        pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(request));
    return ResultUtils.success(picturePage);
  }

  /**
   * 分页查询图片
   *
   * @param request 图片查询请求
   * @return 图片列表（脱敏）
   */
  @Deprecated
  // @PostMapping("/list/page/vo")
  @AuthCheck
  public Response<Page<PictureVO>> getPictureVOByPage(@RequestBody PictureQueryRequest request) {
    int current = request.getCurrent();
    int pageSize = request.getPageSize();
    // 限制每次获取数据的条数
    ThrowUtils.throwIf(pageSize > FileConstant.RECORDS_MAX_COUNT, ErrorCode.PARAMS_ERROR);
    // 普通用户只能看到过审的图片
    request.setReviewStatus(PictureReviewStatus.PASS.getValue());
    Page<Picture> page = getPictureByPage(request).getData();
    long total = page.getTotal();
    List<Picture> records = page.getRecords();
    Page<PictureVO> pictureVOPage = new Page<>(current, pageSize, total);
    List<PictureVO> pictureVORecords = pictureService.convertToPictureVOList(records);
    pictureVOPage.setRecords(pictureVORecords);
    return ResultUtils.success(pictureVOPage);
  }

  /**
   * 分页查询图片（带缓存）
   *
   * @param pictureQueryRequest 图片查询请求
   * @param request request
   * @return 图片列表（脱敏）
   */
  @PostMapping("/list/page/vo")
  @AuthCheck
  public Response<Page<PictureVO>> getPictureVOByPageWithCache(
      @RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
    int current = pictureQueryRequest.getCurrent();
    int pageSize = pictureQueryRequest.getPageSize();
    // 限制每次获取数据的条数
    ThrowUtils.throwIf(pageSize > FileConstant.RECORDS_MAX_COUNT, ErrorCode.PARAMS_ERROR);
    // 普通用户只能看到过审的图片
    pictureQueryRequest.setReviewStatus(PictureReviewStatus.PASS.getValue());
    // todo 可以考虑使用单独的service提供多级缓存服务
    // 1. 先从本地缓存中查询
    String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
    String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
    String cacheKey = String.format("zpicture:getPictureVOByPageWithCache:%s", hashKey);
    String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
    if (cachedValue != null) {
      Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
      return ResultUtils.success(cachedPage);
    }
    // 2. 本地缓存未命中，查询 Redis
    ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
    cachedValue = opsForValue.get(cacheKey);
    if (cachedValue != null) {
      // 如果缓存命中，更新本地缓存，返回结果
      LOCAL_CACHE.put(cacheKey, cachedValue);
      Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
      return ResultUtils.success(cachedPage);
    }
    // 3. 都未命中，则查询数据库并更新多级缓存
    Page<Picture> page = getPictureByPage(pictureQueryRequest).getData();
    long total = page.getTotal();
    List<Picture> records = page.getRecords();
    Page<PictureVO> pictureVOPage = new Page<>(current, pageSize, total);
    List<PictureVO> pictureVORecords = pictureService.convertToPictureVOList(records);
    pictureVOPage.setRecords(pictureVORecords);
    // 4. 更新缓存
    // todo 可以考虑压缩数据
    String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
    int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
    LOCAL_CACHE.put(cacheKey, cacheValue);
    opsForValue.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
    return ResultUtils.success(pictureVOPage);
  }

  /**
   * 根据id更新图片信息（管理员）
   *
   * @param pictureUpdateRequest 图片更新请求
   * @return void
   */
  // todo 形参命名待规范
  @PostMapping("/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Void> updatePictureById(
      @RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
    pictureService.validUpdateRequest(pictureUpdateRequest);
    Picture picture = Picture.builder().build();
    // todo 数组转JSON待检查
    BeanUtil.copyProperties(pictureUpdateRequest, picture);
    Long id = pictureUpdateRequest.getId();
    Picture oldPicture =
        DatabaseUtils.executeWithExceptionLogging(() -> pictureService.getById(id));
    ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
    // 补充审核参数
    User loginUser = userService.getLoginUser(request);
    pictureService.fillReviewParams(picture, loginUser);
    boolean result =
        DatabaseUtils.executeWithExceptionLogging(() -> pictureService.updateById(picture));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(null);
  }

  /**
   * 根据id编辑图片信息
   *
   * @param pictureEditRequest 图片编辑请求
   * @return void
   */
  @PostMapping("/edit")
  @AuthCheck
  public Response<Void> editPictureById(
      @RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
    pictureService.validEditRequest(pictureEditRequest);
    Picture picture = Picture.builder().build();
    // todo 字符串转JSON需要手动处理，hutool会忽略双引号
    BeanUtils.copyProperties(pictureEditRequest, picture);
    picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
    // 补充编辑时间
    picture.setEditTime(new Date());
    Long id = pictureEditRequest.getId();
    Picture oldPicture =
        DatabaseUtils.executeWithExceptionLogging(() -> pictureService.getById(id));
    ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
    // 检查权限
    User loginUser = userService.getLoginUser(request);
    ThrowUtils.throwIf(
        !oldPicture.getUserId().equals(loginUser.getId())
            && UserRole.getEnumByValue(loginUser.getUserRole()) != UserRole.ADMIN,
        ErrorCode.NO_AUTH_ERROR);
    // 补充审核参数
    pictureService.fillReviewParams(picture, loginUser);
    boolean result =
        DatabaseUtils.executeWithExceptionLogging(() -> pictureService.updateById(picture));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(null);
  }

  /**
   * 审核图片
   *
   * @param pictureReviewRequest 图片审核请求
   * @param request request
   * @return true
   */
  @PostMapping("/review")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<Boolean> doPictureReview(
      @RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    pictureService.doPictureReview(pictureReviewRequest, loginUser);
    return ResultUtils.success(true);
  }

  /**
   * 根据id删除图片（管理员/用户）
   *
   * @param deleteRequest 删除请求
   * @param request request
   * @return void
   */
  @PostMapping("/delete")
  @AuthCheck
  public Response<Void> deletePictureById(DeleteRequest deleteRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(
        deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
        ErrorCode.PARAMS_ERROR);
    Long id = deleteRequest.getId();
    Picture picture = DatabaseUtils.executeWithExceptionLogging(() -> pictureService.getById(id));
    ThrowUtils.throwIf(picture == null, ErrorCode.FORBIDDEN_ERROR, "资源不存在，或无权操作");
    User loginUser = userService.getLoginUser(request);
    ThrowUtils.throwIf(
        !loginUser.getId().equals(picture.getUserId())
            && UserRole.ADMIN != UserRole.getEnumByValue(loginUser.getUserRole()),
        ErrorCode.FORBIDDEN_ERROR,
        "资源不存在，或无权操作");
    boolean result = DatabaseUtils.executeWithExceptionLogging(() -> pictureService.removeById(id));
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(null);
  }

  // todo 分类、标签完善
  @GetMapping("/category_tag")
  public Response<PictureCategoryTagVO> listPictureCategoryTag() {
    PictureCategoryTagVO pictureCategoryTagVO = new PictureCategoryTagVO();
    List<String> categoryList = Arrays.asList("背景", "资源", "头像");
    List<String> tagList = Arrays.asList("热门", "高清", "创意", "艺术", "生活");
    pictureCategoryTagVO.setCategoryList(categoryList);
    pictureCategoryTagVO.setTagList(tagList);
    return ResultUtils.success(pictureCategoryTagVO);
  }
}
