package com.printercloud.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一响应格式
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "统一响应格式")
public class R<T> {

    /**
     * 响应码
     */
    @Schema(description = "响应码", example = "200")
    private Integer code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳", example = "2024-12-07 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public R() {
        this.timestamp = LocalDateTime.now();
    }

    public R(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public R(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> R<T> success() {
        return new R<>(200, "操作成功");
    }

    public static <T> R<T> success(String message) {
        return new R<>(200, message);
    }

    public static <T> R<T> success(T data) {
        return new R<>(200, "操作成功", data);
    }

    public static <T> R<T> success(String message, T data) {
        return new R<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> R<T> error() {
        return new R<>(500, "操作失败");
    }

    public static <T> R<T> error(String message) {
        return new R<>(500, message);
    }

    public static <T> R<T> error(Integer code, String message) {
        return new R<>(code, message);
    }

    /**
     * 参数验证失败
     */
    public static <T> R<T> validateFailed() {
        return new R<>(400, "参数验证失败");
    }

    public static <T> R<T> validateFailed(String message) {
        return new R<>(400, message);
    }

    /**
     * 请求错误
     */
    public static <T> R<T> badRequest(String message) {
        return new R<>(400, message);
    }

    /**
     * 未授权
     */
    public static <T> R<T> unauthorized() {
        return new R<>(401, "未授权");
    }

    public static <T> R<T> unauthorized(String message) {
        return new R<>(401, message);
    }

    /**
     * 禁止访问
     */
    public static <T> R<T> forbidden() {
        return new R<>(403, "禁止访问");
    }

    public static <T> R<T> forbidden(String message) {
        return new R<>(403, message);
    }

    /**
     * 资源不存在
     */
    public static <T> R<T> notFound() {
        return new R<>(404, "资源不存在");
    }

    public static <T> R<T> notFound(String message) {
        return new R<>(404, message);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}
