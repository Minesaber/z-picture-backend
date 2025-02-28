package com.minesaber.zpicturebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minesaber.zpicturebackend.model.entity.picture.Picture;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PictureMapper extends BaseMapper<Picture> {
  List<Picture> selectPictures(
      @Param("spaceId") Long spaceId, @Param("pictureIds") List<Long> pictureIds);
}
