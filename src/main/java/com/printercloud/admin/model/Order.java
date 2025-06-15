package com.printercloud.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单模型
 */
public class Order {
    
    private Long id;
    
    @JsonProperty("orderNo")
    private String orderNo;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("fileId")
    private Long fileId;
    
    @JsonProperty("fileName")
    private String fileName;
    
    @JsonProperty("copies")
    private Integer copies;
    
    @JsonProperty("doubleSided")
    private Boolean doubleSided;
    
    @JsonProperty("paperSize")
    private String paperSize;
    
    @JsonProperty("colorMode")
    private String colorMode;
    
    @JsonProperty("totalPages")
    private Integer totalPages;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    private Integer status;
    
    @JsonProperty("payStatus")
    private Integer payStatus;
    
    @JsonProperty("printStatus")
    private Integer printStatus;
    
    @JsonProperty("remark")
    private String remark;
    
    @JsonProperty("createTime")
    private LocalDateTime createTime;
    
    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
    
    @JsonProperty("payTime")
    private LocalDateTime payTime;
    
    @JsonProperty("printTime")
    private LocalDateTime printTime;
    
    // 关联对象
    private User user;
    private PrintFile file;
    private List<PrintJob> printJobs;

    // 构造函数
    public Order() {}

    public Order(String orderNo, Long userId, Long fileId) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.fileId = fileId;
        this.createTime = LocalDateTime.now();
        this.status = 0; // 待支付
        this.payStatus = 0; // 未支付
        this.printStatus = 0; // 未打印
        this.copies = 1;
        this.doubleSided = false;
        this.paperSize = "A4";
        this.colorMode = "BLACK_WHITE";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getCopies() { return copies; }
    public void setCopies(Integer copies) { this.copies = copies; }

    public Boolean getDoubleSided() { return doubleSided; }
    public void setDoubleSided(Boolean doubleSided) { this.doubleSided = doubleSided; }

    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }

    public String getColorMode() { return colorMode; }
    public void setColorMode(String colorMode) { this.colorMode = colorMode; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }

    public Integer getPrintStatus() { return printStatus; }
    public void setPrintStatus(Integer printStatus) { this.printStatus = printStatus; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }

    public LocalDateTime getPrintTime() { return printTime; }
    public void setPrintTime(LocalDateTime printTime) { this.printTime = printTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PrintFile getFile() { return file; }
    public void setFile(PrintFile file) { this.file = file; }

    public List<PrintJob> getPrintJobs() { return printJobs; }
    public void setPrintJobs(List<PrintJob> printJobs) { this.printJobs = printJobs; }

    // 便利方法
    public String getStatusText() {
        if (status == null) return "未知状态";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "打印中";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "已退款";
            default -> "未知状态";
        };
    }

    public String getPayStatusText() {
        if (payStatus == null) return "未知";
        return switch (payStatus) {
            case 0 -> "未支付";
            case 1 -> "已支付";
            case 2 -> "支付失败";
            case 3 -> "已退款";
            default -> "未知";
        };
    }

    public String getPrintStatusText() {
        if (printStatus == null) return "未知";
        return switch (printStatus) {
            case 0 -> "未打印";
            case 1 -> "打印中";
            case 2 -> "已打印";
            case 3 -> "打印失败";
            default -> "未知";
        };
    }

    public boolean isPaid() {
        return payStatus != null && payStatus == 1;
    }

    public boolean isCompleted() {
        return status != null && status == 3;
    }

    public boolean isCancelled() {
        return status != null && status == 4;
    }

    public boolean canCancel() {
        return status != null && (status == 0 || status == 1);
    }

    public boolean canPrint() {
        return isPaid() && printStatus != null && printStatus == 0;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
