package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.service.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket测试控制器
 * 用于测试WebSocket连接和消息传递
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Controller
public class WebSocketTestController {

    /**
     * 处理客户端发送的测试消息
     */
    @MessageMapping("/test")
    @SendTo("/topic/test")
    public Map<String, Object> handleTestMessage(Map<String, Object> message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "TEST_RESPONSE");
        response.put("message", "WebSocket连接正常");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("receivedMessage", message);
        return response;
    }

    /**
     * 发送测试通知的REST端点
     */
    @RestController
    @RequestMapping("/api/websocket")
    @CrossOrigin(origins = "*")
    public static class WebSocketTestRestController {

        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @Autowired
        private WebSocketNotificationService notificationService;

        /**
         * 发送测试通知
         */
        @PostMapping("/test-notification")
        public R<String> sendTestNotification() {
            try {
                // 发送测试系统通知
                notificationService.sendSystemNotification(
                    "WebSocket测试",
                    "这是一条测试通知，用于验证WebSocket连接是否正常工作",
                    "INFO"
                );

                return R.ok("测试通知已发送");
            } catch (Exception e) {
                return R.fail("发送测试通知失败: " + e.getMessage());
            }
        }

        /**
         * 获取WebSocket连接状态
         */
        @GetMapping("/status")
        public R<Map<String, Object>> getWebSocketStatus() {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", LocalDateTime.now().toString());
                data.put("endpoints", new String[]{
                    "/api/ws - WebSocket连接端点",
                    "/topic/newOrders - 新订单通知",
                    "/topic/orderUpdates - 订单状态更新",
                    "/topic/system - 系统通知",
                    "/topic/test - 测试消息"
                });

                return R.ok(data, "WebSocket服务正常运行");
            } catch (Exception e) {
                return R.fail("获取WebSocket状态失败: " + e.getMessage());
            }
        }

        /**
         * 发送自定义消息到指定主题
         */
        @PostMapping("/send-message")
        public R<String> sendCustomMessage(
                @RequestParam String topic,
                @RequestParam String title,
                @RequestParam String message) {

            try {
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "CUSTOM");
                notification.put("title", title);
                notification.put("message", message);
                notification.put("timestamp", LocalDateTime.now().toString());

                messagingTemplate.convertAndSend("/topic/" + topic, notification);

                return R.ok("自定义消息已发送到主题: " + topic);
            } catch (Exception e) {
                return R.fail("发送自定义消息失败: " + e.getMessage());
            }
        }
    }
}
