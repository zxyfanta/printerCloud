package com.printercloud.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 更新价格配置请求DTO
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "更新价格配置请求")
public class UpdatePriceConfigRequest {

    /**
     * 配置ID
     */
    @NotNull(message = "配置ID不能为空")
    @Schema(description = "配置ID", example = "1", required = true)
    private Long id;

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称", example = "A4黑白单面打印", required = true)
    private String configName;

    /**
     * 价格值
     */
    @NotNull(message = "价格值不能为空")
    @DecimalMin(value = "0.01", message = "价格值必须大于0")
    @Schema(description = "价格值", example = "0.50", required = true)
    private BigDecimal priceValue;

    /**
     * 单位
     */
    @Schema(description = "单位", example = "元/页")
    private String unit;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "A4纸张黑白单面打印价格")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortOrder;
}
