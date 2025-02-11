package com.minesaber.zpicturebackend.model.dto.base;

import lombok.Data;

import java.io.Serializable;

/** 通用分页请求类 */
@Data
public class PageRequest implements Serializable {
  private static final long serialVersionUID = -3190937833240379087L;

  /** 当前页号 */
  private int current = 1;

  /** 页面大小 */
  private int pageSize = 10;

  /** 排序字段 */
  private String sortField;

  /** 排序顺序（默认升序） */
  private String sortOrder = "descend";
}
