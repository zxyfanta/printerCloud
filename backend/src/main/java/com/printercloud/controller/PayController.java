package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.service.PayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    private PayService payService;

    /**
     * 创建支付订单
     */
    @PostMapping("/pay/create")
    public R<Map<String, Object>> createPayment(@RequestBody Map<String, Object> requestBody) {
        try {
            Long orderId = Long.parseLong(requestBody.get("orderId").toString());
            Boolean sandbox = requestBody.containsKey("sandbox") ? (Boolean) requestBody.get("sandbox") : false;

            logger.info("创建支付订单: orderId={}, sandbox={}", orderId, sandbox);

            // 调用支付服务创建支付订单
            Map<String, Object> paymentData = payService.createPayment(orderId, sandbox);

            return R.ok(paymentData, "创建支付订单成功");
        } catch (Exception e) {
            logger.error("创建支付订单失败", e);
            return R.fail("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付结果
     */
    @GetMapping("/pay/query/{orderId}")
    public R<Map<String, Object>> queryPayment(@PathVariable Long orderId) {
        try {
            logger.info("查询支付结果: orderId={}", orderId);

            // 调用支付服务查询支付结果
            Map<String, Object> paymentResult = payService.queryPayment(orderId);

            return R.ok(paymentResult, "查询支付结果成功");
        } catch (Exception e) {
            logger.error("查询支付结果失败", e);
            return R.fail("查询支付结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 支付回调通知
     */
    @PostMapping("/pay/notify")
    public R<String> paymentNotify(@RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("接收到支付回调通知: {}", requestBody);

            // 调用支付服务处理支付回调
            boolean success = payService.handlePaymentNotify(requestBody);

            return R.ok(success ? "SUCCESS" : "FAIL", "支付回调处理完成");

        } catch (Exception e) {
            logger.error("支付回调通知处理失败", e);
            return R.fail("FAIL");
        }
    }
}