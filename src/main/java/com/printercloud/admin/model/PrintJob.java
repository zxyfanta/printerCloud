package com.printercloud.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 打印任务模型
 */
public class PrintJob {
    
    private String id;
    private Long orderId;
    private Long fileId;
    private String fileName;
    private String filePath;
    private String printerName;
    private Integer copies;
    private Boolean doubleSided;
    private String paperSize;
    private String colorMode;
    private String quality;
    private Integer status;
    private Integer progress;
    private String errorMessage;
    
    @JsonProperty("createTime")
    private LocalDateTime createTime;
    
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
    @JsonProperty("completeTime")
    private LocalDateTime completeTime;

    // 构造函数
    public PrintJob() {}

    public PrintJob(String id, String fileName, String printerName) {
        this.id = id;
        this.fileName = fileName;
        this.printerName = printerName;
        this.createTime = LocalDateTime.now();
        this.status = 0; // 等待中
        this.progress = 0;
        this.copies = 1;
        this.doubleSided = false;
        this.paperSize = "A4";
        this.colorMode = "BLACK_WHITE";
        this.quality = "NORMAL";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getPrinterName() { return printerName; }
    public void setPrinterName(String printerName) { this.printerName = printerName; }

    public Integer getCopies() { return copies; }
    public void setCopies(Integer copies) { this.copies = copies; }

    public Boolean getDoubleSided() { return doubleSided; }
    public void setDoubleSided(Boolean doubleSided) { this.doubleSided = doubleSided; }

    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }

    public String getColorMode() { return colorMode; }
    public void setColorMode(String colorMode) { this.colorMode = colorMode; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }

    // 便利方法
    public String getStatusText() {
        if (status == null) return "未知状态";
        return switch (status) {
            case 0 -> "等待中";
            case 1 -> "打印中";
            case 2 -> "已完成";
            case 3 -> "失败";
            case 4 -> "已取消";
            default -> "未知状态";
        };
    }

    public boolean isCompleted() {
        return status != null && status == 2;
    }

    public boolean isFailed() {
        return status != null && status == 3;
    }

    public boolean isCancelled() {
        return status != null && status == 4;
    }

    public boolean isRunning() {
        return status != null && status == 1;
    }

    public boolean canCancel() {
        return status != null && (status == 0 || status == 1);
    }

    @Override
    public String toString() {
        return "PrintJob{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", printerName='" + printerName + '\'' +
                ", status=" + status +
                ", progress=" + progress +
                '}';
    }
}
