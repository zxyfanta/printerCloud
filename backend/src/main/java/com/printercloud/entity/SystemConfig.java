package com.printercloud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Entity
@Table(name = "pc_system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", unique = true, nullable = false)
    private String configKey;

    @Column(name = "config_name", nullable = false)
    private String configName;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_type")
    private String configType; // STRING, NUMBER, BOOLEAN, JSON

    @Column(name = "description")
    private String description;

    @Column(name = "is_system")
    private Boolean isSystem; // 是否为系统配置，系统配置不允许删除

    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Column(name = "deleted")
    private Boolean deleted;

    // 构造函数
    public SystemConfig() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.deleted = false;
        this.isSystem = false;
    }

    public SystemConfig(String configKey, String configName, String configValue, String configType) {
        this();
        this.configKey = configKey;
        this.configName = configName;
        this.configValue = configValue;
        this.configType = configType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    // 便利方法
    public boolean isSystem() {
        return this.isSystem != null && this.isSystem;
    }

    public boolean isDeleted() {
        return this.deleted != null && this.deleted;
    }

    // 类型转换方法
    public Integer getIntValue() {
        try {
            return Integer.parseInt(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getLongValue() {
        try {
            return Long.parseLong(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Boolean getBooleanValue() {
        return Boolean.parseBoolean(this.configValue);
    }

    public Double getDoubleValue() {
        try {
            return Double.parseDouble(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
