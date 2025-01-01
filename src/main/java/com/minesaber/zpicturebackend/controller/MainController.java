package com.minesaber.zpicturebackend.controller;

import com.minesaber.zpicturebackend.common.BaseResponse;
import com.minesaber.zpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {
  /**
   * 健康检查
   *
   * @return 检查响应
   */
  @GetMapping("/health")
  public BaseResponse<?> health() {
    return ResultUtils.success(null);
  }
}
