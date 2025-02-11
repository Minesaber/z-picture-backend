package com.minesaber.zpicturebackend.controller;

import com.minesaber.zpicturebackend.model.vo.base.Response;
import com.minesaber.zpicturebackend.utils.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 允许请求跨域注解示例
// @CrossOrigin(origins = {"http://localhost1280"},allowCredentials = "true")
@RestController
@RequestMapping("/")
public class MainController {
  /**
   * 健康检查
   *
   * @return 检查响应
   */
  @GetMapping("/health")
  public Response<?> health() {
    return ResultUtils.success(null);
  }
}
