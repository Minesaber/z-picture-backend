package com.minesaber.zpicturebackend.api.ai.aliyun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 创建扩图任务响应 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutPaintingTaskResponse {
  private Output output;

  /** 创建任务的响应 */
  @Data
  public static class Output {
    /**
     * 任务状态（枚举值）
     *
     * <ul>
     *   <li>PENDING：排队中
     *   <li>RUNNING：处理中
     *   <li>SUSPENDED：挂起
     *   <li>SUCCEEDED：执行成功
     *   <li>FAILED：执行失败
     *   <li>UNKNOWN：任务不存在或状态未知
     * </ul>
     */
    private String taskStatus;

    /** 任务 ID */
    private String taskId;
  }

  /** 接口错误码，成功请求不会返回该参数 */
  private String code;

  /** 接口错误信息，成功请求不会返回该参数 */
  private String message;

  /** 请求唯一标识，可用于请求明细溯源和问题排查 */
  private String requestId;
}
