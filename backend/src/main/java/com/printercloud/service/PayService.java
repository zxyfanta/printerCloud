package com.printercloud.service;

import com.printercloud.config.WechatConfig;
import com.printercloud.config.WechatPayConfig;
import com.printercloud.entity.PrintOrder;
import com.printercloud.repository.PrintOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 支付服务类
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class PayService {

    private static final Logger logger = LoggerFactory.getLogger(PayService.class);

    @Autowired
    private PrintOrderRepository orderRepository;

    @Autowired
    private PrintOrderService orderService;

    @Autowired
    private WechatConfig wechatConfig;

    @Autowired
    private WechatPayConfig wechatPayConfig;

    /**
     * 创建支付订单
     */
    public Map<String, Object> createPayment(Long orderId, boolean sandbox) {
        logger.info("创建支付订单: orderId={}, sandbox={}", orderId, sandbox);
        
        // 获取订单信息
        PrintOrder order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new IllegalArgumentException("订单状态异常，无法支付: " + order.getStatus());
        }
        
        // 生成支付参数
        return generatePaymentData(order, sandbox);
    }
    
    /**
     * 查询支付结果
     */
    public Map<String, Object> queryPayment(Long orderId) {
        logger.info("查询支付结果: orderId={}", orderId);
        
        // 获取订单信息
        PrintOrder order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }
        
        // 检查订单状态
        boolean isPaid = order.getStatus() > 0 && order.getPayTime() != null;
        
        Map<String, Object> paymentResult = new HashMap<>();
        paymentResult.put("status", isPaid ? "SUCCESS" : "WAITING");
        
        if (isPaid) {
            paymentResult.put("verifyCode", order.getVerifyCode());
            paymentResult.put("payTime", order.getPayTime());
        }
        
        return paymentResult;
    }
    
    /**
     * 处理支付回调通知
     */
    public boolean handlePaymentNotify(Map<String, Object> notifyData) {
        try {
            logger.info("处理支付回调通知: {}", notifyData);
            
            // 解析回调数据
            String outTradeNo = (String) notifyData.get("out_trade_no");
            String transactionId = (String) notifyData.get("transaction_id");
            String tradeState = (String) notifyData.get("trade_state");
            
            // 提取订单号
            Long orderId = extractOrderIdFromOutTradeNo(outTradeNo);
            
            // 获取订单信息
            PrintOrder order = orderService.getOrderById(orderId);
            if (order == null) {
                logger.error("支付回调通知处理失败: 订单不存在, orderId={}", orderId);
                return false;
            }
            
            // 检查订单状态
            if (order.getStatus() != 0) {
                logger.warn("支付回调通知: 订单状态异常, orderId={}, status={}", orderId, order.getStatus());
                return true; // 返回成功避免重复通知
            }
            
            // 处理支付成功
            if ("SUCCESS".equals(tradeState)) {
                // 更新订单状态为已支付
                orderService.updateOrderStatus(orderId, 1);
                logger.info("支付回调通知处理成功: 订单已支付, orderId={}", orderId);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("支付回调通知处理失败", e);
            return false;
        }
    }
    
    /**
     * 生成支付参数
     */
    private Map<String, Object> generatePaymentData(PrintOrder order, boolean sandbox) {
        Map<String, Object> paymentData = new HashMap<>();
        
        // 使用配置的沙盒环境设置
        boolean useSandbox = sandbox || wechatPayConfig.getSandboxEnabled();
        
        // 生成随机字符串
        String nonceStr = generateNonceStr();
        // 生成时间戳
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        // 生成商户订单号
        String outTradeNo = generateOutTradeNo(order.getId());
        
        // 设置支付参数
        paymentData.put("timeStamp", timeStamp);
        paymentData.put("nonceStr", nonceStr);
        paymentData.put("package", "prepay_id=wx" + timeStamp);
        paymentData.put("signType", "MD5");
        paymentData.put("paySign", generatePaySign(timeStamp, nonceStr, outTradeNo, useSandbox));
        
        // 沙盒环境标记
        paymentData.put("sandbox", useSandbox);
        
        // 记录日志
        logger.info("生成支付参数: orderId={}, outTradeNo={}, sandbox={}", 
                order.getId(), outTradeNo, useSandbox);
        
        return paymentData;
    }
    
    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * 生成商户订单号
     */
    private String generateOutTradeNo(Long orderId) {
        return "PC" + System.currentTimeMillis() + orderId;
    }
    
    /**
     * 从商户订单号中提取订单ID
     */
    private Long extractOrderIdFromOutTradeNo(String outTradeNo) {
        // 假设格式为PC + 时间戳 + 订单ID
        if (outTradeNo != null && outTradeNo.startsWith("PC") && outTradeNo.length() > 15) {
            return Long.parseLong(outTradeNo.substring(15));
        }
        throw new IllegalArgumentException("无效的商户订单号: " + outTradeNo);
    }
    
    /**
     * 生成支付签名
     */
    private String generatePaySign(String timeStamp, String nonceStr, String outTradeNo, boolean sandbox) {
        // 实际项目中应使用微信支付API生成签名
        // 这里简单模拟一个签名
        String signStr = timeStamp + nonceStr + outTradeNo + (sandbox ? "SANDBOX" : "PROD");
        return String.valueOf(Math.abs(signStr.hashCode()));
    }
}