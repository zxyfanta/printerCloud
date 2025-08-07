package com.printercloud.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建订单请求DTO
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    /**
     * 文件信息
     */
    @NotNull(message = "文件信息不能为空")
    @Schema(description = "文件信息", required = true)
    private FileInfoDto fileInfo;

    /**
     * 打印配置
     */
    @NotNull(message = "打印配置不能为空")
    @Schema(description = "打印配置", required = true)
    private PrintConfigDto printConfig;

    /**
     * 预估页数
     */
    @Min(value = 1, message = "预估页数至少为1")
    @Schema(description = "预估页数", example = "3")
    private Integer estimatedPages;

    /**
     * 总费用
     */
    @Schema(description = "总费用", example = "3.00")
    private String totalCost;

    /**
     * 文件信息DTO
     */
    @Data
    @Schema(description = "文件信息")
    public static class FileInfoDto {
        @Schema(description = "文件ID", example = "1")
        private Long fileId;

        @Schema(description = "文件名", example = "document.pdf")
        private String fileName;

        @Schema(description = "文件大小", example = "1024000")
        private Long fileSize;

        @Schema(description = "文件类型", example = "PDF")
        private String fileType;

        @Schema(description = "页数", example = "3")
        private Integer pageCount;
    }

    /**
     * 打印配置DTO
     */
    @Data
    @Schema(description = "打印配置")
    public static class PrintConfigDto {
        @NotNull(message = "打印份数不能为空")
        @Min(value = 1, message = "打印份数至少为1")
        @Schema(description = "打印份数", example = "2", required = true)
        private Integer copies;

        @NotBlank(message = "颜色类型不能为空")
        @Schema(description = "颜色类型", example = "BW", allowableValues = {"BW", "COLOR"}, required = true)
        private String colorType;

        @NotBlank(message = "纸张规格不能为空")
        @Schema(description = "纸张规格", example = "A4", allowableValues = {"A4", "A3"}, required = true)
        private String paperSize;

        @NotBlank(message = "单双面设置不能为空")
        @Schema(description = "单双面设置", example = "SINGLE", allowableValues = {"SINGLE", "DOUBLE"}, required = true)
        private String duplex;
    }
}
