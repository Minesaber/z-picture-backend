package com.minesaber.zpicturebackend.api.imagesearch.baidu.sub;

import com.minesaber.zpicturebackend.enums.ErrorCode;
import com.minesaber.zpicturebackend.exception.BusinessException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** 获取图片列表接口的 Api（Step 2） */
@Slf4j
public class GetImageFirstUrlApi {
  public static String getImageFirstUrl(String url) {
    try {
      Document document = Jsoup.connect(url).timeout(5000).get();
      Elements scriptElements = document.getElementsByTag("script");
      for (Element script : scriptElements) {
        String scriptContent = script.html();
        if (scriptContent.contains("\"firstUrl\"")) {
          Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
          Matcher matcher = pattern.matcher(scriptContent);
          if (matcher.find()) {
            String firstUrl = matcher.group(1);
            firstUrl = firstUrl.replace("\\/", "/");
            return firstUrl;
          }
        }
      }
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到 url");
    } catch (Exception e) {
      log.error("搜索失败", e);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
    }
  }
}
