package com.printercloud.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格配置响应DTO
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "价格配置响应")
public class PriceConfigResponse {

    /**
     * 配置ID
     */
    @Schema(description = "配置ID", example = "1")
    private Long id;

    /**
     * 配置键
     */
    @Schema(description = "配置键", example = "print.bw.single.a4")
    private String configKey;

    /**
     * 配置名称
     */
    @Schema(description = "配置名称", example = "A4黑白单面打印")
    private String configName;

    /**
     * 配置分类
     */
    @Schema(description = "配置分类", example = "PRINT")
    private String category;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述", example = "打印价格")
    private String categoryDescription;

    /**
     * 价格值
     */
    @Schema(description = "价格值", example = "0.50")
    private BigDecimal priceValue;

    /**
     * 单位
     */
    @Schema(description = "单位", example = "元/页")
    private String unit;

    /**
     * 格式化价格
     */
    @Schema(description = "格式化价格", example = "0.50 元/页")
    private String formattedPrice;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "A4纸张黑白单面打印价格")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "启用")
    private String statusDescription;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

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
     * 创建者
     */
    @Schema(description = "创建者", example = "admin")
    private String createdBy;

    /**
     * 更新者
     */
    @Schema(description = "更新者", example = "admin")
    private String updatedBy;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
}
