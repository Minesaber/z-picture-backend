package com.minesaber.zpicturebackend.api.ai.aliyun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 扩图任务结果 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutPaintingTaskResult {
  /** 请求唯一标识 */
  private String requestId;

  /** 输出信息 */
  private Output output;

  /** 表示任务的输出信息 */
  @Data
  public static class Output {

    /** 任务 ID */
    private String taskId;

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

    /** 任务指标信息 */
    private TaskMetrics taskMetrics;

    /** 提交时间 格式：YYYY-MM-DD HH:mm:ss.SSS */
    private String submitTime;

    /** 调度时间 格式：YYYY-MM-DD HH:mm:ss.SSS */
    private String scheduledTime;

    /** 结束时间 格式：YYYY-MM-DD HH:mm:ss.SSS */
    private String endTime;

    /** 输出图像的 URL */
    private String outputImageUrl;

    /** 接口错误码，成功请求不会返回该参数 */
    private String code;

    /** 接口错误信息，成功请求不会返回该参数 */
    private String message;
  }

  /** 表示任务的统计信息 */
  @Data
  public static class TaskMetrics {
    /** 总任务数 */
    private Integer total;

    /** 成功任务数 */
    private Integer succeeded;

    /** 失败任务数 */
    private Integer failed;
  }
}
