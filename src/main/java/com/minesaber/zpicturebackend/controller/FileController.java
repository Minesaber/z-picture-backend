package com.minesaber.zpicturebackend.controller;

import com.aliyun.core.utils.IOUtils;
import com.aliyun.oss.model.OSSObject;
import com.minesaber.zpicturebackend.aop.annotation.AuthCheck;
import com.minesaber.zpicturebackend.constants.UserConstant;
import com.minesaber.zpicturebackend.helpers.OssHelper;
import com.minesaber.zpicturebackend.helpers.upload.PictureUploadHelper;
import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.utils.ResultUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Slf4j
@Deprecated
public class FileController {
  /** 基础操作直接使用OSS 工具即可 */
  @Resource private OssHelper ossHelper;

  /** 其他操作使用文件工具完成 */
  @Resource private PictureUploadHelper pictureUploadHelper;

  /**
   * 测试：上传文件 使用缓存文件的方式
   *
   * @param multipartFile 文件
   * @return 唯一键
   */
  @PostMapping("/test/upload")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public Response<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
    String name = multipartFile.getOriginalFilename();
    String key = String.format("test/%s", name);
    File file;
    try {
      // 不指定suffix，默认为tmp，但因为后续使用key来确定OSS文件名，所以不影响使用
      file = File.createTempFile(key, null);
      multipartFile.transferTo(file);
    } catch (IOException ioe) {
      log.error("缓存文件失败，key={}", key, ioe);
      throw new RuntimeException(ioe);
    }
    ossHelper.putObject(key, file);
    boolean delete = file.delete();
    if (!delete) {
      log.error("缓存删除失败，key={}", key);
    }
    return ResultUtils.success(key);
  }

  /**
   * 测试：下载文件
   *
   * @param key 唯一键
   * @param response 响应对象
   */
  @GetMapping("/test/download")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public void testDownloadFile(String key, HttpServletResponse response) {
    // 获取输入流
    OSSObject ossObject = ossHelper.getObject(key, null);
    InputStream objectInputStream = ossObject.getObjectContent();
    // 设置响应头
    response.setContentType("application/octet-stream;charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment;filename=" + key);
    try (objectInputStream) {
      // 写入响应
      byte[] bytes = IOUtils.toByteArray(objectInputStream);
      response.getOutputStream().write(bytes);
      response.getOutputStream().flush();
    } catch (IOException e) {
      log.error("写入响应失败，key={}", key);
      throw new RuntimeException(e);
    }
  }
}
