package com.minesaber.zpicturebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minesaber.zpicturebackend.model.entity.picture.Picture;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PictureMapper extends BaseMapper<Picture> {
  List<Picture> selectPictures(
      @Param("spaceId") Long spaceId, @Param("pictureIds") List<Long> pictureIds);
}
