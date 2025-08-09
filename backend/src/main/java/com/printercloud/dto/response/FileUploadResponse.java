package com.printercloud.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件上传响应DTO
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "文件上传响应")
public class FileUploadResponse {

    /**
     * 文件ID（小程序使用）
     */
    @Schema(description = "文件ID", example = "1")
    private Long id;

    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名", example = "document.pdf")
    private String originalName;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小", example = "1024000")
    private Long fileSize;

    /**
     * 格式化文件大小
     */
    @Schema(description = "格式化文件大小", example = "1.0 MB")
    private String formattedFileSize;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型", example = "PDF")
    private String fileType;

    /**
     * 文件类型图标
     */
    @Schema(description = "文件类型图标", example = "📄")
    private String fileTypeIcon;

    /**
     * 页数
     */
    @Schema(description = "页数", example = "3")
    private Integer pageCount;

    /**
     * 宽度（图片类型）
     */
    @Schema(description = "宽度", example = "1920")
    private Integer width;

    /**
     * 高度（图片类型）
     */
    @Schema(description = "高度", example = "1080")
    private Integer height;

    /**
     * 文件哈希值
     */
    @Schema(description = "文件哈希值", example = "abc123def456")
    private String fileHash;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间", example = "2024-12-07 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    /**
     * 是否为图片
     */
    @Schema(description = "是否为图片", example = "false")
    private Boolean isImage;

    /**
     * 是否为文档
     */
    @Schema(description = "是否为文档", example = "true")
    private Boolean isDocument;
}
