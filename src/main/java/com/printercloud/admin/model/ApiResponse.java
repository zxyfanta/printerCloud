package com.printercloud.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API响应模型
 */
public class ApiResponse<T> {
    
    private Integer code;
    private String message;
    private T data;
    
    @JsonProperty("timestamp")
    private Long timestamp;

    // 构造函数
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ApiResponse(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    // 静态工厂方法
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功");
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message);
    }

    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(401, "未授权访问");
    }

    public static <T> ApiResponse<T> forbidden() {
        return new ApiResponse<>(403, "访问被禁止");
    }

    public static <T> ApiResponse<T> notFound() {
        return new ApiResponse<>(404, "资源不存在");
    }

    // Getters and Setters
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    // 便利方法
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    public boolean isError() {
        return !isSuccess();
    }

    public boolean isUnauthorized() {
        return code != null && code == 401;
    }

    public boolean isForbidden() {
        return code != null && code == 403;
    }

    public boolean isNotFound() {
        return code != null && code == 404;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}

/**
 * 分页结果模型
 */
class PagedResult<T> {
    
    @JsonProperty("content")
    private java.util.List<T> content;
    
    @JsonProperty("totalElements")
    private Long totalElements;
    
    @JsonProperty("totalPages")
    private Integer totalPages;
    
    @JsonProperty("size")
    private Integer size;
    
    @JsonProperty("number")
    private Integer number;
    
    @JsonProperty("first")
    private Boolean first;
    
    @JsonProperty("last")
    private Boolean last;
    
    @JsonProperty("empty")
    private Boolean empty;

    // 构造函数
    public PagedResult() {}

    public PagedResult(java.util.List<T> content, Long totalElements, Integer totalPages, Integer size, Integer number) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.size = size;
        this.number = number;
        this.first = number == 0;
        this.last = number == totalPages - 1;
        this.empty = content == null || content.isEmpty();
    }

    // Getters and Setters
    public java.util.List<T> getContent() { return content; }
    public void setContent(java.util.List<T> content) { this.content = content; }

    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }

    public Boolean getFirst() { return first; }
    public void setFirst(Boolean first) { this.first = first; }

    public Boolean getLast() { return last; }
    public void setLast(Boolean last) { this.last = last; }

    public Boolean getEmpty() { return empty; }
    public void setEmpty(Boolean empty) { this.empty = empty; }

    // 便利方法
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public boolean hasNext() {
        return !Boolean.TRUE.equals(last);
    }

    public boolean hasPrevious() {
        return !Boolean.TRUE.equals(first);
    }

    public int getContentSize() {
        return content != null ? content.size() : 0;
    }

    @Override
    public String toString() {
        return "PagedResult{" +
                "totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", size=" + size +
                ", number=" + number +
                ", contentSize=" + getContentSize() +
                '}';
    }
}
