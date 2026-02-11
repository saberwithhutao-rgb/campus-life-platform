package com.school.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应体类
 */
public class Result {
    private boolean success;
    private int code;
    private String message;
    private Map<String, Object> data;

    // 构造方法
    private Result() {
        this.data = new HashMap<>();
    }

    // 成功响应
    public static Result success() {
        Result result = new Result();
        result.setSuccess(true);
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    // 失败响应
    public static Result error(String message) {
        Result result = new Result();
        result.setSuccess(false);
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    // 添加数据
    public Result data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    // 添加多个数据
    public Result data(Map<String, Object> data) {
        this.data.putAll(data);
        return this;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
