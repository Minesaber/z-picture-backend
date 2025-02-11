package com.minesaber.zpicturebackend.helpers;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.minesaber.zpicturebackend.config.SystemConfig;
import com.minesaber.zpicturebackend.constant.FileConstant;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import com.minesaber.zpicturebackend.model.bo.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/** 文件工具 */
@Component
@Slf4j
public class FileHelper {
  @Resource private SystemConfig systemConfig;
  @Resource private OssHelper ossHelper;

  /**
   * 上传图片
   *
   * @param pathPrefix 路径前缀，不包含结尾的/
   * @param multipartFile 文件
   * @return 上传图片结果
   */
  public UploadPictureResult uploadPicture(String pathPrefix, MultipartFile multipartFile) {
    // 检查参数
    validPicture(multipartFile);
    // 确定key
    String originalFilename = multipartFile.getOriginalFilename();
    String uuid = RandomUtil.randomString(FileConstant.UUID_LENGTH);
    String key =
        String.format(
            "%s/%s_%s.%s",
            pathPrefix,
            DateUtil.formatDate(new Date()),
            uuid,
            FileUtil.getSuffix(originalFilename));
    try (InputStream inputStream = multipartFile.getInputStream()) {
      // 缓存流
      byte[] bytes = IoUtil.readBytes(inputStream);
      // 上传图片
      ossHelper.putObject(key, new ByteArrayInputStream(bytes));
      // 图片信息
      String url = ossHelper.getBaseURL() + "/" + key;
      String name = FileUtil.getPrefix(originalFilename);
      /*
      以下代码使用OSS服务自动解析图片属性
      OSSObject ossObject = ossHelper.getPictureInfo(key);
      InputStream contentStream = ossObject.getObjectContent();
      JSONObject jsonObject = JSONUtil.parseObj(IoUtil.readUtf8(contentStream));
      // 1、size
      Long size = jsonObject.getJSONObject("FileSize").getLong("value");
      // 2、format
      String format = jsonObject.getJSONObject("Format").getStr("value");
      // 3、width
      Integer width = jsonObject.getJSONObject("ImageWidth").getInt("value");
      // 4、height
      Integer height = jsonObject.getJSONObject("ImageHeight").getInt("value");
      // 5、scale
      Double scale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
      */
      try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
        BufferedImage bufferedImage = ImgUtil.read(byteArrayInputStream);
        // 1、size
        long size = bytes.length;
        // 2、format
        String format = FileUtil.getSuffix(originalFilename);
        // 3、width
        int width = bufferedImage.getWidth();
        // 4、height
        int height = bufferedImage.getHeight();
        // 5、scale
        Double scale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
        return UploadPictureResult.builder()
            .url(url)
            .name(name)
            .picSize(size)
            .picFormat(format)
            .picWidth(width)
            .picHeight(height)
            .picScale(scale)
            .build();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 检查图片
   *
   * @param multipartFile 图片
   */
  private void validPicture(MultipartFile multipartFile) {
    String prefix = FileUtil.getPrefix(multipartFile.getOriginalFilename());
    ThrowUtils.throwIf(prefix == null, ErrorCode.PARAMS_ERROR, "文件名不能为空");
    ThrowUtils.throwIf(
        prefix.length() > FileConstant.FILE_NAME_MAX_LENGTH, ErrorCode.PARAMS_ERROR, "文件名称过长");
    // 校验大小
    long size = multipartFile.getSize();
    int maxSize = systemConfig.getMaxSize();
    ThrowUtils.throwIf(
        size > maxSize * FileConstant.MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过" + maxSize + "MB");
    // 校验后缀与魔数
    String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
    ThrowUtils.throwIf(
        !FileConstant.ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "图片类型不支持");
    try {
      InputStream inputStream = multipartFile.getInputStream();
      String type = FileTypeUtil.getType(inputStream);
      ThrowUtils.throwIf(
          !FileConstant.ALLOW_FORMAT_LIST.contains(type), ErrorCode.PARAMS_ERROR, "文件类型错误");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
