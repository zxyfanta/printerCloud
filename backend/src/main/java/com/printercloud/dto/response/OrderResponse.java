package com.printercloud.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应DTO
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "订单响应")
public class OrderResponse {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    private Long id;

    /**
     * 订单号
     */
    @Schema(description = "订单号", example = "PRT202412071430001")
    private String orderNo;

    /**
     * 文件信息
     */
    @Schema(description = "文件信息")
    private FileInfoResponse fileInfo;

    /**
     * 打印配置
     */
    @Schema(description = "打印配置")
    private PrintConfigResponse printConfig;

    /**
     * 总金额
     */
    @Schema(description = "总金额", example = "3.00")
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态", example = "PAID")
    private String status;

    /**
     * 订单状态描述
     */
    @Schema(description = "订单状态描述", example = "已支付")
    private String statusDescription;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "123456")
    private String verificationCode;

    /**
     * 格式化验证码
     */
    @Schema(description = "格式化验证码", example = "123 456")
    private String verificationCodeDisplay;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-12-07 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-12-07 14:31:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 操作权限
     */
    @Schema(description = "操作权限")
    private OperationPermission operations;

    /**
     * 文件信息响应DTO
     */
    @Data
    @Schema(description = "文件信息")
    public static class FileInfoResponse {
        @Schema(description = "文件ID", example = "1")
        private Long id;

        @Schema(description = "原始文件名", example = "document.pdf")
        private String originalName;

        @Schema(description = "文件大小", example = "1024000")
        private Long fileSize;

        @Schema(description = "格式化文件大小", example = "1.0 MB")
        private String formattedFileSize;

        @Schema(description = "文件类型", example = "PDF")
        private String fileType;

        @Schema(description = "文件类型图标", example = "📄")
        private String fileTypeIcon;

        @Schema(description = "页数", example = "3")
        private Integer pageCount;
    }

    /**
     * 打印配置响应DTO
     */
    @Data
    @Schema(description = "打印配置")
    public static class PrintConfigResponse {
        @Schema(description = "打印份数", example = "2")
        private Integer copies;

        @Schema(description = "颜色类型", example = "BW")
        private String colorType;

        @Schema(description = "颜色类型描述", example = "黑白")
        private String colorTypeDescription;

        @Schema(description = "纸张规格", example = "A4")
        private String paperSize;

        @Schema(description = "纸张规格描述", example = "A4")
        private String paperSizeDescription;

        @Schema(description = "单双面", example = "SINGLE")
        private String duplex;

        @Schema(description = "单双面描述", example = "单面")
        private String duplexDescription;

        @Schema(description = "预估页数", example = "3")
        private Integer estimatedPages;

        @Schema(description = "单价", example = "0.50")
        private BigDecimal unitPrice;
    }

    /**
     * 操作权限DTO
     */
    @Data
    @Schema(description = "操作权限")
    public static class OperationPermission {
        @Schema(description = "是否可以取消", example = "false")
        private Boolean canCancel;

        @Schema(description = "是否可以支付", example = "false")
        private Boolean canPay;

        @Schema(description = "是否可以取件", example = "true")
        private Boolean canPickup;
    }
}
