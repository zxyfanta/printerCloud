package com.printercloud.common;

import java.io.Serializable;

/**
 * 统一API响应结果封装
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 * @param <T> 数据类型
 */
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    private R() {
    }

    /**
     * 成功返回结果
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功返回结果
     *
     * @param data 返回数据
     */
    public static <T> R<T> ok(T data) {
        return ok(data, "操作成功");
    }

    /**
     * 成功返回结果
     *
     * @param data 返回数据
     * @param message 返回消息
     */
    public static <T> R<T> ok(T data, String message) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setSuccess(true);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /**
     * 失败返回结果
     */
    public static <T> R<T> fail() {
        return fail("操作失败");
    }

    /**
     * 失败返回结果
     *
     * @param message 错误消息
     */
    public static <T> R<T> fail(String message) {
        return fail(message, 500);
    }

    /**
     * 失败返回结果
     *
     * @param message 错误消息
     * @param code 错误码
     */
    public static <T> R<T> fail(String message, Integer code) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> R<T> validateFailed() {
        return validateFailed("参数验证失败");
    }

    /**
     * 参数验证失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> R<T> validateFailed(String message) {
        R<T> r = new R<>();
        r.setCode(400);
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }

    /**
     * 未登录返回结果
     */
    public static <T> R<T> unauthorized() {
        return unauthorized("暂未登录或token已经过期");
    }

    /**
     * 未登录返回结果
     *
     * @param message 提示信息
     */
    public static <T> R<T> unauthorized(String message) {
        R<T> r = new R<>();
        r.setCode(401);
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }

    /**
     * 未授权返回结果
     */
    public static <T> R<T> forbidden() {
        return forbidden("没有相关权限");
    }

    /**
     * 未授权返回结果
     *
     * @param message 提示信息
     */
    public static <T> R<T> forbidden(String message) {
        R<T> r = new R<>();
        r.setCode(403);
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }

    /**
     * 资源不存在返回结果
     */
    public static <T> R<T> notFound() {
        return notFound("资源不存在");
    }

    /**
     * 资源不存在返回结果
     *
     * @param message 提示信息
     */
    public static <T> R<T> notFound(String message) {
        R<T> r = new R<>();
        r.setCode(404);
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.success);
    }

    /**
     * 判断是否失败
     */
    public boolean isFail() {
        return !isSuccess();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}