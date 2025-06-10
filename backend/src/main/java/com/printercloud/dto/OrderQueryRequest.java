package com.printercloud.dto;

/**
 * 订单查询请求DTO
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
public class OrderQueryRequest {

    private Long userId;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
    private String verifyCode;

    // 构造函数
    public OrderQueryRequest() {}

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
