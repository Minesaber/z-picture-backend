package com.minesaber.zpicturebackend.api.imagesearch.baidu.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

/** 获取以图搜图页面地址（step 1） */
@Slf4j
public class GetImagePageUrlApi {
  public static String getImagePageUrl(String imageUrl) {
    Map<String, Object> formData = new HashMap<>();
    formData.put("image", imageUrl);
    formData.put("tn", "pc");
    formData.put("from", "pc");
    formData.put("image_source", "PC_UPLOAD_URL");
    String url = "https://graph.baidu.com/upload?uptime=" + System.currentTimeMillis();
    try {
      String body;
      try (HttpResponse httpResponse =
          HttpRequest.post(url).form(formData).header("Acs-Token", "").timeout(5000).execute()) {
        if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
          throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
        }
        body = httpResponse.body();
      }
      Map<String, Object> result = JSONUtil.toBean(body, Map.class);
      if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
      }
      Map<String, Object> data = (Map<String, Object>) result.get("data");
      String rawUrl = (String) data.get("url");
      String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
      if (StrUtil.isBlank(searchResultUrl)) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效的结果地址");
      }
      return searchResultUrl;
    } catch (Exception e) {
      log.error("调用百度以图搜图接口失败", e);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
    }
  }
}
