package com.printercloud.dto;

import java.math.BigDecimal;

/**
 * 创建订单请求DTO
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
public class CreateOrderRequest {

    private Long userId;
    private String userName;
    private String fileName;
    private String fileType;
    private Integer copies;
    private String pageRange;
    private Integer actualPages;
    private Boolean isColor;
    private Boolean isDoubleSide;
    private String paperSize;
    private String remark;
    private BigDecimal amount;
    private Long fileId; // 添加文件ID字段

    // 构造函数
    public CreateOrderRequest() {}

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    public String getPageRange() {
        return pageRange;
    }

    public void setPageRange(String pageRange) {
        this.pageRange = pageRange;
    }

    public Integer getActualPages() {
        return actualPages;
    }

    public void setActualPages(Integer actualPages) {
        this.actualPages = actualPages;
    }

    public Boolean getIsColor() {
        return isColor;
    }

    public void setIsColor(Boolean isColor) {
        this.isColor = isColor;
    }

    public Boolean getIsDoubleSide() {
        return isDoubleSide;
    }

    public void setIsDoubleSide(Boolean isDoubleSide) {
        this.isDoubleSide = isDoubleSide;
    }

    public String getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(String paperSize) {
        this.paperSize = paperSize;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
}
