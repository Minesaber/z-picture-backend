package com.minesaber.zpicturebackend.exception;

import com.minesaber.zpicturebackend.common.BaseResponse;
import com.minesaber.zpicturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局异常处理器 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  public BaseResponse<?> businessException(BusinessException businessException) {
    log.error("BusinessException", businessException);
    return ResultUtils.error(businessException.getCode(), businessException.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  public BaseResponse<?> businessException(RuntimeException runtimeException) {
    log.error("RuntimeException", runtimeException);
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
  }
}
