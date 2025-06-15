package com.printercloud.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printercloud.admin.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 * 处理用户登录、token管理等认证相关功能
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String currentToken;
    private User currentUser;
    
    /**
     * 管理员登录
     */
    public boolean login(String username, String password) {
        try {
            logger.info("尝试管理员登录 - username: {}", username);
            
            Map<String, Object> loginRequest = new HashMap<>();
            loginRequest.put("username", username);
            loginRequest.put("password", password);
            loginRequest.put("loginType", "ADMIN");
            
            String response = webClient.post()
                .uri("/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.get("code").asInt() == 200) {
                    JsonNode data = jsonNode.get("data");
                    this.currentToken = data.get("token").asText();
                    
                    // 解析用户信息
                    JsonNode userInfo = data.get("userInfo");
                    this.currentUser = objectMapper.treeToValue(userInfo, User.class);
                    
                    logger.info("登录成功 - user: {}", currentUser.getUsername());
                    return true;
                } else {
                    logger.warn("登录失败 - message: {}", jsonNode.get("message").asText());
                }
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("登录失败", e);
            return false;
        }
    }
    
    /**
     * 登出
     */
    public void logout() {
        logger.info("用户登出");
        this.currentToken = null;
        this.currentUser = null;
    }
    
    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return currentToken != null && currentUser != null;
    }
    
    /**
     * 获取当前token
     */
    public String getCurrentToken() {
        return currentToken;
    }
    
    /**
     * 获取当前用户
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 验证token有效性
     */
    public boolean validateToken() {
        if (currentToken == null) {
            return false;
        }
        
        try {
            String response = webClient.get()
                .uri("/auth/userinfo")
                .header("Authorization", "Bearer " + currentToken)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                return jsonNode.get("code").asInt() == 200;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("验证token失败", e);
            return false;
        }
    }
    
    /**
     * 刷新用户信息
     */
    public boolean refreshUserInfo() {
        if (currentToken == null) {
            return false;
        }
        
        try {
            String response = webClient.get()
                .uri("/auth/userinfo")
                .header("Authorization", "Bearer " + currentToken)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.get("code").asInt() == 200) {
                    JsonNode data = jsonNode.get("data");
                    this.currentUser = objectMapper.treeToValue(data, User.class);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("刷新用户信息失败", e);
            return false;
        }
    }
}
