package com.printercloud.controller;

import com.printercloud.entity.SystemConfig;
import com.printercloud.entity.User;
import com.printercloud.service.SystemConfigService;
import com.printercloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> getPublicConfigs() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, String> configs = new HashMap<>();
            
            // 只返回允许公开的配置
            configs.put("polling.interval", systemConfigService.getConfigValue("polling.interval", "5000"));
            configs.put("display.default_view_mode", systemConfigService.getConfigValue("display.default_view_mode", "card"));
            configs.put("display.page_size", systemConfigService.getConfigValue("display.page_size", "20"));
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", configs);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有配置（管理员）
     */
    @GetMapping("/admin/list")
    public ResponseEntity<Map<String, Object>> getAllConfigs(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            List<SystemConfig> configs = systemConfigService.getAllConfigs();
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", configs);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据配置键获取配置
     */
    @GetMapping("/admin/{configKey}")
    public ResponseEntity<Map<String, Object>> getConfigByKey(
            @PathVariable String configKey,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            SystemConfig config = systemConfigService.getConfigByKey(configKey);
            if (config == null) {
                response.put("code", 404);
                response.put("message", "配置不存在");
            } else {
                response.put("code", 200);
                response.put("message", "获取成功");
                response.put("data", config);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 创建配置（管理员）
     */
    @PostMapping("/admin/create")
    public ResponseEntity<Map<String, Object>> createConfig(
            @RequestBody SystemConfig config,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            SystemConfig created = systemConfigService.createConfig(config);
            response.put("code", 200);
            response.put("message", "创建成功");
            response.put("data", created);
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "创建配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新配置（管理员）
     */
    @PostMapping("/admin/update")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @RequestBody SystemConfig config,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            SystemConfig updated = systemConfigService.updateConfig(config);
            response.put("code", 200);
            response.put("message", "更新成功");
            response.put("data", updated);
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "更新配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 批量更新配置
     */
    @PostMapping("/admin/batch-update")
    public ResponseEntity<Map<String, Object>> batchUpdateConfigs(
            @RequestBody Map<String, String> configMap,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
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
            
            response.put("code", 200);
            response.put("message", "批量更新成功");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "批量更新失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除配置（管理员）
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> deleteConfig(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            systemConfigService.deleteConfig(id);
            response.put("code", 200);
            response.put("message", "删除成功");
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 搜索配置（管理员）
     */
    @GetMapping("/admin/search")
    public ResponseEntity<Map<String, Object>> searchConfigs(
            @RequestParam String keyword,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            List<SystemConfig> configs = systemConfigService.searchConfigs(keyword);
            response.put("code", 200);
            response.put("message", "搜索成功");
            response.put("data", configs);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "搜索失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
