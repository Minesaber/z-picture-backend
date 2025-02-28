package com.minesaber.zpicturebackend.api.ai.aliyun.helpers;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.minesaber.zpicturebackend.api.ai.aliyun.model.CreateOutPaintingTaskRequest;
import com.minesaber.zpicturebackend.api.ai.aliyun.model.CreateOutPaintingTaskResponse;
import com.minesaber.zpicturebackend.api.ai.aliyun.model.OutPaintingTaskResult;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.exception.BusinessException;
import com.minesaber.zpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PictureAIHelper {
  @Value("${dashscope-api-key}")
  private String apiKey;

  /** 扩图接口地址 */
  public static final String CREATE_OUT_PAINTING_TASK_URL =
      "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

  /** 获取任务结果接口地址 */
  public static final String GET_OUT_PAINTING_TASK_URL =
      "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

  /**
   * 创建任务，并接收创建任务的响应
   *
   * @param createOutPaintingTaskRequest 扩图任务创建请求
   * @return 创建扩图任务响应
   */
  public CreateOutPaintingTaskResponse createOutPaintingTask(
      CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
    if (createOutPaintingTaskRequest == null) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
    }
    HttpRequest httpRequest =
        HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
            .header("Authorization", "Bearer " + apiKey)
            .header("X-DashScope-Async", "enable")
            .header("Content-Type", "application/json")
            .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
    try (HttpResponse httpResponse = httpRequest.execute()) {
      ThrowUtils.throwIf(
          !httpResponse.isOk(), ErrorCode.OPERATION_ERROR, "AI 扩图失败" + httpResponse.body());
      CreateOutPaintingTaskResponse createOutPaintingTaskResponse =
          JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
      if (createOutPaintingTaskResponse.getCode() != null) {
        throw new BusinessException(
            ErrorCode.OPERATION_ERROR, "AI 扩图失败" + createOutPaintingTaskResponse.getMessage());
      }
      return createOutPaintingTaskResponse;
    }
  }

  /**
   * 查询任务执行结果
   *
   * @param taskId 任务 id
   * @return 扩图任务结果
   */
  public OutPaintingTaskResult getOutPaintingTask(String taskId) {
    if (StrUtil.isBlank(taskId)) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务id不能为空");
    }
    String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
    try (HttpResponse httpResponse =
        HttpRequest.get(url).header("Authorization", "Bearer " + apiKey).execute()) {
      ThrowUtils.throwIf(
          !httpResponse.isOk(), ErrorCode.OPERATION_ERROR, "获取任务结果失败" + httpResponse.body());
      return JSONUtil.toBean(httpResponse.body(), OutPaintingTaskResult.class);
    }
  }
}
