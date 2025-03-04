package com.minesaber.zpicturebackend.helpers.upload;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.*;
import com.minesaber.zpicturebackend.constants.FileConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.exception.BusinessException;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** URL文件上传工具 */
@Component
public class UrlUploadHelper extends FileUploadTemplate {
  private final ThreadLocal<String> pictureType = new ThreadLocal<>();

  @Override
  protected void validPicture(Object inputSource) {
    String fileUrl = (String) inputSource;
    // 基本检查
    ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");
    // 检查 URL 格式、协议
    try {
      new URL(fileUrl);
    } catch (MalformedURLException e) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
    }
    ThrowUtils.throwIf(
        !fileUrl.startsWith("https://") && !fileUrl.startsWith("http://"),
        ErrorCode.PARAMS_ERROR,
        "仅支持 HTTP 或 HTTPS 协议的文件地址");
    // 发送 HEAD 尝试检查图片是否存在
    try (HttpResponse httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()) {
      if (!httpResponse.isOk()) {
        ThrowUtils.throwIf(
            httpResponse.getStatus() == 302, ErrorCode.PARAMS_ERROR, "请确保使用的是纯图片地址而非页面地址，并保证格式正确");
        return;
      }
      // 校验文件格式
      String contentType = httpResponse.header("Content-Type");
      if (StrUtil.isNotBlank(contentType) && contentType.startsWith("image/")) {
        String type = StrUtil.subAfter(contentType, '/', true);
        pictureType.set(type);
      }
      List<String> mimeTypes =
          FileConstant.ALLOW_FORMAT_LIST.stream()
              .map(allowType -> "image/" + allowType)
              .collect(Collectors.toList());
      if (StrUtil.isNotBlank(contentType)) {
        ThrowUtils.throwIf(!mimeTypes.contains(contentType), ErrorCode.PARAMS_ERROR, "文件类型不支持");
      }
      // 检查文件大小
      String contentLength = httpResponse.header("Content-Length");
      if (contentLength != null) {
        long length;
        try {
          length = Long.parseLong(contentLength);
        } catch (NumberFormatException e) {
          throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小异常");
        }
        int maxSize = FileConstant.FILE_MAX_SIZE;
        ThrowUtils.throwIf(
            length > maxSize * FileConstant.MB,
            ErrorCode.PARAMS_ERROR,
            "文件大小不能超过" + maxSize + "MB");
      }
    }
  }

  @Override
  protected String getOriginalFilename(Object inputSource) {
    String fileUrl = (String) inputSource;
    String type =
        pictureType.get() != null
            ? pictureType.get()
            : StrUtil.subAfter(StrUtil.subBefore(fileUrl, "?", false), '.', true);
    return StrUtil.subBefore(StrUtil.subBefore(fileUrl, "?", false), '.', true) + "." + type;
  }

  @Override
  protected InputStream getInputStream(Object inputSource) {
    String fileUrl = (String) inputSource;
    try {
      return URLUtil.getStream(new URL(fileUrl));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
