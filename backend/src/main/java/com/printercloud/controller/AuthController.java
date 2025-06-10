package com.printercloud.controller;

import com.printercloud.dto.LoginRequest;
import com.printercloud.dto.LoginResponse;
import com.printercloud.entity.User;
import com.printercloud.service.UserService;
import com.printercloud.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 统一登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if ("ADMIN".equals(request.getLoginType())) {
                // 管理员登录
                String token = userService.adminLogin(request.getUsername(), request.getPassword());
                if (token != null) {
                    User user = userService.getUserByUsername(request.getUsername());
                    response.put("code", 200);
                    response.put("message", "登录成功");
                    response.put("data", new LoginResponse(token, user));
                    return ResponseEntity.ok(response);
                } else {
                    response.put("code", 401);
                    response.put("message", "用户名或密码错误");
                    return ResponseEntity.ok(response);
                }
            } else if ("WECHAT".equals(request.getLoginType())) {
                // 微信登录
                User user = userService.wechatLogin(request.getCode());
                if (user != null) {
                    String token = jwtUtil.generateToken(user.getId(), user.getUsername() != null ? user.getUsername() : user.getOpenId(), user.getRole());
                    response.put("code", 200);
                    response.put("message", "登录成功");
                    response.put("data", new LoginResponse(token, user));
                    return ResponseEntity.ok(response);
                } else {
                    response.put("code", 401);
                    response.put("message", "微信登录失败");
                    return ResponseEntity.ok(response);
                }
            } else {
                response.put("code", 400);
                response.put("message", "不支持的登录类型");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User user = userService.validateTokenAndGetUser(token);
            if (user != null) {
                response.put("code", 200);
                response.put("message", "获取成功");
                response.put("data", user);
            } else {
                response.put("code", 401);
                response.put("message", "token无效");
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取用户信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody User userInfo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser != null) {
                // 更新允许的字段
                currentUser.setNickname(userInfo.getNickname());
                currentUser.setAvatarUrl(userInfo.getAvatarUrl());
                currentUser.setGender(userInfo.getGender());
                currentUser.setPhone(userInfo.getPhone());
                
                User updatedUser = userService.updateUser(currentUser);
                response.put("code", 200);
                response.put("message", "更新成功");
                response.put("data", updatedUser);
            } else {
                response.put("code", 401);
                response.put("message", "token无效");
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "更新用户信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> passwordData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser != null) {
                String oldPassword = passwordData.get("oldPassword");
                String newPassword = passwordData.get("newPassword");
                
                boolean success = userService.changePassword(currentUser.getId(), oldPassword, newPassword);
                if (success) {
                    response.put("code", 200);
                    response.put("message", "密码修改成功");
                } else {
                    response.put("code", 400);
                    response.put("message", "原密码错误");
                }
            } else {
                response.put("code", 401);
                response.put("message", "token无效");
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "修改密码失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "登出成功");
        return ResponseEntity.ok(response);
    }
}
