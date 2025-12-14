package com.fuzz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 参数敏感数据库模糊测试平台后端应用启动类
 */
@SpringBootApplication
public class FuzzBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FuzzBackendApplication.class, args);
    }
}