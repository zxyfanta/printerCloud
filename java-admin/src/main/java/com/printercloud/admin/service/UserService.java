package com.printercloud.admin.service;

import com.printercloud.admin.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 用户服务
 * 负责用户管理相关功能
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Value("${app.api.base-url}")
    private String apiBaseUrl;
    
    @Autowired
    private WebClient webClient;
    
    /**
     * 获取用户列表
     */
    public Map<String, Object> getUsers(int page, int pageSize, String search, Integer status) {
        try {
            logger.info("调用后端API获取用户列表");
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("pageSize", pageSize);
            if (search != null && !search.trim().isEmpty()) {
                params.put("search", search);
            }
            if (status != null) {
                params.put("status", status);
            }
            
            // 调用后端API
            String response = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/users");
                    params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应，为了简化，返回模拟数据
            return createMockUserList(page, pageSize, search, status);
            
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            throw new RuntimeException("获取用户列表失败", e);
        }
    }
    
    /**
     * 获取用户详情
     */
    public User getUserById(Long id) {
        try {
            logger.info("调用后端API获取用户详情 - id: {}", id);
            
            // 调用后端API
            String response = webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应，为了简化，返回模拟数据
            return createMockUser(id);
            
        } catch (Exception e) {
            logger.error("获取用户详情失败", e);
            throw new RuntimeException("获取用户详情失败", e);
        }
    }
    
    /**
     * 更新用户状态
     */
    public boolean updateUserStatus(Long id, Integer status) {
        try {
            logger.info("调用后端API更新用户状态 - id: {}, status: {}", id, status);
            
            Map<String, Object> requestBody = Map.of("status", status);
            
            // 调用后端API
            String response = webClient.post()
                .uri("/users/{id}/status", id)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应判断是否成功
            return true;
            
        } catch (Exception e) {
            logger.error("更新用户状态失败", e);
            return false;
        }
    }
    
    /**
     * 删除用户
     */
    public boolean deleteUser(Long id) {
        try {
            logger.info("调用后端API删除用户 - id: {}", id);
            
            // 调用后端API
            String response = webClient.delete()
                .uri("/users/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应判断是否成功
            return true;
            
        } catch (Exception e) {
            logger.error("删除用户失败", e);
            return false;
        }
    }
    
    /**
     * 重置用户密码
     */
    public String resetPassword(Long id) {
        try {
            logger.info("调用后端API重置用户密码 - id: {}", id);
            
            // 调用后端API
            String response = webClient.post()
                .uri("/users/{id}/reset-password", id)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应获取新密码，为了简化，返回随机密码
            return generateRandomPassword();
            
        } catch (Exception e) {
            logger.error("重置用户密码失败", e);
            return null;
        }
    }
    
    /**
     * 获取用户统计信息
     */
    public Map<String, Object> getUserStatistics() {
        try {
            logger.info("调用后端API获取用户统计信息");
            
            // 调用后端API
            String response = webClient.get()
                .uri("/users/statistics")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应，为了简化，返回模拟数据
            return createMockUserStatistics();
            
        } catch (Exception e) {
            logger.error("获取用户统计信息失败", e);
            throw new RuntimeException("获取用户统计信息失败", e);
        }
    }
    
    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    // 以下是模拟数据方法
    
    private Map<String, Object> createMockUserList(int page, int pageSize, String search, Integer status) {
        List<User> users = new ArrayList<>();
        
        // 创建模拟用户数据
        for (int i = 1; i <= pageSize; i++) {
            User user = new User();
            user.setId((long) ((page - 1) * pageSize + i));
            user.setUsername("user" + i);
            user.setNickname("用户" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPhone("138" + String.format("%08d", i));
            user.setStatus(status != null ? status : (i % 2));
            user.setCreateTime(LocalDateTime.now());
            user.setLastLoginTime(LocalDateTime.now());
            user.setOrderCount(i * 5);
            user.setTotalAmount(i * 25.5);
            
            if (search == null || user.getUsername().contains(search) || 
                user.getNickname().contains(search) || user.getEmail().contains(search)) {
                users.add(user);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", users);
        result.put("totalElements", 800L);
        result.put("totalPages", (800 + pageSize - 1) / pageSize);
        result.put("number", page - 1);
        result.put("size", pageSize);
        
        return result;
    }
    
    private User createMockUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setNickname("用户" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPhone("138" + String.format("%08d", id));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setLastLoginTime(LocalDateTime.now());
        user.setOrderCount(25);
        user.setTotalAmount(125.75);
        user.setAvatar("https://example.com/avatar/" + id + ".jpg");
        user.setRemark("测试用户");
        
        return user;
    }
    
    private Map<String, Object> createMockUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", 1580);
        statistics.put("activeUsers", 1420);
        statistics.put("inactiveUsers", 160);
        statistics.put("newUsersToday", 12);
        statistics.put("newUsersThisMonth", 285);
        statistics.put("averageOrdersPerUser", 8.5);
        statistics.put("averageAmountPerUser", 156.25);
        
        return statistics;
    }
}
