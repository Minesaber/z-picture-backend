package com.minesaber.zpicturebackend.model.dto.base;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {
  private static final long serialVersionUID = -6282841710622035992L;

  /** id */
  private Long id;
}
