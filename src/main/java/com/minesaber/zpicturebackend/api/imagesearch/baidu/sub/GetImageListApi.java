package com.minesaber.zpicturebackend.api.imagesearch.baidu.sub;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.minesaber.zpicturebackend.api.imagesearch.baidu.model.ImageSearchResult;
import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.exception.BusinessException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** 获取图片列表（step 3） */
@Slf4j
public class GetImageListApi {
  public static List<ImageSearchResult> getImageList(String url) {
    try {
      HttpResponse response = HttpUtil.createGet(url).execute();
      int statusCode = response.getStatus();
      String body = response.body();
      if (statusCode == 200) {
        return processResponse(body);
      } else {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
      }
    } catch (Exception e) {
      log.error("获取图片列表失败", e);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
    }
  }

  /**
   * 处理接口响应内容
   *
   * @param responseBody 接口返回的JSON字符串
   */
  private static List<ImageSearchResult> processResponse(String responseBody) {
    JSONObject jsonObject = new JSONObject(responseBody);
    if (!jsonObject.containsKey("data")) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
    }
    JSONObject data = jsonObject.getJSONObject("data");
    if (!data.containsKey("list")) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
    }
    JSONArray list = data.getJSONArray("list");
    return JSONUtil.toList(list, ImageSearchResult.class);
  }
}
