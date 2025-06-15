package com.printercloud.controller;

import com.printercloud.dto.CreateOrderRequest;
import com.printercloud.dto.OrderQueryRequest;
import com.printercloud.entity.PrintOrder;
import com.printercloud.service.PrintOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印订单控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // 允许跨域访问
public class PrintOrderController {

    @Autowired
    private PrintOrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            PrintOrder order = orderService.createOrder(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "订单创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取订单列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrderList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        try {
            OrderQueryRequest request = new OrderQueryRequest();
            request.setUserId(userId);
            request.setStatus(status);
            request.setSearch(search);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);
            request.setPage(page);
            request.setPageSize(pageSize);

            Page<PrintOrder> orderPage = orderService.getOrderList(request);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("success", true);
            response.put("message", "获取订单列表成功");

            Map<String, Object> data = new HashMap<>();
            data.put("content", orderPage.getContent());
            data.put("totalElements", orderPage.getTotalElements());
            data.put("totalPages", orderPage.getTotalPages());
            data.put("page", page);
            data.put("pageSize", pageSize);

            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "获取订单列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据ID获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id) {
        try {
            PrintOrder order = orderService.getOrderById(id);
            Map<String, Object> response = new HashMap<>();
            
            if (order != null) {
                response.put("success", true);
                response.put("data", order);
            } else {
                response.put("success", false);
                response.put("message", "订单不存在");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取订单详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据验证码查询订单
     */
    @GetMapping("/verify/{verifyCode}")
    public ResponseEntity<Map<String, Object>> getOrderByVerifyCode(@PathVariable String verifyCode) {
        try {
            List<PrintOrder> orders = orderService.searchByVerifyCode(verifyCode);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询订单失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam Integer status) {
        
        try {
            boolean success = orderService.updateOrderStatus(id, status);
            Map<String, Object> response = new HashMap<>();
            
            if (success) {
                response.put("success", true);
                response.put("message", "订单状态更新成功");
            } else {
                response.put("success", false);
                response.put("message", "订单不存在或更新失败");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新订单状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 验证码验证并完成订单
     */
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeOrder(@RequestParam String verifyCode) {
        try {
            boolean success = orderService.completeOrderByVerifyCode(verifyCode);
            Map<String, Object> response = new HashMap<>();
            
            if (success) {
                response.put("success", true);
                response.put("message", "订单完成成功");
            } else {
                response.put("success", false);
                response.put("message", "验证码无效或订单状态不正确");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "完成订单失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取今日订单
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayOrders() {
        try {
            List<PrintOrder> orders = orderService.getTodayOrders();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取今日订单失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取待处理订单
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingOrders() {
        try {
            List<PrintOrder> orders = orderService.getPendingOrders();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取待处理订单失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) Long userId) {

        try {
            Map<String, Object> statistics;
            if (userId != null) {
                statistics = orderService.getUserOrderStatistics(userId);
            } else {
                statistics = orderService.getOrderStatistics();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取订单统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@RequestParam Long orderId) {
        try {
            boolean success = orderService.cancelOrder(orderId);
            Map<String, Object> response = new HashMap<>();

            if (success) {
                response.put("success", true);
                response.put("message", "订单取消成功");
            } else {
                response.put("success", false);
                response.put("message", "订单状态不允许取消或订单不存在");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取消订单失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取最近订单
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<PrintOrder> orders = orderService.getRecentOrders(userId, limit);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取最近订单失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新订单状态（字符串参数版本）
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatusByString(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            boolean success = orderService.updateOrderStatusByString(id, status);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "订单状态更新成功");
            } else {
                response.put("success", false);
                response.put("message", "订单不存在或状态更新失败");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新订单状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
