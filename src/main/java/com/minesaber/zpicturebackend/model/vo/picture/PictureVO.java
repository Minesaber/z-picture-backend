package com.minesaber.zpicturebackend.model.vo.picture;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.minesaber.zpicturebackend.model.po.picture.Picture;
import com.minesaber.zpicturebackend.model.vo.user.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/** 图片视图 */
@Data
public class PictureVO implements Serializable {
  private static final long serialVersionUID = 6893035001788805957L;

  /** id */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 创建时间 */
  private Date createTime;

  /** 编辑时间 */
  private Date editTime;

  /** 更新时间 */
  private Date updateTime;

  /** url */
  private String url;

  /** 名称 */
  private String name;

  /** 创建用户id */
  private Long userId;

  /** 创建用户视图 */
  private UserVO userVO;

  /** 简介 */
  private String profile;

  /** 分类 */
  private String category;

  /** 标签 */
  private List<String> tags;

  /** 大小 */
  private Long picSize;

  /** 格式 */
  private String picFormat;

  /** 宽度 */
  private Integer picWidth;

  /** 高度 */
  private Integer picHeight;

  /** 宽高比 */
  private Double picScale;

  /**
   * 对象转视图
   *
   * @param picture 图片
   * @return 图片视图
   */
  public static PictureVO convertToVO(Picture picture) {
    PictureVO pictureVO = new PictureVO();
    BeanUtil.copyProperties(picture, pictureVO);
    // 转tags为列表，添加用户视图
    if (picture.getTags() != null)
      pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
    return pictureVO;
  }

  /**
   * 视图转对象
   *
   * @param pictureVO 图片视图
   * @return 图片
   */
  public static Picture convertToPO(PictureVO pictureVO) {
    Picture picture = Picture.builder().build();
    BeanUtil.copyProperties(pictureVO, picture);
    // 转tags为字符串
    if (pictureVO.getTags() != null) picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
    return picture;
  }
}
