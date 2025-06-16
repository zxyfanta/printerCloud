package com.printercloud.controller;

import com.printercloud.entity.PrintOrder;
import com.printercloud.service.PrintOrderService;
import com.printercloud.service.PayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 支付控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PayController {

    private static final Logger logger = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private PrintOrderService printOrderService;
    
    @Autowired
    private PayService payService;

    /**
     * 创建支付订单
     */
    @PostMapping("/pay/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long orderId = Long.parseLong(requestBody.get("orderId").toString());
            Boolean sandbox = requestBody.containsKey("sandbox") ? (Boolean) requestBody.get("sandbox") : false;
            
            logger.info("创建支付订单: orderId={}, sandbox={}", orderId, sandbox);
            
            // 调用支付服务创建支付订单
            Map<String, Object> paymentData = payService.createPayment(orderId, sandbox);
            
            response.put("code", 200);
            response.put("message", "创建支付订单成功");
            response.put("data", paymentData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("创建支付订单失败", e);
            response.put("code", 500);
            response.put("message", "创建支付订单失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 查询支付结果
     */
    @GetMapping("/pay/query/{orderId}")
    public ResponseEntity<Map<String, Object>> queryPayment(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("查询支付结果: orderId={}", orderId);
            
            // 调用支付服务查询支付结果
            Map<String, Object> paymentResult = payService.queryPayment(orderId);
            
            response.put("code", 200);
            response.put("message", "查询支付结果成功");
            response.put("data", paymentResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询支付结果失败", e);
            response.put("code", 500);
            response.put("message", "查询支付结果失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 支付回调通知
     */
    @PostMapping("/pay/notify")
    public ResponseEntity<String> paymentNotify(@RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("接收到支付回调通知: {}", requestBody);
            
            // 调用支付服务处理支付回调
            boolean success = payService.handlePaymentNotify(requestBody);
            
            return ResponseEntity.ok(success ? "SUCCESS" : "FAIL");
            
        } catch (Exception e) {
            logger.error("支付回调通知处理失败", e);
            return ResponseEntity.ok("FAIL");
        }
    }
}