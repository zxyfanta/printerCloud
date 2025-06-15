package com.printercloud.admin.controller;

import com.printercloud.admin.model.Order;
import com.printercloud.admin.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 * 提供订单查询、状态更新等功能
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 获取订单列表
     */
    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        try {
            logger.info("获取订单列表 - page: {}, pageSize: {}, search: {}, status: {}", 
                       page, pageSize, search, status);
            
            Map<String, Object> result = orderService.getOrders(page, pageSize, search, status, sortBy, sortDirection);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", result
            ));
            
        } catch (Exception e) {
            logger.error("获取订单列表失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取订单列表失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            logger.info("获取订单详情 - id: {}", id);
            
            Order order = orderService.getOrderById(id);
            if (order != null) {
                return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "success",
                    "data", order
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "code", 404,
                    "message", "订单不存在"
                ));
            }
            
        } catch (Exception e) {
            logger.error("获取订单详情失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取订单详情失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 完成订单
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeOrder(@RequestParam String verifyCode) {
        try {
            logger.info("完成订单 - verifyCode: {}", verifyCode);
            
            boolean success = orderService.completeOrder(verifyCode);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "订单完成成功"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "订单完成失败，请检查验证码"
                ));
            }
            
        } catch (Exception e) {
            logger.error("完成订单失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "完成订单失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            logger.info("取消订单 - id: {}", id);
            
            boolean success = orderService.cancelOrder(id);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "订单取消成功"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "订单取消失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("取消订单失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "取消订单失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新订单状态
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String status = (String) request.get("status");
            logger.info("更新订单状态 - id: {}, status: {}", id, status);
            
            boolean success = orderService.updateOrderStatus(id, status);
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
            logger.error("更新订单状态失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "状态更新失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取订单统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getOrderStatistics() {
        try {
            logger.info("获取订单统计信息");
            
            Map<String, Object> statistics = orderService.getOrderStatistics();
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", statistics
            ));
            
        } catch (Exception e) {
            logger.error("获取订单统计信息失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }
}
