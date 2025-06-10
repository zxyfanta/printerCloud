package com.printercloud.service;

import com.printercloud.dto.CreateOrderRequest;
import com.printercloud.dto.OrderQueryRequest;
import com.printercloud.entity.PrintOrder;
import com.printercloud.repository.PrintOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 打印订单服务类
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
@Transactional
public class PrintOrderService {

    @Autowired
    private PrintOrderRepository orderRepository;

    /**
     * 创建订单
     */
    public PrintOrder createOrder(CreateOrderRequest request) {
        PrintOrder order = new PrintOrder();
        
        // 生成订单号
        order.setOrderNo(generateOrderNo());
        
        // 生成验证码
        order.setVerifyCode(generateVerifyCode());
        
        // 设置订单信息
        order.setUserId(request.getUserId());
        order.setUserName(request.getUserName());
        order.setFileName(request.getFileName());
        order.setFileType(request.getFileType());
        order.setCopies(request.getCopies());
        order.setPageRange(request.getPageRange());
        order.setActualPages(request.getActualPages());
        order.setIsColor(request.getIsColor());
        order.setIsDoubleSide(request.getIsDoubleSide());
        order.setPaperSize(request.getPaperSize());
        order.setRemark(request.getRemark());
        order.setAmount(request.getAmount());
        order.setStatus(0); // 待支付
        
        return orderRepository.save(order);
    }

    /**
     * 获取订单列表
     */
    public Page<PrintOrder> getOrderList(OrderQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize());
        
        if (request.getUserId() != null && request.getStatus() != null) {
            return orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(
                request.getUserId(), request.getStatus(), pageable);
        } else if (request.getUserId() != null) {
            return orderRepository.findByUserIdOrderByCreateTimeDesc(request.getUserId(), pageable);
        } else if (request.getStatus() != null) {
            return orderRepository.findByStatusOrderByCreateTimeDesc(request.getStatus(), pageable);
        } else {
            return orderRepository.findAllByOrderByCreateTimeDesc(pageable);
        }
    }

    /**
     * 根据ID获取订单详情
     */
    public PrintOrder getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    /**
     * 根据订单号获取订单
     */
    public PrintOrder getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo).orElse(null);
    }

    /**
     * 根据验证码获取订单
     */
    public PrintOrder getOrderByVerifyCode(String verifyCode) {
        return orderRepository.findByVerifyCode(verifyCode).orElse(null);
    }

    /**
     * 验证码查询订单（支持模糊查询）
     */
    public List<PrintOrder> searchByVerifyCode(String verifyCode) {
        if (verifyCode.length() == 6) {
            // 精确查询
            PrintOrder order = orderRepository.findByVerifyCode(verifyCode).orElse(null);
            return order != null ? Arrays.asList(order) : new ArrayList<>();
        } else {
            // 模糊查询
            return orderRepository.findByVerifyCodeContaining(verifyCode);
        }
    }

    /**
     * 更新订单状态
     */
    public boolean updateOrderStatus(Long orderId, Integer status) {
        Optional<PrintOrder> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            PrintOrder order = optionalOrder.get();
            order.setStatus(status);
            
            // 设置相应的时间
            if (status == 1) { // 已支付
                order.setPayTime(LocalDateTime.now());
            } else if (status == 3) { // 已完成
                order.setFinishTime(LocalDateTime.now());
            }
            
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    /**
     * 验证码验证并完成订单
     */
    public boolean completeOrderByVerifyCode(String verifyCode) {
        Optional<PrintOrder> optionalOrder = orderRepository.findByVerifyCode(verifyCode);
        if (optionalOrder.isPresent()) {
            PrintOrder order = optionalOrder.get();
            if (order.getStatus() == 1 || order.getStatus() == 2) { // 已支付或打印中
                order.setStatus(3); // 已完成
                order.setFinishTime(LocalDateTime.now());
                orderRepository.save(order);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取今日订单
     */
    public List<PrintOrder> getTodayOrders() {
        return orderRepository.findTodayOrders();
    }

    /**
     * 获取待处理订单
     */
    public List<PrintOrder> getPendingOrders() {
        return orderRepository.findPendingOrders();
    }

    /**
     * 获取订单统计
     */
    public Map<String, Object> getOrderStatistics() {
        List<Object[]> statusCounts = orderRepository.countByStatus();
        Map<String, Object> statistics = new HashMap<>();
        
        // 初始化统计数据
        statistics.put("total", 0L);
        statistics.put("pending", 0L);    // 待支付
        statistics.put("paid", 0L);       // 已支付
        statistics.put("printing", 0L);   // 打印中
        statistics.put("completed", 0L);  // 已完成
        statistics.put("cancelled", 0L);  // 已取消
        statistics.put("refunded", 0L);   // 已退款
        
        long total = 0;
        for (Object[] row : statusCounts) {
            Integer status = (Integer) row[0];
            Long count = (Long) row[1];
            total += count;
            
            switch (status) {
                case 0: statistics.put("pending", count); break;
                case 1: statistics.put("paid", count); break;
                case 2: statistics.put("printing", count); break;
                case 3: statistics.put("completed", count); break;
                case 4: statistics.put("cancelled", count); break;
                case 5: statistics.put("refunded", count); break;
            }
        }
        
        statistics.put("total", total);
        return statistics;
    }

    /**
     * 获取用户订单统计
     */
    public Map<String, Object> getUserOrderStatistics(Long userId) {
        List<Object[]> statusCounts = orderRepository.countByUserIdAndStatus(userId);
        Map<String, Object> statistics = new HashMap<>();
        
        // 初始化统计数据
        statistics.put("total", 0L);
        statistics.put("pending", 0L);
        statistics.put("paid", 0L);
        statistics.put("printing", 0L);
        statistics.put("completed", 0L);
        statistics.put("cancelled", 0L);
        statistics.put("refunded", 0L);
        
        long total = 0;
        for (Object[] row : statusCounts) {
            Integer status = (Integer) row[0];
            Long count = (Long) row[1];
            total += count;
            
            switch (status) {
                case 0: statistics.put("pending", count); break;
                case 1: statistics.put("paid", count); break;
                case 2: statistics.put("printing", count); break;
                case 3: statistics.put("completed", count); break;
                case 4: statistics.put("cancelled", count); break;
                case 5: statistics.put("refunded", count); break;
            }
        }
        
        statistics.put("total", total);
        return statistics;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "PC" + timestamp + random;
    }

    /**
     * 生成验证码
     */
    private String generateVerifyCode() {
        String code;
        do {
            code = String.format("%06d", new Random().nextInt(1000000));
        } while (orderRepository.findByVerifyCode(code).isPresent());
        return code;
    }
}
