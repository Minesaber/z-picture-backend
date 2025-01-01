package com.minesaber.zpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.minesaber.zpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class ZPictureBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZPictureBackendApplication.class, args);
  }
}
