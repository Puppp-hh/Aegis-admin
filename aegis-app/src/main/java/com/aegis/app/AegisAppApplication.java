package com.aegis.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aegis")
@MapperScan("com.aegis.system.mapper")
public class AegisAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AegisAppApplication.class, args);
    }
}