package com.printercloud.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * API服务
 * 统一处理与后端API的交互
 */
@Service
public class ApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AuthService authService;
    
    /**
     * 发送GET请求
     */
    public String get(String uri) {
        return get(uri, null);
    }
    
    /**
     * 发送带参数的GET请求
     */
    public String get(String uri, Map<String, Object> params) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(uri);
                    if (params != null) {
                        params.forEach(uriBuilder::queryParam);
                    }
                    return uriBuilder.build();
                });
            
            // 添加认证头
            if (authService.isLoggedIn()) {
                request = request.header("Authorization", "Bearer " + authService.getCurrentToken());
            }
            
            return request.retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
                
        } catch (WebClientResponseException e) {
            logger.error("GET请求失败 - uri: {}, status: {}, body: {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("GET请求失败 - uri: {}", uri, e);
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送POST请求
     */
    public String post(String uri, Object body) {
        try {
            WebClient.RequestBodySpec request = webClient.post().uri(uri);
            
            // 添加认证头
            if (authService.isLoggedIn()) {
                request = request.header("Authorization", "Bearer " + authService.getCurrentToken());
            }
            
            return request.bodyValue(body != null ? body : "{}")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
                
        } catch (WebClientResponseException e) {
            logger.error("POST请求失败 - uri: {}, status: {}, body: {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("POST请求失败 - uri: {}", uri, e);
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送PUT请求
     */
    public String put(String uri, Object body) {
        try {
            WebClient.RequestBodySpec request = webClient.put().uri(uri);
            
            // 添加认证头
            if (authService.isLoggedIn()) {
                request = request.header("Authorization", "Bearer " + authService.getCurrentToken());
            }
            
            return request.bodyValue(body != null ? body : "{}")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
                
        } catch (WebClientResponseException e) {
            logger.error("PUT请求失败 - uri: {}, status: {}, body: {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("PUT请求失败 - uri: {}", uri, e);
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送DELETE请求
     */
    public String delete(String uri) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.delete().uri(uri);
            
            // 添加认证头
            if (authService.isLoggedIn()) {
                request = request.header("Authorization", "Bearer " + authService.getCurrentToken());
            }
            
            return request.retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
                
        } catch (WebClientResponseException e) {
            logger.error("DELETE请求失败 - uri: {}, status: {}, body: {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("DELETE请求失败 - uri: {}", uri, e);
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析API响应
     */
    public JsonNode parseResponse(String response) {
        try {
            return objectMapper.readTree(response);
        } catch (Exception e) {
            logger.error("解析API响应失败", e);
            throw new RuntimeException("解析API响应失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查API响应是否成功
     */
    public boolean isSuccessResponse(String response) {
        try {
            JsonNode jsonNode = parseResponse(response);
            return jsonNode.has("code") && jsonNode.get("code").asInt() == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取API响应中的数据部分
     */
    public JsonNode getResponseData(String response) {
        try {
            JsonNode jsonNode = parseResponse(response);
            if (isSuccessResponse(response)) {
                return jsonNode.get("data");
            } else {
                String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "未知错误";
                throw new RuntimeException("API调用失败: " + message);
            }
        } catch (Exception e) {
            logger.error("获取API响应数据失败", e);
            throw new RuntimeException("获取API响应数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取API响应中的错误信息
     */
    public String getResponseMessage(String response) {
        try {
            JsonNode jsonNode = parseResponse(response);
            return jsonNode.has("message") ? jsonNode.get("message").asText() : "未知错误";
        } catch (Exception e) {
            return "解析错误信息失败";
        }
    }
}
