package com.minesaber.zpicturebackend.aop;

import com.minesaber.zpicturebackend.helpers.OssHelper;
import com.minesaber.zpicturebackend.model.entity.picture.Picture;
import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.model.vo.picture.PictureVO;
import com.minesaber.zpicturebackend.api.ai.aliyun.model.OutPaintingTaskResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Resource;
import java.util.List;

/** 对返回的图片地址进行处理，便于公有读/私有读的快速切换 */
@ControllerAdvice
public class PictureUrlAdvice implements ResponseBodyAdvice<Object> {

  @Resource private OssHelper ossHelper;

  /** 默认预签名 URL 过期时间为 5分钟 */
  private final long DEFAULT_EXPIRATION = 5 * 60 * 1000L;

  /**
   * 确定哪些Controller方法的返回值被处理，此处对所有返回值进行拦截，实际可根据包名或注解进行更精确控制
   *
   * @param returnType 方法的返回值类型
   * @param converterType 使用的消息转换器类型
   * @return true 表示需要处理，false 表示不需要处理
   */
  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    // 仅处理返回类型为 Response 的情况
    return returnType.getParameterType().equals(Response.class);
  }

  /**
   * 在响应数据写入HTTP响应体之前被调用，对返回的数据进行处理
   *
   * @param body Controller方法返回的原始数据
   * @param returnType Controller方法的返回值类型
   * @param selectedContentType 响应的媒体类型（例如application/json）
   * @param selectedConverterType
   *     负责序列化数据的HttpMessageConverter类型（例如MappingJackson2HttpMessageConverter实例）
   * @param request 当前请求的封装对象
   * @param response 当前响应的封装对象
   * @return 处理后的响应数据
   */
  @Override
  public Object beforeBodyWrite(
      Object body,
      @NonNull MethodParameter returnType,
      @NonNull MediaType selectedContentType,
      @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
      @NonNull ServerHttpRequest request,
      @NonNull ServerHttpResponse response) {
    if (body == null) {
      return null;
    }

    if (body instanceof Response) {
      Response<?> res = (Response<?>) body;
      Object data = res.getData();
      if (data != null) {
        processData(data);
      }
    }

    return body;
  }

  /**
   * 根据对象类型替换其中的 URL 字段
   *
   * @param data 需要处理的数据对象
   */
  private void processData(Object data) {
    if (data instanceof Picture) {
      processPicture((Picture) data);
    } else if (data instanceof PictureVO) {
      processPictureVO((PictureVO) data);
    } else if (data instanceof OutPaintingTaskResult) {
      processOutPaintingTaskResult((OutPaintingTaskResult) data);
    } else if (data instanceof Page) {
      Page<?> page = (Page<?>) data;
      page.getRecords().forEach(this::processData);
    } else if (data instanceof List) {
      List<?> list = (List<?>) data;
      list.forEach(this::processData);
    }
  }

  /**
   * 处理 Picture 实体中的 URL 字段
   *
   * @param picture Picture 实体
   */
  private void processPicture(Picture picture) {
    if (picture.getUrl() != null) {
      picture.setUrl(
          ossHelper.genPresignedUrl(ossHelper.getKeyFromUrl(picture.getUrl()), DEFAULT_EXPIRATION));
    }
    if (picture.getThumbnailUrl() != null) {
      picture.setThumbnailUrl(
          ossHelper.genPresignedUrl(
              ossHelper.getKeyFromUrl(picture.getThumbnailUrl()), DEFAULT_EXPIRATION));
    }
  }

  /**
   * 处理 PictureVO 视图对象中的 URL 字段
   *
   * @param pictureVO 视图对象
   */
  private void processPictureVO(PictureVO pictureVO) {
    if (pictureVO.getUrl() != null) {
      pictureVO.setUrl(
          ossHelper.genPresignedUrl(
              ossHelper.getKeyFromUrl(pictureVO.getUrl()), DEFAULT_EXPIRATION));
    }
    if (pictureVO.getThumbnailUrl() != null) {
      pictureVO.setThumbnailUrl(
          ossHelper.genPresignedUrl(
              ossHelper.getKeyFromUrl(pictureVO.getThumbnailUrl()), DEFAULT_EXPIRATION));
    }
  }

  /**
   * 针对扩图任务结果中输出图片URL的处理
   *
   * @param taskResult 扩图任务结果
   */
  private void processOutPaintingTaskResult(OutPaintingTaskResult taskResult) {
    if (taskResult.getOutput() != null && taskResult.getOutput().getOutputImageUrl() != null) {
      taskResult
          .getOutput()
          .setOutputImageUrl(
              ossHelper.genPresignedUrl(
                  ossHelper.getKeyFromUrl(taskResult.getOutput().getOutputImageUrl()),
                  DEFAULT_EXPIRATION));
    }
  }
}
