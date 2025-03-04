package com.minesaber.zpicturebackend.model.dto.base;

import java.io.Serializable;
import lombok.Data;

@Data
public class DeleteRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  /** id */
  private Long id;
}
