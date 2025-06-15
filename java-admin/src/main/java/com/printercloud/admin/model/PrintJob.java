package com.printercloud.admin.model;

import java.time.LocalDateTime;

public class PrintJob {
    private String id;
    private String fileName;
    private String printerName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private int pages;
    private String fileType;
    private long fileSize;
    private String userId;
    private String errorMessage;

    // Constructors
    public PrintJob() {}

    public PrintJob(String id, String fileName, String printerName, String status) {
        this.id = id;
        this.fileName = fileName;
        this.printerName = printerName;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getPrinterName() { return printerName; }
    public void setPrinterName(String printerName) { this.printerName = printerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    @Override
    public String toString() {
        return "PrintJob{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", printerName='" + printerName + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
