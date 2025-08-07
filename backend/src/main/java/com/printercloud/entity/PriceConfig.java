package com.printercloud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格配置实体类
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Entity
@Table(name = "price_config", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key", unique = true),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_status", columnList = "status")
})
public class PriceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置键（唯一标识）
     */
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    /**
     * 配置名称
     */
    @Column(name = "config_name", nullable = false, length = 200)
    private String configName;

    /**
     * 配置分类
     */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * 价格值
     */
    @Column(name = "price_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceValue;

    /**
     * 单位
     */
    @Column(name = "unit", length = 20)
    private String unit = "元/页";

    /**
     * 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(name = "status")
    private Integer status = 1;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

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

    /**
     * 创建者
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 更新者
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = 1;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        if (this.unit == null) {
            this.unit = "元/页";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 价格配置分类枚举
     */
    public enum Category {
        PRINT("PRINT", "打印价格"),
        PAPER("PAPER", "纸张价格"),
        SERVICE("SERVICE", "服务费用"),
        DISCOUNT("DISCOUNT", "折扣配置");

        private final String code;
        private final String description;

        Category(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用");

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
     * 预定义价格配置键
     */
    public static class ConfigKey {
        // 黑白打印价格
        public static final String BW_SINGLE_A4 = "print.bw.single.a4";
        public static final String BW_DOUBLE_A4 = "print.bw.double.a4";
        public static final String BW_SINGLE_A3 = "print.bw.single.a3";
        public static final String BW_DOUBLE_A3 = "print.bw.double.a3";
        
        // 彩色打印价格
        public static final String COLOR_SINGLE_A4 = "print.color.single.a4";
        public static final String COLOR_DOUBLE_A4 = "print.color.double.a4";
        public static final String COLOR_SINGLE_A3 = "print.color.single.a3";
        public static final String COLOR_DOUBLE_A3 = "print.color.double.a3";
        
        // 服务费用
        public static final String SERVICE_FEE = "service.fee";
        public static final String DELIVERY_FEE = "delivery.fee";
        
        // 折扣配置
        public static final String BULK_DISCOUNT_THRESHOLD = "discount.bulk.threshold";
        public static final String BULK_DISCOUNT_RATE = "discount.bulk.rate";
        public static final String VIP_DISCOUNT_RATE = "discount.vip.rate";
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return Status.ENABLED.getCode().equals(this.status);
    }

    /**
     * 判断是否禁用
     */
    public boolean isDisabled() {
        return Status.DISABLED.getCode().equals(this.status);
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        for (Status s : Status.values()) {
            if (s.getCode().equals(this.status)) {
                return s.getDescription();
            }
        }
        return "未知";
    }

    /**
     * 获取分类描述
     */
    public String getCategoryDescription() {
        for (Category c : Category.values()) {
            if (c.getCode().equals(this.category)) {
                return c.getDescription();
            }
        }
        return this.category;
    }

    /**
     * 格式化价格显示
     */
    public String getFormattedPrice() {
        return this.priceValue + " " + this.unit;
    }
}
