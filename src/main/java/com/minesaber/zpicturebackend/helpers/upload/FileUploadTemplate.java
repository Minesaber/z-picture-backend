package com.minesaber.zpicturebackend.helpers.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.minesaber.zpicturebackend.config.SystemConfig;
import com.minesaber.zpicturebackend.constants.FileConstant;
import com.minesaber.zpicturebackend.helpers.OssHelper;
import com.minesaber.zpicturebackend.model.bo.picture.UploadPictureResult;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/** 文件工具模板 */
@Slf4j
public abstract class FileUploadTemplate {
  @Resource private SystemConfig systemConfig;
  @Resource private OssHelper ossHelper;

  /**
   * 检查图片相关参数
   *
   * @param inputSource 输入源
   */
  protected abstract void validPicture(Object inputSource);

  /**
   * 获取输入源的文件名
   *
   * @param inputSource 输入源
   * @return 文件名
   */
  protected abstract String getOriginalFilename(Object inputSource);

  /**
   * 获取输入源对应文件流
   *
   * @param inputSource 输入源
   * @return 文件流
   */
  protected abstract InputStream getInputStream(Object inputSource);

  /**
   * 上传图片
   *
   * @param pathPrefix 路径前缀，不包含结尾的/
   * @param inputSource 文件
   * @return 上传图片结果
   */
  public UploadPictureResult uploadPicture(String pathPrefix, Object inputSource) {
    // 1、检查参数
    validPicture(inputSource);
    // 2、确定不同情况下的key
    String originalFilename = getOriginalFilename(inputSource);
    String uuid = RandomUtil.randomString(FileConstant.UUID_LENGTH);
    String key =
        String.format(
            "%s/%s_%s.%s",
            pathPrefix,
            DateUtil.formatDate(new Date()),
            uuid,
            FileUtil.getSuffix(originalFilename));
    // 3、处理不同文件源
    try (InputStream inputStream = getInputStream(inputSource)) {
      // 缓存流
      byte[] bytes = inputStream.readAllBytes();
      // 上传图片
      ossHelper.putObject(key, new ByteArrayInputStream(bytes));
      return getUploadPictureResult(key, originalFilename, bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 封装图片上传结果
   *
   * @param key 唯一键
   * @param originalFilename 文件原始名
   * @param bytes 文件字节数组
   * @return 文件上传结果
   */
  private UploadPictureResult getUploadPictureResult(
      String key, String originalFilename, byte[] bytes) {
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
