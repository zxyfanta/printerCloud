package com.printercloud.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Swing版本的通知服务
 * 处理系统通知、新订单提醒等功能
 */
@Service
public class SwingNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SwingNotificationService.class);
    
    // 通知监听器列表
    private final List<Consumer<Notification>> notificationListeners = new CopyOnWriteArrayList<>();
    
    // 通知历史
    private final List<Notification> notificationHistory = new ArrayList<>();
    
    /**
     * 添加通知监听器
     */
    public void addNotificationListener(Consumer<Notification> listener) {
        notificationListeners.add(listener);
    }
    
    /**
     * 移除通知监听器
     */
    public void removeNotificationListener(Consumer<Notification> listener) {
        notificationListeners.remove(listener);
    }
    
    /**
     * 发送新订单通知
     */
    public void sendNewOrderNotification(String orderNo, String fileName, Double amount) {
        Notification notification = new Notification(
            NotificationType.NEW_ORDER,
            "新订单提醒",
            String.format("收到新订单：%s\n文件：%s\n金额：%.2f元", orderNo, fileName, amount),
            System.currentTimeMillis()
        );
        
        sendNotification(notification);
    }
    
    /**
     * 发送订单状态更新通知
     */
    public void sendOrderStatusUpdateNotification(String orderNo, String status) {
        Notification notification = new Notification(
            NotificationType.ORDER_UPDATE,
            "订单状态更新",
            String.format("订单 %s 状态已更新为：%s", orderNo, status),
            System.currentTimeMillis()
        );
        
        sendNotification(notification);
    }
    
    /**
     * 发送系统通知
     */
    public void sendSystemNotification(String title, String message, NotificationType type) {
        Notification notification = new Notification(type, title, message, System.currentTimeMillis());
        sendNotification(notification);
    }
    
    /**
     * 发送错误通知
     */
    public void sendErrorNotification(String title, String message) {
        sendSystemNotification(title, message, NotificationType.ERROR);
    }
    
    /**
     * 发送成功通知
     */
    public void sendSuccessNotification(String title, String message) {
        sendSystemNotification(title, message, NotificationType.SUCCESS);
    }
    
    /**
     * 发送警告通知
     */
    public void sendWarningNotification(String title, String message) {
        sendSystemNotification(title, message, NotificationType.WARNING);
    }
    
    /**
     * 发送信息通知
     */
    public void sendInfoNotification(String title, String message) {
        sendSystemNotification(title, message, NotificationType.INFO);
    }
    
    /**
     * 显示对话框通知
     */
    public void showDialogNotification(String title, String message, int messageType) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                message,
                title,
                messageType
            );
        });
    }
    
    /**
     * 显示确认对话框
     */
    public void showConfirmDialog(String title, String message, Runnable onConfirm) {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(
                null,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION && onConfirm != null) {
                onConfirm.run();
            }
        });
    }
    
    /**
     * 显示系统托盘通知（如果支持）
     */
    public void showTrayNotification(String title, String message, NotificationType type) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                
                // 创建托盘图标（这里使用默认图标）
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                TrayIcon trayIcon = new TrayIcon(image, "云打印管理系统");
                trayIcon.setImageAutoSize(true);
                
                // 显示通知
                TrayIcon.MessageType messageType = getTrayMessageType(type);
                trayIcon.displayMessage(title, message, messageType);
                
                // 添加到系统托盘
                if (tray.getTrayIcons().length == 0) {
                    tray.add(trayIcon);
                }
                
            } catch (Exception e) {
                logger.warn("显示系统托盘通知失败", e);
                // 降级到普通对话框
                showDialogNotification(title, message, getSwingMessageType(type));
            }
        } else {
            // 系统不支持托盘，使用对话框
            showDialogNotification(title, message, getSwingMessageType(type));
        }
    }
    
    /**
     * 获取托盘消息类型
     */
    private TrayIcon.MessageType getTrayMessageType(NotificationType type) {
        switch (type) {
            case ERROR:
                return TrayIcon.MessageType.ERROR;
            case WARNING:
                return TrayIcon.MessageType.WARNING;
            case SUCCESS:
            case INFO:
            default:
                return TrayIcon.MessageType.INFO;
        }
    }
    
    /**
     * 获取Swing消息类型
     */
    private int getSwingMessageType(NotificationType type) {
        switch (type) {
            case ERROR:
                return JOptionPane.ERROR_MESSAGE;
            case WARNING:
                return JOptionPane.WARNING_MESSAGE;
            case SUCCESS:
            case INFO:
            default:
                return JOptionPane.INFORMATION_MESSAGE;
        }
    }
    
    /**
     * 获取通知历史
     */
    public List<Notification> getNotificationHistory() {
        return new ArrayList<>(notificationHistory);
    }
    
    /**
     * 清空通知历史
     */
    public void clearNotificationHistory() {
        notificationHistory.clear();
    }
    
    /**
     * 发送通知到所有监听器
     */
    private void sendNotification(Notification notification) {
        logger.info("发送通知: {} - {}", notification.getTitle(), notification.getMessage());
        
        // 添加到历史记录
        notificationHistory.add(notification);
        
        // 限制历史记录数量
        if (notificationHistory.size() > 100) {
            notificationHistory.remove(0);
        }
        
        // 通知所有监听器
        for (Consumer<Notification> listener : notificationListeners) {
            try {
                listener.accept(notification);
            } catch (Exception e) {
                logger.error("通知监听器处理失败", e);
            }
        }
        
        // 显示系统托盘通知
        showTrayNotification(notification.getTitle(), notification.getMessage(), notification.getType());
    }
    
    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        NEW_ORDER("新订单"),
        ORDER_UPDATE("订单更新"),
        SYSTEM("系统通知"),
        ERROR("错误"),
        WARNING("警告"),
        SUCCESS("成功"),
        INFO("信息");
        
        private final String displayName;
        
        NotificationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 通知数据类
     */
    public static class Notification {
        private final NotificationType type;
        private final String title;
        private final String message;
        private final long timestamp;
        
        public Notification(NotificationType type, String title, String message, long timestamp) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", type.getDisplayName(), title, message);
        }
    }
}
