package com.minesaber.zpicturebackend.model.vo.space;

import java.io.Serializable;
import java.util.Date;

import com.minesaber.zpicturebackend.model.entity.space.Space;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/** 空间视图 */
@Data
public class SpaceVO implements Serializable {
  private static final long serialVersionUID = 1L;

  /** id */
  private Long id;

  /** 创建时间 */
  private Date createTime;

  /** 编辑时间 */
  private Date editTime;

  /** 更新时间 */
  private Date updateTime;

  /** 空间名称 */
  private String spaceName;

  /** 创建用户 id */
  private Long userId;

  /** 空间级别：0-普通版 1-专业版 2-旗舰版 */
  private Integer spaceLevel;

  /** 空间图片的最大总大小 */
  private Long maxSize;

  /** 空间图片的最大数量 */
  private Long maxCount;

  /** 当前空间下图片的总大小 */
  private Long totalSize;

  /** 当前空间下的图片数量 */
  private Long totalCount;

  /** 补充：创建用户信息 */
  private UserVO userVO;

  /**
   * 封装类转对象
   *
   * @param spaceVO 空间视图
   * @return 空间对象
   */
  public static Space convertToEntity(SpaceVO spaceVO) {
    if (spaceVO == null) {
      return null;
    }
    Space space = new Space();
    BeanUtils.copyProperties(spaceVO, space);
    return space;
  }

  /**
   * 对象转封装类
   *
   * @param space 空间对象
   * @return 空间视图
   */
  public static SpaceVO convertToVO(Space space) {
    if (space == null) {
      return null;
    }
    SpaceVO spaceVO = new SpaceVO();
    BeanUtils.copyProperties(space, spaceVO);
    return spaceVO;
  }
}
