package com.printercloud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_open_id", columnList = "open_id", unique = true),
    @Index(name = "idx_union_id", columnList = "union_id"),
    @Index(name = "idx_phone", columnList = "phone"),
    @Index(name = "idx_created_time", columnList = "created_time")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 微信OpenID
     */
    @Column(name = "open_id", nullable = false, unique = true, length = 64)
    private String openId;

    /**
     * 微信UnionID
     */
    @Column(name = "union_id", length = 64)
    private String unionId;

    /**
     * 用户名
     */
    @Column(name = "username", length = 50)
    private String username;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Column(name = "gender")
    private Integer gender = 0;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 用户角色：USER-普通用户，ADMIN-管理员
     */
    @Column(name = "role", length = 20)
    private String role = "USER";

    /**
     * 用户状态：0-禁用，1-正常
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

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column(name = "deleted")
    private Boolean deleted = false;

    /**
     * JPA生命周期回调
     */
    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = 1;
        }
        if (this.gender == null) {
            this.gender = 0;
        }
        if (this.role == null) {
            this.role = "USER";
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 用户状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
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
     * 性别枚举
     */
    public enum Gender {
        UNKNOWN(0, "未知"),
        MALE(1, "男"),
        FEMALE(2, "女");

        private final Integer code;
        private final String description;

        Gender(Integer code, String description) {
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
     * 用户角色枚举
     */
    public enum Role {
        USER("USER", "普通用户"),
        ADMIN("ADMIN", "管理员");

        private final String code;
        private final String description;

        Role(String code, String description) {
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
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return Role.ADMIN.getCode().equals(this.role);
    }

    /**
     * 判断用户是否正常状态
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status) && !this.deleted;
    }

    /**
     * 判断用户是否被禁用
     */
    public boolean isDisabled() {
        return Status.DISABLED.getCode().equals(this.status);
    }

    /**
     * 获取性别描述
     */
    public String getGenderDescription() {
        for (Gender g : Gender.values()) {
            if (g.getCode().equals(this.gender)) {
                return g.getDescription();
            }
        }
        return "未知";
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
}
