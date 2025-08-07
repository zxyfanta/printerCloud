package com.printercloud.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * 文件上传请求DTO
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "文件上传请求")
public class UploadFileRequest {

    /**
     * 上传的文件
     */
    @NotNull(message = "上传文件不能为空")
    @Schema(description = "上传的文件", required = true)
    private MultipartFile file;

    /**
     * 文件描述
     */
    @Schema(description = "文件描述", example = "重要文档")
    private String description;
}
