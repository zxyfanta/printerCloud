package com.printercloud.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printercloud.admin.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 订单服务
 * 负责与后端API交互，处理订单相关业务逻辑
 */
@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Value("${app.api.base-url}")
    private String apiBaseUrl;
    
    @Autowired
    private ApiService apiService;

    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 获取订单列表
     */
    public Map<String, Object> getOrders(int page, int pageSize, String search, Integer status,
                                        String sortBy, String sortDirection) {
        try {
            logger.info("调用后端API获取订单列表");

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
            params.put("sortBy", sortBy);
            params.put("sortDirection", sortDirection);

            // 调用后端API
            String response = apiService.get("/orders", params);

            if (apiService.isSuccessResponse(response)) {
                JsonNode data = apiService.getResponseData(response);
                return objectMapper.convertValue(data, Map.class);
            } else {
                // 如果API调用失败，返回模拟数据
                logger.warn("API调用失败，返回模拟数据: {}", apiService.getResponseMessage(response));
                return createMockOrderList(page, pageSize, search, status);
            }

        } catch (Exception e) {
            logger.error("获取订单列表失败", e);
            // 返回模拟数据作为降级处理
            return createMockOrderList(page, pageSize, search, status);
        }
    }
    
    /**
     * 获取订单详情
     */
    public Order getOrderById(Long id) {
        try {
            logger.info("调用后端API获取订单详情 - id: {}", id);

            // 调用后端API
            String response = apiService.get("/orders/" + id);

            if (apiService.isSuccessResponse(response)) {
                JsonNode data = apiService.getResponseData(response);
                return objectMapper.treeToValue(data, Order.class);
            } else {
                logger.warn("API调用失败，返回模拟数据: {}", apiService.getResponseMessage(response));
                return createMockOrder(id);
            }

        } catch (Exception e) {
            logger.error("获取订单详情失败", e);
            return createMockOrder(id);
        }
    }
    
    /**
     * 完成订单
     */
    public boolean completeOrder(String verifyCode) {
        try {
            logger.info("调用后端API完成订单 - verifyCode: {}", verifyCode);

            // 调用后端API
            Map<String, Object> params = Map.of("verifyCode", verifyCode);
            String response = apiService.post("/orders/complete", params);

            return apiService.isSuccessResponse(response);

        } catch (Exception e) {
            logger.error("完成订单失败", e);
            return false;
        }
    }
    
    /**
     * 取消订单
     */
    public boolean cancelOrder(Long id) {
        try {
            logger.info("调用后端API取消订单 - id: {}", id);

            // 调用后端API
            String response = apiService.post("/orders/" + id + "/cancel", null);

            return apiService.isSuccessResponse(response);

        } catch (Exception e) {
            logger.error("取消订单失败", e);
            return false;
        }
    }
    
    /**
     * 更新订单状态
     */
    public boolean updateOrderStatus(Long id, String status) {
        try {
            logger.info("调用后端API更新订单状态 - id: {}, status: {}", id, status);

            Map<String, Object> requestBody = Map.of("status", status);

            // 调用后端API
            String response = apiService.post("/orders/" + id + "/status", requestBody);

            return apiService.isSuccessResponse(response);

        } catch (Exception e) {
            logger.error("更新订单状态失败", e);
            return false;
        }
    }
    
    /**
     * 获取订单统计信息
     */
    public Map<String, Object> getOrderStatistics() {
        try {
            logger.info("调用后端API获取订单统计信息");

            // 调用后端API
            String response = apiService.get("/orders/statistics");

            if (apiService.isSuccessResponse(response)) {
                JsonNode data = apiService.getResponseData(response);
                return objectMapper.convertValue(data, Map.class);
            } else {
                logger.warn("API调用失败，返回模拟数据: {}", apiService.getResponseMessage(response));
                return createMockStatistics();
            }

        } catch (Exception e) {
            logger.error("获取订单统计信息失败", e);
            return createMockStatistics();
        }
    }
    
    // 以下是模拟数据方法，实际应该解析后端API响应
    
    private Map<String, Object> createMockOrderList(int page, int pageSize, String search, Integer status) {
        List<Order> orders = new ArrayList<>();
        
        // 创建模拟订单数据
        for (int i = 1; i <= pageSize; i++) {
            Order order = new Order();
            order.setId((long) ((page - 1) * pageSize + i));
            order.setOrderNo("ORD" + System.currentTimeMillis() + i);
            order.setFileName("document_" + i + ".pdf");
            order.setFileType("pdf");
            order.setCopies(1);
            order.setActualPages(5);
            order.setAmount(2.50);
            order.setVerifyCode("VER" + (1000 + i));
            order.setStatus(status != null ? status : (i % 4));
            order.setIsColor(i % 3 == 0);
            order.setIsDoubleSide(i % 2 == 0);
            order.setPaperSize("A4");
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            
            if (search == null || order.getOrderNo().contains(search) || order.getVerifyCode().contains(search)) {
                orders.add(order);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", orders);
        result.put("totalElements", 100L);
        result.put("totalPages", (100 + pageSize - 1) / pageSize);
        result.put("number", page - 1);
        result.put("size", pageSize);
        
        return result;
    }
    
    private Order createMockOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setFileName("document_" + id + ".pdf");
        order.setFileType("pdf");
        order.setCopies(1);
        order.setActualPages(5);
        order.setAmount(2.50);
        order.setVerifyCode("VER" + (1000 + id));
        order.setStatus(1);
        order.setIsColor(false);
        order.setIsDoubleSide(true);
        order.setPaperSize("A4");
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setUserName("测试用户");
        order.setRemark("测试订单");
        
        return order;
    }
    
    private Map<String, Object> createMockStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalOrders", 1250);
        statistics.put("pendingOrders", 45);
        statistics.put("completedOrders", 1180);
        statistics.put("cancelledOrders", 25);
        statistics.put("todayOrders", 28);
        statistics.put("todayRevenue", 156.50);
        statistics.put("monthlyRevenue", 3250.75);
        
        return statistics;
    }
}
