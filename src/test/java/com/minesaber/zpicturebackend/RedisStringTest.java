package com.minesaber.zpicturebackend;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisStringTest {

  @Autowired private StringRedisTemplate stringRedisTemplate;

  @Test
  public void testRedisStringOperations() {
    // 获取操作对象
    ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();

    // Key 和 Value
    String key = "testKey";
    String value = "testValue";

    // 测试新增（修改）、查询
    valueOps.set(key, value);
    String storedValue = valueOps.get(key);
    assertEquals(value, storedValue, "存储的值与预期不一致");

    // 测试删除
    stringRedisTemplate.delete(key);
    storedValue = valueOps.get(key);
    assertNull(storedValue, "删除后的值不为空");
  }
}
