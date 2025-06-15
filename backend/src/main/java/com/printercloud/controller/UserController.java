package com.printercloud.controller;

import com.printercloud.entity.User;
import com.printercloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（管理员）
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        
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
            
            // 构建分页和排序
            Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            
            // 获取用户列表
            Page<User> userPage = userService.getUserList(pageable, search, role, status);
            
            response.put("code", 200);
            response.put("success", true);
            response.put("message", "获取用户列表成功");
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", userPage.getContent());
            data.put("totalElements", userPage.getTotalElements());
            data.put("totalPages", userPage.getTotalPages());
            data.put("page", page);
            data.put("pageSize", size);
            
            response.put("data", data);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "获取用户列表失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
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
            
            User user = userService.getUserById(id);
            if (user != null) {
                response.put("code", 200);
                response.put("success", true);
                response.put("message", "获取用户详情成功");
                response.put("data", user);
            } else {
                response.put("code", 404);
                response.put("success", false);
                response.put("message", "用户不存在");
            }
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "获取用户详情失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户状态
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam Integer status) {
        
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
            
            User user = userService.getUserById(id);
            if (user != null) {
                user.setStatus(status);
                userService.updateUser(user);
                
                response.put("code", 200);
                response.put("success", true);
                response.put("message", "用户状态更新成功");
                response.put("data", user);
            } else {
                response.put("code", 404);
                response.put("success", false);
                response.put("message", "用户不存在");
            }
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "更新用户状态失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
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
            
            boolean success = userService.resetPassword(id, "123456");
            if (success) {
                response.put("code", 200);
                response.put("success", true);
                response.put("message", "密码重置成功，新密码为：123456");
            } else {
                response.put("code", 404);
                response.put("success", false);
                response.put("message", "用户不存在");
            }
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "重置密码失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户（软删除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isSuperAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            userService.deleteUser(id);
            response.put("code", 200);
            response.put("success", true);
            response.put("message", "用户删除成功");
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "删除用户失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
