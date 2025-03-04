package com.minesaber.zpicturebackend.helpers.upload;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import com.minesaber.zpicturebackend.constants.FileConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** 图片文件上传工具 */
@Component
public class PictureUploadHelper extends FileUploadTemplate {
  @Override
  protected void validPicture(Object inputSource) {
    MultipartFile multipartFile = (MultipartFile) inputSource;
    String originalFilename = multipartFile.getOriginalFilename();
    String prefix = FileUtil.getPrefix(originalFilename);
    ThrowUtils.throwIf(prefix == null, ErrorCode.PARAMS_ERROR, "文件名不能为空");
    ThrowUtils.throwIf(
        prefix.length() > FileConstant.FILE_NAME_MAX_LENGTH, ErrorCode.PARAMS_ERROR, "文件名称过长");
    // 校验大小
    long size = multipartFile.getSize();
    int maxSize = FileConstant.FILE_MAX_SIZE;
    ThrowUtils.throwIf(
        size > maxSize * FileConstant.MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过" + maxSize + "MB");
    // 校验后缀与魔数
    String suffix = FileUtil.getSuffix(originalFilename);
    ThrowUtils.throwIf(
        !FileConstant.ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "图片类型不支持");
    try (InputStream inputStream = multipartFile.getInputStream()) {
      String type = FileTypeUtil.getType(inputStream);
      ThrowUtils.throwIf(
          !FileConstant.ALLOW_FORMAT_LIST.contains(type), ErrorCode.PARAMS_ERROR, "文件类型错误");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  protected String getOriginalFilename(Object inputSource) {
    MultipartFile multipartFile = (MultipartFile) inputSource;
    return multipartFile.getOriginalFilename();
  }

  @Override
  protected InputStream getInputStream(Object inputSource) {
    MultipartFile multipartFile = (MultipartFile) inputSource;
    try {
      return multipartFile.getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
