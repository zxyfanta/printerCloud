package com.printercloud.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 * 订单模型
 */
public class Order {
    
    private Long id;
    private String orderNo;
    private String fileName;
    private String fileType;
    private Integer copies;
    private Integer actualPages;
    private Double amount;
    private String verifyCode;
    private Integer status;
    private Boolean isColor;
    private Boolean isDoubleSide;
    private String paperSize;
    private String pageRange;
    private String userName;
    private String remark;
    
    @JsonProperty("createTime")
    private Date createTime;
    
    @JsonProperty("updateTime")
    private Date updateTime;
    
    @JsonProperty("payTime")
    private Date payTime;
    
    @JsonProperty("finishTime")
    private Date finishTime;

    // 构造函数
    public Order() {}

    public Order(Long id, String orderNo, String fileName) {
        this.id = id;
        this.orderNo = orderNo;
        this.fileName = fileName;
        this.createTime = new Date();
        this.status = 0; // 待支付
        this.copies = 1;
        this.isColor = false;
        this.isDoubleSide = false;
        this.paperSize = "A4";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Integer getCopies() { return copies; }
    public void setCopies(Integer copies) { this.copies = copies; }

    public Integer getActualPages() { return actualPages; }
    public void setActualPages(Integer actualPages) { this.actualPages = actualPages; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getVerifyCode() { return verifyCode; }
    public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Boolean getIsColor() { return isColor; }
    public void setIsColor(Boolean isColor) { this.isColor = isColor; }

    public Boolean getIsDoubleSide() { return isDoubleSide; }
    public void setIsDoubleSide(Boolean isDoubleSide) { this.isDoubleSide = isDoubleSide; }

    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }

    public String getPageRange() { return pageRange; }
    public void setPageRange(String pageRange) { this.pageRange = pageRange; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }

    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "打印中";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "已退款";
            default -> "未知";
        };
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return status != null && status == 0;
    }

    /**
     * 判断是否可以完成
     */
    public boolean canComplete() {
        return status != null && (status == 1 || status == 2);
    }

    /**
     * 判断是否可以下载
     */
    public boolean canDownload() {
        return status != null && (status == 1 || status == 2 || status == 3);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                ", amount=" + amount +
                ", createTime=" + createTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id != null && id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
