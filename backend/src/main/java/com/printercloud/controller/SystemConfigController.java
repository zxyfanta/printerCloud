package com.printercloud.controller;

import com.printercloud.entity.SystemConfig;
import com.printercloud.entity.User;
import com.printercloud.service.SystemConfigService;
import com.printercloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.printercloud.common.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private UserService userService;

    /**
     * 获取公开配置（无需认证）
     */
    @GetMapping("/public")
    public R<Map<String, String>> getPublicConfigs() {
        try {
            Map<String, String> configs = new HashMap<>();

            // 只返回允许公开的配置
            configs.put("polling.interval", systemConfigService.getConfigValue("polling.interval", "5000"));
            configs.put("display.default_view_mode", systemConfigService.getConfigValue("display.default_view_mode", "card"));
            configs.put("display.page_size", systemConfigService.getConfigValue("display.page_size", "20"));

            return R.ok(configs, "获取成功");
        } catch (Exception e) {
            return R.fail("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有配置（管理员）
     */
    @GetMapping("/admin/list")
    public R<List<SystemConfig>> getAllConfigs(@RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            List<SystemConfig> configs = systemConfigService.getAllConfigs();
            return R.ok(configs, "获取成功");
        } catch (Exception e) {
            return R.fail("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据配置键获取配置
     */
    @GetMapping("/admin/{configKey}")
    public R<SystemConfig> getConfigByKey(
            @PathVariable String configKey,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            SystemConfig config = systemConfigService.getConfigByKey(configKey);
            if (config == null) {
                return R.notFound("配置不存在");
            } else {
                return R.ok(config, "获取成功");
            }
        } catch (Exception e) {
            return R.fail("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 创建配置（管理员）
     */
    @PostMapping("/admin/create")
    public R<SystemConfig> createConfig(
            @RequestBody SystemConfig config,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            SystemConfig created = systemConfigService.createConfig(config);
            return R.ok(created, "创建成功");
        } catch (IllegalArgumentException e) {
            return R.validateFailed(e.getMessage());
        } catch (Exception e) {
            return R.fail("创建配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新配置（管理员）
     */
    @PostMapping("/admin/update")
    public R<SystemConfig> updateConfig(
            @RequestBody SystemConfig config,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            SystemConfig updated = systemConfigService.updateConfig(config);
            return R.ok(updated, "更新成功");
        } catch (IllegalArgumentException e) {
            return R.validateFailed(e.getMessage());
        } catch (Exception e) {
            return R.fail("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新配置
     */
    @PostMapping("/admin/batch-update")
    public R<Void> batchUpdateConfigs(
            @RequestBody Map<String, String> configMap,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                String configKey = entry.getKey();
                String configValue = entry.getValue();

                SystemConfig config = systemConfigService.getConfigByKey(configKey);
                if (config != null) {
                    config.setConfigValue(configValue);
                    systemConfigService.updateConfig(config);
                }
            }

            return R.ok(null, "批量更新成功");
        } catch (Exception e) {
            return R.fail("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除配置（管理员）
     */
    @DeleteMapping("/admin/{id}")
    public R<Void> deleteConfig(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            systemConfigService.deleteConfig(id);
            return R.ok(null, "删除成功");
        } catch (IllegalArgumentException e) {
            return R.validateFailed(e.getMessage());
        } catch (Exception e) {
            return R.fail("删除配置失败: " + e.getMessage());
        }
    }

    /**
     * 搜索配置（管理员）
     */
    @GetMapping("/admin/search")
    public R<List<SystemConfig>> searchConfigs(
            @RequestParam String keyword,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            List<SystemConfig> configs = systemConfigService.searchConfigs(keyword);
            return R.ok(configs, "搜索成功");
        } catch (Exception e) {
            return R.fail("搜索失败: " + e.getMessage());
        }
    }
}
