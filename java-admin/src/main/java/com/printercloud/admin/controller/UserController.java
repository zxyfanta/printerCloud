package com.printercloud.admin.controller;

import com.printercloud.admin.model.User;
import com.printercloud.admin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户查询、管理等功能
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户列表
     */
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer status) {
        
        try {
            logger.info("获取用户列表 - page: {}, pageSize: {}, search: {}, status: {}", 
                       page, pageSize, search, status);
            
            Map<String, Object> result = userService.getUsers(page, pageSize, search, status);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", result
            ));
            
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取用户列表失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            logger.info("获取用户详情 - id: {}", id);
            
            User user = userService.getUserById(id);
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "success",
                    "data", user
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "code", 404,
                    "message", "用户不存在"
                ));
            }
            
        } catch (Exception e) {
            logger.error("获取用户详情失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取用户详情失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新用户状态
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Integer status = (Integer) request.get("status");
            logger.info("更新用户状态 - id: {}, status: {}", id, status);
            
            boolean success = userService.updateUserStatus(id, status);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "状态更新成功"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "状态更新失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("更新用户状态失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "状态更新失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            logger.info("删除用户 - id: {}", id);
            
            boolean success = userService.deleteUser(id);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "用户删除成功"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "用户删除失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("删除用户失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "删除用户失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getUserStatistics() {
        try {
            logger.info("获取用户统计信息");
            
            Map<String, Object> statistics = userService.getUserStatistics();
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", statistics
            ));
            
        } catch (Exception e) {
            logger.error("获取用户统计信息失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        try {
            logger.info("重置用户密码 - id: {}", id);
            
            String newPassword = userService.resetPassword(id);
            if (newPassword != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "密码重置成功",
                    "data", Map.of("newPassword", newPassword)
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "密码重置失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("重置用户密码失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "密码重置失败: " + e.getMessage()
            ));
        }
    }
}
