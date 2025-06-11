package com.printercloud.service;

import com.printercloud.entity.SystemConfig;
import com.printercloud.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 系统配置服务
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    /**
     * 获取所有配置
     */
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findByDeletedFalseOrderByCreateTimeDesc();
    }

    /**
     * 根据配置键获取配置
     */
    public SystemConfig getConfigByKey(String configKey) {
        return systemConfigRepository.findByConfigKeyAndDeletedFalse(configKey).orElse(null);
    }

    /**
     * 根据配置键获取配置值
     */
    public String getConfigValue(String configKey) {
        SystemConfig config = getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 根据配置键获取配置值，如果不存在则返回默认值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取整数配置值
     */
    public Integer getIntConfig(String configKey, Integer defaultValue) {
        SystemConfig config = getConfigByKey(configKey);
        if (config != null) {
            Integer value = config.getIntValue();
            return value != null ? value : defaultValue;
        }
        return defaultValue;
    }

    /**
     * 获取布尔配置值
     */
    public Boolean getBooleanConfig(String configKey, Boolean defaultValue) {
        SystemConfig config = getConfigByKey(configKey);
        if (config != null) {
            return config.getBooleanValue();
        }
        return defaultValue;
    }

    /**
     * 获取长整数配置值
     */
    public Long getLongConfig(String configKey, Long defaultValue) {
        SystemConfig config = getConfigByKey(configKey);
        if (config != null) {
            Long value = config.getLongValue();
            return value != null ? value : defaultValue;
        }
        return defaultValue;
    }

    /**
     * 获取双精度配置值
     */
    public Double getDoubleConfig(String configKey, Double defaultValue) {
        SystemConfig config = getConfigByKey(configKey);
        if (config != null) {
            Double value = config.getDoubleValue();
            return value != null ? value : defaultValue;
        }
        return defaultValue;
    }

    /**
     * 创建或更新配置
     */
    public SystemConfig saveOrUpdateConfig(String configKey, String configName, String configValue, String configType) {
        SystemConfig config = getConfigByKey(configKey);
        if (config == null) {
            config = new SystemConfig(configKey, configName, configValue, configType);
        } else {
            config.setConfigValue(configValue);
            config.setConfigName(configName);
            config.setConfigType(configType);
        }
        return systemConfigRepository.save(config);
    }

    /**
     * 创建配置
     */
    public SystemConfig createConfig(SystemConfig config) {
        // 检查配置键是否已存在
        if (systemConfigRepository.existsByConfigKeyAndDeletedFalse(config.getConfigKey())) {
            throw new IllegalArgumentException("配置键已存在: " + config.getConfigKey());
        }
        return systemConfigRepository.save(config);
    }

    /**
     * 更新配置
     */
    public SystemConfig updateConfig(SystemConfig config) {
        SystemConfig existingConfig = systemConfigRepository.findById(config.getId())
                .orElseThrow(() -> new IllegalArgumentException("配置不存在"));

        // 系统配置不允许修改配置键
        if (existingConfig.isSystem() && !existingConfig.getConfigKey().equals(config.getConfigKey())) {
            throw new IllegalArgumentException("系统配置不允许修改配置键");
        }

        existingConfig.setConfigName(config.getConfigName());
        existingConfig.setConfigValue(config.getConfigValue());
        existingConfig.setConfigType(config.getConfigType());
        existingConfig.setDescription(config.getDescription());

        return systemConfigRepository.save(existingConfig);
    }

    /**
     * 删除配置
     */
    public void deleteConfig(Long id) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("配置不存在"));

        // 系统配置不允许删除
        if (config.isSystem()) {
            throw new IllegalArgumentException("系统配置不允许删除");
        }

        config.setDeleted(true);
        systemConfigRepository.save(config);
    }

    /**
     * 搜索配置
     */
    public List<SystemConfig> searchConfigs(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllConfigs();
        }
        return systemConfigRepository.searchByKeyword(keyword);
    }

    /**
     * 初始化默认配置
     */
    public void initDefaultConfigs() {
        // 轮询相关配置
        createDefaultConfig("polling.enabled", "启用轮询", "true", "BOOLEAN", "是否启用订单轮询刷新", true);
        createDefaultConfig("polling.interval", "轮询间隔", "5000", "NUMBER", "轮询刷新间隔（毫秒）", true);
        
        // 通知相关配置
        createDefaultConfig("notification.new_order", "新订单通知", "true", "BOOLEAN", "是否启用新订单通知", true);
        createDefaultConfig("notification.status_update", "状态更新通知", "true", "BOOLEAN", "是否启用订单状态更新通知", true);
        createDefaultConfig("notification.sound", "声音提醒", "false", "BOOLEAN", "是否启用声音提醒", true);
        
        // 显示相关配置
        createDefaultConfig("display.default_view_mode", "默认视图模式", "card", "STRING", "订单列表默认视图模式", true);
        createDefaultConfig("display.page_size", "每页显示数量", "20", "NUMBER", "订单列表每页显示数量", true);
        createDefaultConfig("display.show_refresh_tip", "显示刷新提示", "true", "BOOLEAN", "是否显示轮询刷新提示", true);
    }

    /**
     * 创建默认配置（如果不存在）
     */
    private void createDefaultConfig(String configKey, String configName, String configValue, 
                                   String configType, String description, boolean isSystem) {
        if (!systemConfigRepository.existsByConfigKeyAndDeletedFalse(configKey)) {
            SystemConfig config = new SystemConfig(configKey, configName, configValue, configType);
            config.setDescription(description);
            config.setIsSystem(isSystem);
            systemConfigRepository.save(config);
        }
    }
}
