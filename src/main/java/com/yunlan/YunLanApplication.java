package com.yunlan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yunlan.mapper")
public class YunLanApplication {
    public static void main(String[] args) {
        SpringApplication.run(YunLanApplication.class, args);
    }
}
