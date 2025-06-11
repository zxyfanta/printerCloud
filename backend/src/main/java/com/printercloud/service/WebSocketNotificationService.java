package com.printercloud.service;

import com.printercloud.entity.PrintOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket通知服务
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 发送新订单通知
     */
    public void sendNewOrderNotification(PrintOrder order) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_ORDER");
        notification.put("title", "新订单通知");
        notification.put("message", "收到新订单：" + order.getOrderNo());
        notification.put("orderId", order.getId());
        notification.put("orderNo", order.getOrderNo());
        notification.put("amount", order.getAmount());
        notification.put("fileName", order.getFileName());
        notification.put("timestamp", System.currentTimeMillis());

        // 发送到所有订阅了新订单主题的客户端
        messagingTemplate.convertAndSend("/topic/newOrders", notification);
        
        // 也可以发送到特定用户（如果需要）
        // messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * 发送订单状态更新通知
     */
    public void sendOrderUpdateNotification(PrintOrder order, String action) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ORDER_UPDATE");
        notification.put("title", "订单状态更新");
        notification.put("message", getUpdateMessage(order, action));
        notification.put("orderId", order.getId());
        notification.put("orderNo", order.getOrderNo());
        notification.put("status", order.getStatus());
        notification.put("action", action);
        notification.put("timestamp", System.currentTimeMillis());

        // 发送到所有订阅了订单更新主题的客户端
        messagingTemplate.convertAndSend("/topic/orderUpdates", notification);
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(String title, String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "SYSTEM");
        notification.put("title", title);
        notification.put("message", message);
        notification.put("notificationType", type); // INFO, WARNING, ERROR
        notification.put("timestamp", System.currentTimeMillis());

        // 发送到所有订阅了系统通知主题的客户端
        messagingTemplate.convertAndSend("/topic/system", notification);
    }

    /**
     * 发送给特定用户的通知
     */
    public void sendUserNotification(String userId, String title, String message, Map<String, Object> data) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "USER_NOTIFICATION");
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());
        
        if (data != null) {
            notification.putAll(data);
        }

        // 发送到特定用户
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * 广播消息给所有连接的客户端
     */
    public void broadcastMessage(String topic, Object message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
    }

    /**
     * 获取订单更新消息
     */
    private String getUpdateMessage(PrintOrder order, String action) {
        String orderNo = order.getOrderNo();
        switch (action) {
            case "PAID":
                return "订单 " + orderNo + " 已支付";
            case "PRINTING":
                return "订单 " + orderNo + " 开始打印";
            case "COMPLETED":
                return "订单 " + orderNo + " 已完成";
            case "CANCELLED":
                return "订单 " + orderNo + " 已取消";
            case "REFUNDED":
                return "订单 " + orderNo + " 已退款";
            default:
                return "订单 " + orderNo + " 状态已更新";
        }
    }

    /**
     * 发送订单统计更新通知
     */
    public void sendStatisticsUpdateNotification() {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "STATISTICS_UPDATE");
        notification.put("message", "统计数据已更新");
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/statistics", notification);
    }

    /**
     * 发送文件上传通知
     */
    public void sendFileUploadNotification(String fileName, Long fileSize) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FILE_UPLOAD");
        notification.put("title", "文件上传通知");
        notification.put("message", "新文件已上传：" + fileName);
        notification.put("fileName", fileName);
        notification.put("fileSize", fileSize);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/fileUploads", notification);
    }
}
