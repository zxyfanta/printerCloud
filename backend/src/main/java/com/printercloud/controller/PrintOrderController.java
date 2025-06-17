package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.dto.CreateOrderRequest;
import com.printercloud.dto.OrderQueryRequest;
import com.printercloud.entity.PrintOrder;
import com.printercloud.service.PrintOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public R<PrintOrder> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            // 参数验证
            if (request.getUserId() == null) {
                return R.validateFailed("用户ID不能为空");
            }

            if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
                return R.validateFailed("文件名不能为空");
            }

            if (request.getCopies() == null || request.getCopies() <= 0) {
                return R.validateFailed("打印份数必须大于0");
            }

            if (request.getActualPages() == null || request.getActualPages() <= 0) {
                return R.validateFailed("打印页数必须大于0");
            }

            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                return R.validateFailed("订单金额不能为负数");
            }

            PrintOrder order = orderService.createOrder(request);
            return R.ok(order, "订单创建成功");

        } catch (IllegalArgumentException e) {
            return R.validateFailed("参数错误: " + e.getMessage());
        } catch (Exception e) {
            return R.fail("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单列表
     */
    @GetMapping
    public R<Map<String, Object>> getOrderList(
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

            Map<String, Object> data = new HashMap<>();
            data.put("content", orderPage.getContent());
            data.put("totalElements", orderPage.getTotalElements());
            data.put("totalPages", orderPage.getTotalPages());
            data.put("page", page);
            data.put("pageSize", pageSize);

            return R.ok(data, "获取订单列表成功");
        } catch (Exception e) {
            return R.fail("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取订单
     */
    @GetMapping("/{id}")
    public R<PrintOrder> getOrderById(@PathVariable Long id) {
        try {
            PrintOrder order = orderService.getOrderById(id);
            if (order == null) {
                return R.fail("订单不存在", 404);
            }

            return R.ok(order, "获取订单成功");
        } catch (Exception e) {
            return R.fail("获取订单失败: " + e.getMessage());
        }
    }

    /**
     * 根据验证码查询订单
     */
    @GetMapping("/verify/{verifyCode}")
    public R<List<PrintOrder>> getOrderByVerifyCode(@PathVariable String verifyCode) {
        try {
            List<PrintOrder> orders = orderService.searchByVerifyCode(verifyCode);
            return R.ok(orders, "查询订单成功");
        } catch (Exception e) {
            return R.fail("查询订单失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status/{status}")
    public R<Void> updateOrderStatus(
            @PathVariable Long id,
            @PathVariable Integer status) {
        try {
            boolean success = orderService.updateOrderStatus(id, status);

            if (success) {
                return R.ok(null, "更新订单状态成功");
            } else {
                return R.fail("订单不存在或状态更新失败", 404);
            }
        } catch (Exception e) {
            return R.fail("更新订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 验证码完成订单
     */
    @PutMapping("/complete/{verifyCode}")
    public R<Void> completeOrder(@PathVariable String verifyCode) {
        try {
            boolean success = orderService.completeOrderByVerifyCode(verifyCode);

            if (success) {
                return R.ok(null, "订单完成成功");
            } else {
                return R.validateFailed("订单不存在或状态不正确");
            }
        } catch (Exception e) {
            return R.fail("订单完成失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日订单
     */
    @GetMapping("/today")
    public R<List<PrintOrder>> getTodayOrders() {
        try {
            List<PrintOrder> orders = orderService.getTodayOrders();
            return R.ok(orders, "获取今日订单成功");
        } catch (Exception e) {
            return R.fail("获取今日订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理订单
     */
    @GetMapping("/pending")
    public R<List<PrintOrder>> getPendingOrders() {
        try {
            List<PrintOrder> orders = orderService.getPendingOrders();
            return R.ok(orders, "获取待处理订单成功");
        } catch (Exception e) {
            return R.fail("获取待处理订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/statistics")
    public R<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) Long userId) {

        try {
            Map<String, Object> statistics;
            if (userId != null) {
                statistics = orderService.getUserOrderStatistics(userId);
            } else {
                statistics = orderService.getOrderStatistics();
            }

            return R.ok(statistics, "获取订单统计信息成功");
        } catch (Exception e) {
            return R.fail("获取订单统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public R<Void> cancelOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.cancelOrder(id);

            if (success) {
                return R.ok(null, "订单取消成功");
            } else {
                return R.validateFailed("订单不存在或状态不允许取消");
            }
        } catch (Exception e) {
            return R.fail("订单取消失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近订单
     */
    @GetMapping("/recent")
    public R<List<PrintOrder>> getRecentOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<PrintOrder> orders = orderService.getRecentOrders(userId, limit);
            return R.ok(orders, "获取最近订单成功");
        } catch (Exception e) {
            return R.fail("获取最近订单失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态名称更新订单状态
     */
    @PutMapping("/{id}/status/name/{statusName}")
    public R<Void> updateOrderStatusByString(
            @PathVariable Long id,
            @PathVariable String statusName) {
        try {
            boolean success = orderService.updateOrderStatusByString(id, statusName);

            if (success) {
                return R.ok(null, "更新订单状态成功");
            } else {
                return R.validateFailed("订单不存在或状态名称无效");
            }
        } catch (Exception e) {
            return R.fail("更新订单状态失败: " + e.getMessage());
        }
    }
}
