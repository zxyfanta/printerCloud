package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.entity.User;
import com.printercloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public R<Map<String, Object>> getUserList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {

        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            // 构建分页和排序
            Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            // 获取用户列表
            Page<User> userPage = userService.getUserList(pageable, search, role, status);

            Map<String, Object> data = new HashMap<>();
            data.put("content", userPage.getContent());
            data.put("totalElements", userPage.getTotalElements());
            data.put("totalPages", userPage.getTotalPages());
            data.put("page", page);
            data.put("pageSize", size);

            return R.ok(data, "获取用户列表成功");

        } catch (Exception e) {
            return R.fail("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public R<User> getUserById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            User user = userService.getUserById(id);
            if (user != null) {
                return R.ok(user, "获取用户详情成功");
            } else {
                return R.notFound("用户不存在");
            }

        } catch (Exception e) {
            return R.fail("获取用户详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PostMapping("/{id}/status")
    public R<User> updateUserStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam Integer status) {

        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            User user = userService.getUserById(id);
            if (user != null) {
                user.setStatus(status);
                userService.updateUser(user);
                return R.ok(user, "用户状态更新成功");
            } else {
                return R.notFound("用户不存在");
            }

        } catch (Exception e) {
            return R.fail("更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    public R<Void> resetPassword(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            boolean success = userService.resetPassword(id, "123456");
            if (success) {
                return R.ok(null, "密码重置成功，新密码为：123456");
            } else {
                return R.notFound("用户不存在");
            }

        } catch (Exception e) {
            return R.fail("重置密码失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户（软删除）
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isSuperAdmin()) {
                return R.forbidden("无权限访问");
            }

            userService.deleteUser(id);
            return R.ok(null, "用户删除成功");

        } catch (Exception e) {
            return R.fail("删除用户失败: " + e.getMessage());
        }
    }
}
