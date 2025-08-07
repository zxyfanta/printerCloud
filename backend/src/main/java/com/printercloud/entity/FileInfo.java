package com.printercloud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件信息实体类
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Entity
@Table(name = "file_info", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_file_hash", columnList = "file_hash"),
    @Index(name = "idx_created_time", columnList = "created_time")
})
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /**
     * 存储文件名
     */
    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    /**
     * 文件路径
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 文件类型
     */
    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    /**
     * MIME类型
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * 文件哈希值
     */
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    /**
     * 页数（文档类型）
     */
    @Column(name = "page_count")
    private Integer pageCount = 0;

    /**
     * 宽度（图片类型）
     */
    @Column(name = "width")
    private Integer width = 0;

    /**
     * 高度（图片类型）
     */
    @Column(name = "height")
    private Integer height = 0;

    /**
     * 文件状态：0-删除，1-正常
     */
    @Column(name = "status")
    private Integer status = 1;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = 1;
        }
        if (this.pageCount == null) {
            this.pageCount = 0;
        }
        if (this.width == null) {
            this.width = 0;
        }
        if (this.height == null) {
            this.height = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 文件状态枚举
     */
    public enum Status {
        DELETED(0, "已删除"),
        NORMAL(1, "正常");

        private final Integer code;
        private final String description;

        Status(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 文件类型枚举
     */
    public enum FileType {
        PDF("PDF", "PDF文档", "📄"),
        DOC("DOC", "Word文档", "📝"),
        DOCX("DOCX", "Word文档", "📝"),
        XLS("XLS", "Excel表格", "📊"),
        XLSX("XLSX", "Excel表格", "📊"),
        PPT("PPT", "PowerPoint演示", "📽️"),
        PPTX("PPTX", "PowerPoint演示", "📽️"),
        TXT("TXT", "文本文件", "📄"),
        JPG("JPG", "图片文件", "🖼️"),
        JPEG("JPEG", "图片文件", "🖼️"),
        PNG("PNG", "图片文件", "🖼️");

        private final String code;
        private final String description;
        private final String icon;

        FileType(String code, String description, String icon) {
            this.code = code;
            this.description = description;
            this.icon = icon;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    /**
     * 获取文件类型图标
     */
    public String getFileTypeIcon() {
        String upperType = this.fileType != null ? this.fileType.toUpperCase() : "";
        for (FileType ft : FileType.values()) {
            if (ft.getCode().equals(upperType)) {
                return ft.getIcon();
            }
        }
        return "📄";
    }

    /**
     * 获取格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (this.fileSize == null || this.fileSize == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = this.fileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * 判断是否为图片文件
     */
    public boolean isImage() {
        if (this.fileType == null) {
            return false;
        }
        String upperType = this.fileType.toUpperCase();
        return "JPG".equals(upperType) || "JPEG".equals(upperType) || "PNG".equals(upperType);
    }

    /**
     * 判断是否为文档文件
     */
    public boolean isDocument() {
        if (this.fileType == null) {
            return false;
        }
        String upperType = this.fileType.toUpperCase();
        return "PDF".equals(upperType) || "DOC".equals(upperType) || 
               "DOCX".equals(upperType) || "TXT".equals(upperType);
    }

    /**
     * 判断文件是否正常
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }
}
