package com.school.entity;

/**
 * 统一返回值类
 */
public class ResultVO {

    /**
     * 状态码：成功200/失败500
     */
    private int code;

    /**
     * 提示语
     */
    private String message;

    /**
     * 返回数据，无数据则为null/空值
     */
    private Object data;

    /**
     * 私有构造方法
     */
    private ResultVO() {
    }

    /**
     * 成功返回
     * 
     * @param message 提示语
     * @param data    返回数据
     * @return ResultVO
     */
    public static ResultVO success(String message, Object data) {
        ResultVO result = new ResultVO();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（无数据）
     * 
     * @param message 提示语
     * @return ResultVO
     */
    public static ResultVO success(String message) {
        return success(message, null);
    }

    /**
     * 失败返回
     * 
     * @param message 提示语
     * @param data    返回数据
     * @return ResultVO
     */
    public static ResultVO fail(String message, Object data) {
        ResultVO result = new ResultVO();
        result.setCode(500);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回（无数据）
     * 
     * @param message 提示语
     * @return ResultVO
     */
    public static ResultVO fail(String message) {
        return fail(message, null);
    }

    // getter and setter
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}