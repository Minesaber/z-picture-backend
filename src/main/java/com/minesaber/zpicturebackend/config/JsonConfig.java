package com.minesaber.zpicturebackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/** SpringMVC JSON 配置 */
@JsonComponent
public class JsonConfig {
  /**
   * 添加Long转JSON精度丢失的配置
   *
   * @param builder builder
   * @return objectMapper
   */
  @Bean
  public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
    SimpleModule module = new SimpleModule();
    module.addSerializer(Long.class, ToStringSerializer.instance);
    module.addSerializer(Long.TYPE, ToStringSerializer.instance);
    ObjectMapper objectMapper = builder.createXmlMapper(false).build();
    objectMapper.registerModule(module);
    return objectMapper;
  }
}
