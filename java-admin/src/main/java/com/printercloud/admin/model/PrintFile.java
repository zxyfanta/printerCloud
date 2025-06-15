package com.printercloud.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * 打印文件实体模型
 */
public class PrintFile {
    
    private Long id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("originalName")
    private String originalName;
    
    @JsonProperty("fileName")
    private String fileName;
    
    @JsonProperty("filePath")
    private String filePath;
    
    @JsonProperty("fileSize")
    private Long fileSize;
    
    @JsonProperty("fileType")
    private String fileType;
    
    @JsonProperty("fileMd5")
    private String fileMd5;
    
    @JsonProperty("pageCount")
    private Integer pageCount;
    
    @JsonProperty("previewPath")
    private String previewPath;
    
    private Integer status;
    
    @JsonProperty("parseError")
    private String parseError;
    
    @JsonProperty("createTime")
    private LocalDateTime createTime;
    
    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
    
    private Boolean deleted;
    
    // 关联对象
    private User user;

    // 构造函数
    public PrintFile() {}

    public PrintFile(Long id, String originalName, Long fileSize) {
        this.id = id;
        this.originalName = originalName;
        this.fileSize = fileSize;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getFileMd5() { return fileMd5; }
    public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public String getPreviewPath() { return previewPath; }
    public void setPreviewPath(String previewPath) { this.previewPath = previewPath; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getParseError() { return parseError; }
    public void setParseError(String parseError) { this.parseError = parseError; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // 便利方法
    public String getFileExtension() {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
    }

    public boolean isImage() {
        String ext = getFileExtension();
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || 
               "gif".equals(ext) || "bmp".equals(ext);
    }

    public boolean isPdf() {
        return "pdf".equals(getFileExtension());
    }

    public boolean isDocument() {
        String ext = getFileExtension();
        return "doc".equals(ext) || "docx".equals(ext) || "xls".equals(ext) || 
               "xlsx".equals(ext) || "ppt".equals(ext) || "pptx".equals(ext);
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    public String getStatusText() {
        if (status == null) return "未知状态";
        return switch (status) {
            case 0 -> "上传中";
            case 1 -> "上传成功";
            case 2 -> "解析中";
            case 3 -> "解析成功";
            case 4 -> "解析失败";
            default -> "未知状态";
        };
    }

    @Override
    public String toString() {
        return "PrintFile{" +
                "id=" + id +
                ", originalName='" + originalName + '\'' +
                ", fileSize=" + fileSize +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrintFile printFile = (PrintFile) o;
        return id != null && id.equals(printFile.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
