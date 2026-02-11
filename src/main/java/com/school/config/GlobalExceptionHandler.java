package com.school.config;

import com.school.entity.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;

/**
 * 全局异常处理类
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e) {
        // 打印详细的错误日志
        System.err.println("全局异常捕获:");
        e.printStackTrace();
        
        // 构建统一的错误响应，确保包含suggestions空数组
        Result errorResult = Result.error("系统繁忙，请稍后重试")
                .data("suggestions", new ArrayList<>());
        
        // 返回200状态码
        return ResponseEntity.ok(errorResult);
    }
}
