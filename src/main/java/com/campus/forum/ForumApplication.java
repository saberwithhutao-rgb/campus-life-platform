package com.campus.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class ForumApplication {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        SpringApplication app = new SpringApplication(ForumApplication.class);
        Environment env = app.run(args).getEnvironment();

        long endTime = System.currentTimeMillis();
        long startupTime = endTime - startTime;

        log.info("================================================");
        log.info("  校园生活服务平台 启动成功！");
        log.info("================================================");
        log.info("  启动耗时: {} 毫秒 ({} 秒)", startupTime, startupTime / 1000.0);
        log.info("  服务端口: {}", env.getProperty("server.port"));
        log.info("  当前时间: {}", new java.util.Date());
        log.info("================================================");
    }
}