package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.dto.LoginRequest;
import com.printercloud.entity.User;
import com.printercloud.service.UserService;
import com.printercloud.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 统一登录接口
     */
    @Operation(summary = "用户登录", description = "支持管理员登录和微信登录")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "认证失败"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/login")
    public R<Map<String, Object>> login(
            @Parameter(description = "登录请求参数", required = true) @RequestBody LoginRequest request) {

        // 添加调试日志
        System.out.println("收到登录请求，loginType: " + request.getLoginType() + ", code: " + request.getCode());

        try {
            if ("ADMIN".equals(request.getLoginType())) {
                // 管理员登录
                String token = userService.adminLogin(request.getUsername(), request.getPassword());
                if (token != null) {
                    User user = userService.getUserByUsername(request.getUsername());
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("userInfo", user);
                    return R.ok(data, "登录成功");
                } else {
                    return R.unauthorized("用户名或密码错误");
                }
            } else if ("WECHAT".equals(request.getLoginType())) {
                // 微信登录
                User user = userService.wechatLogin(request.getCode());
                if (user != null) {
                    String token = jwtUtil.generateToken(user.getId(), user.getUsername() != null ? user.getUsername() : user.getOpenId(), user.getRole());
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("userInfo", user);
                    return R.ok(data, "登录成功");
                } else {
                    return R.unauthorized("微信登录失败");
                }
            } else {
                return R.validateFailed("不支持的登录类型");
            }
        } catch (Exception e) {
            return R.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据token获取当前登录用户的详细信息")
    @GetMapping("/userinfo")
    public R<User> getUserInfo(
            @Parameter(description = "JWT Token", required = true) @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User user = userService.validateTokenAndGetUser(token);
            if (user != null) {
                return R.ok(user, "获取成功");
            } else {
                return R.unauthorized("token无效");
            }
        } catch (Exception e) {
            return R.fail("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public R<User> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody User userInfo) {
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
                return R.ok(updatedUser, "更新成功");
            } else {
                return R.unauthorized("token无效");
            }
        } catch (Exception e) {
            return R.fail("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public R<Void> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> passwordData) {
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
                    return R.ok(null, "密码修改成功");
                } else {
                    return R.validateFailed("原密码错误");
                }
            } else {
                return R.unauthorized("token无效");
            }
        } catch (Exception e) {
            return R.fail("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/user/stats")
    public R<Map<String, Object>> getUserStats(
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser != null) {
                Map<String, Object> stats = userService.getUserStatistics(currentUser.getId());
                return R.ok(stats, "获取成功");
            } else {
                return R.unauthorized("token无效");
            }
        } catch (Exception e) {
            return R.fail("获取用户统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok(null, "登出成功");
    }
}
