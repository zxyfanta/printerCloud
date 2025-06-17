package com.printercloud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 打印文件实体类
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Entity
@Table(name = "pc_print_file")
public class PrintFile {

    // 文件状态常量
    public static final int STATUS_UPLOADING = 0;      // 上传中
    public static final int STATUS_UPLOADED = 1;       // 上传成功
    public static final int STATUS_CALCULATING_MD5 = 2; // 计算MD5中
    public static final int STATUS_PARSING = 3;        // 解析文件中
    public static final int STATUS_COMPLETED = 4;      // 处理完成
    public static final int STATUS_FAILED = 5;         // 处理失败

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_md5")
    private String fileMd5;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "preview_path")
    private String previewPath;

    @Column(name = "status")
    private Integer status; // 0-上传中，1-上传成功，2-计算MD5中，3-解析文件中，4-处理完成，5-处理失败

    @Column(name = "parse_error")
    private String parseError; // 解析错误信息

    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Column(name = "deleted")
    private Boolean deleted;

    // 构造函数
    public PrintFile() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.deleted = false;
        this.status = STATUS_UPLOADING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getParseError() {
        return parseError;
    }

    public void setParseError(String parseError) {
        this.parseError = parseError;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    // 便利方法
    public String getFileExtension() {
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public boolean isImage() {
        String ext = getFileExtension();
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "gif".equals(ext);
    }

    public boolean isPdf() {
        return "pdf".equals(getFileExtension());
    }

    public boolean isDocument() {
        String ext = getFileExtension();
        return "doc".equals(ext) || "docx".equals(ext) || "xls".equals(ext) || "xlsx".equals(ext) || "ppt".equals(ext) || "pptx".equals(ext);
    }

    // 状态检查便利方法
    public boolean isUploading() {
        return this.status != null && this.status.equals(STATUS_UPLOADING);
    }

    public boolean isUploaded() {
        return this.status != null && this.status.equals(STATUS_UPLOADED);
    }

    public boolean isCalculatingMd5() {
        return this.status != null && this.status.equals(STATUS_CALCULATING_MD5);
    }

    public boolean isParsing() {
        return this.status != null && this.status.equals(STATUS_PARSING);
    }

    public boolean isCompleted() {
        return this.status != null && this.status.equals(STATUS_COMPLETED);
    }

    public boolean isFailed() {
        return this.status != null && this.status.equals(STATUS_FAILED);
    }

    public boolean isProcessing() {
        return isCalculatingMd5() || isParsing();
    }
}
