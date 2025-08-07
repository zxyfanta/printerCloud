package com.printercloud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_no", columnList = "order_no", unique = true),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_time", columnList = "created_time")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单号
     */
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 文件ID
     */
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    /**
     * 打印份数
     */
    @Column(name = "copies", nullable = false)
    private Integer copies = 1;

    /**
     * 颜色类型：BW-黑白，COLOR-彩色
     */
    @Column(name = "color_type", nullable = false, length = 20)
    private String colorType = "BW";

    /**
     * 纸张规格：A4, A3
     */
    @Column(name = "paper_size", nullable = false, length = 20)
    private String paperSize = "A4";

    /**
     * 单双面：SINGLE-单面，DOUBLE-双面
     */
    @Column(name = "duplex", nullable = false, length = 20)
    private String duplex = "SINGLE";

    /**
     * 预估页数
     */
    @Column(name = "estimated_pages", nullable = false)
    private Integer estimatedPages = 1;

    /**
     * 单价（元）
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * 总金额（元）
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING_PAYMENT";

    /**
     * 开始打印时间
     */
    @Column(name = "print_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printStartTime;

    /**
     * 打印完成时间
     */
    @Column(name = "print_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printEndTime;

    /**
     * 取件时间
     */
    @Column(name = "pickup_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

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
            this.status = "PENDING_PAYMENT";
        }
        if (this.copies == null) {
            this.copies = 1;
        }
        if (this.estimatedPages == null) {
            this.estimatedPages = 1;
        }
        if (this.colorType == null) {
            this.colorType = "BW";
        }
        if (this.paperSize == null) {
            this.paperSize = "A4";
        }
        if (this.duplex == null) {
            this.duplex = "SINGLE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 订单状态枚举
     */
    public enum Status {
        PENDING_PAYMENT("PENDING_PAYMENT", "待支付"),
        PAID("PAID", "已支付"),
        PRINTING("PRINTING", "打印中"),
        READY_PICKUP("READY_PICKUP", "可取件"),
        COMPLETED("COMPLETED", "已完成"),
        CANCELLED("CANCELLED", "已取消"),
        PRINT_FAILED("PRINT_FAILED", "打印失败");

        private final String code;
        private final String description;

        Status(String code, String description) {
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
     * 颜色类型枚举
     */
    public enum ColorType {
        BW("BW", "黑白"),
        COLOR("COLOR", "彩色");

        private final String code;
        private final String description;

        ColorType(String code, String description) {
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
     * 纸张规格枚举
     */
    public enum PaperSize {
        A4("A4", "A4"),
        A3("A3", "A3");

        private final String code;
        private final String description;

        PaperSize(String code, String description) {
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
     * 单双面枚举
     */
    public enum Duplex {
        SINGLE("SINGLE", "单面"),
        DOUBLE("DOUBLE", "双面");

        private final String code;
        private final String description;

        Duplex(String code, String description) {
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
     * 获取状态描述
     */
    public String getStatusDescription() {
        for (Status s : Status.values()) {
            if (s.getCode().equals(this.status)) {
                return s.getDescription();
            }
        }
        return this.status;
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return Status.PENDING_PAYMENT.getCode().equals(this.status);
    }

    /**
     * 判断是否可以支付
     */
    public boolean canPay() {
        return Status.PENDING_PAYMENT.getCode().equals(this.status);
    }

    /**
     * 判断是否已完成
     */
    public boolean isCompleted() {
        return Status.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 判断是否可以打印
     */
    public boolean canPrint() {
        return Status.PAID.getCode().equals(this.status);
    }

    /**
     * 判断是否可以取件
     */
    public boolean canPickup() {
        return Status.READY_PICKUP.getCode().equals(this.status);
    }
}
