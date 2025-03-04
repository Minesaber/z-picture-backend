package com.minesaber.zpicturebackend.model.dto.base;

import java.io.Serializable;
import lombok.Data;

/** 通用分页请求 */
@Data
public class PageRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 当前页号 */
  private int current = 1;

  /** 页面大小 */
  private int pageSize = 10;

  /** 排序字段 */
  private String sortField;

  /** 排序顺序（默认升序） */
  private String sortOrder = "descend";
}
