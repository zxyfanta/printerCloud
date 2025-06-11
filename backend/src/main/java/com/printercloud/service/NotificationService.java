package com.printercloud.service;

import com.printercloud.entity.PrintOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知服务
 * 用于发送实时通知
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 发送新订单通知
     */
    public void sendNewOrderNotification(PrintOrder order) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_ORDER");
        notification.put("title", "新订单提醒");
        notification.put("message", String.format("收到新订单：%s", order.getOrderNo()));
        notification.put("orderId", order.getId());
        notification.put("orderNo", order.getOrderNo());
        notification.put("fileName", order.getFileName());
        notification.put("amount", order.getAmount());
        notification.put("timestamp", System.currentTimeMillis());

        // 发送到所有订阅了新订单通知的客户端
        messagingTemplate.convertAndSend("/topic/newOrders", notification);
    }

    /**
     * 发送订单状态更新通知
     */
    public void sendOrderStatusUpdateNotification(PrintOrder order) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ORDER_STATUS_UPDATE");
        notification.put("title", "订单状态更新");
        notification.put("message", String.format("订单 %s 状态已更新为：%s", 
            order.getOrderNo(), order.getStatusText()));
        notification.put("orderId", order.getId());
        notification.put("orderNo", order.getOrderNo());
        notification.put("status", order.getStatus());
        notification.put("statusText", order.getStatusText());
        notification.put("timestamp", System.currentTimeMillis());

        // 发送到所有订阅了订单状态更新的客户端
        messagingTemplate.convertAndSend("/topic/orderUpdates", notification);
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(String title, String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/system", notification);
    }
}
