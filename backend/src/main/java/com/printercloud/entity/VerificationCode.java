package com.printercloud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 验证码实体类
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Entity
@Table(name = "verification_codes", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_code", columnList = "code"),
    @Index(name = "idx_type_status", columnList = "type,status"),
    @Index(name = "idx_expire_time", columnList = "expire_time")
})
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单ID
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 验证码
     */
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    /**
     * 验证码类型：pickup-取件
     */
    @Column(name = "type", nullable = false, length = 20)
    private String type = "pickup";

    /**
     * 状态：ACTIVE-有效，USED-已使用，EXPIRED-已过期
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    /**
     * 过期时间
     */
    @Column(name = "expire_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 使用时间
     */
    @Column(name = "used_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime usedTime;

    /**
     * 使用者
     */
    @Column(name = "used_by", length = 100)
    private String usedBy;

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
        if (this.type == null) {
            this.type = "pickup";
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.expireTime == null) {
            // 默认24小时后过期
            this.expireTime = LocalDateTime.now().plusHours(24);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 验证码类型枚举
     */
    public enum Type {
        PICKUP("pickup", "取件验证码");

        private final String code;
        private final String description;

        Type(String code, String description) {
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
     * 验证码状态枚举
     */
    public enum Status {
        ACTIVE("ACTIVE", "有效"),
        USED("USED", "已使用"),
        EXPIRED("EXPIRED", "已过期");

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
     * 获取格式化的验证码（如：123 456）
     */
    public String getFormattedCode() {
        if (this.code == null || this.code.length() != 6) {
            return this.code;
        }
        return this.code.substring(0, 3) + " " + this.code.substring(3);
    }

    /**
     * 判断验证码是否有效
     */
    public boolean isActive() {
        return Status.ACTIVE.getCode().equals(this.status) && 
               this.expireTime != null && 
               LocalDateTime.now().isBefore(this.expireTime);
    }

    /**
     * 判断验证码是否已使用
     */
    public boolean isUsed() {
        return Status.USED.getCode().equals(this.status);
    }

    /**
     * 判断验证码是否已过期
     */
    public boolean isExpired() {
        return Status.EXPIRED.getCode().equals(this.status) || 
               (this.expireTime != null && LocalDateTime.now().isAfter(this.expireTime));
    }

    /**
     * 判断验证码是否可以使用
     */
    public boolean canUse() {
        return isActive() && !isUsed() && !isExpired();
    }

    /**
     * 标记验证码为已使用
     */
    public void markAsUsed(String usedBy) {
        this.status = Status.USED.getCode();
        this.usedTime = LocalDateTime.now();
        this.usedBy = usedBy;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 获取剩余有效时间（分钟）
     */
    public long getRemainingMinutes() {
        if (this.expireTime == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(this.expireTime)) {
            return 0;
        }
        
        return java.time.Duration.between(now, this.expireTime).toMinutes();
    }
}
