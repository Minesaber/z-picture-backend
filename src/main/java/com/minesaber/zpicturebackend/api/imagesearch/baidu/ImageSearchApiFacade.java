package com.minesaber.zpicturebackend.api.imagesearch.baidu;

import java.util.List;

import com.minesaber.zpicturebackend.api.imagesearch.baidu.model.ImageSearchResult;
import com.minesaber.zpicturebackend.api.imagesearch.baidu.sub.GetImageFirstUrlApi;
import com.minesaber.zpicturebackend.api.imagesearch.baidu.sub.GetImageListApi;
import com.minesaber.zpicturebackend.api.imagesearch.baidu.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

// todo 可以考虑使用bing图片搜索接口
// todo 不能接受webp
@Slf4j
public class ImageSearchApiFacade {
  public static List<ImageSearchResult> searchImage(String imageUrl) {
    String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
    String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
    List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
    return imageList;
  }
}
