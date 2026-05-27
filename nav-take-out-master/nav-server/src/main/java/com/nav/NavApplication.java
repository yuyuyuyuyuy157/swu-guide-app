package com.nav;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("com.nav.mapper") // 痛点驱动：让 Spring 自动扫描并管理未来编写的 Mapper 接口
public class NavApplication {

    public static void main(String[] args) {
        SpringApplication.run(NavApplication.class, args);
        log.info("==================================================");
        log.info("🚀 校园导览系统 (nav-take-out) 基础服务空载点火启动成功！");
        log.info("==================================================");
    }
}