package com.printercloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebSocket配置类
 * 使用Spring WebSocket + STOMP协议实现稳定的实时通信
 *
 * 特性：
 * - STOMP协议支持
 * - SockJS降级支持
 * - 心跳检测
 * - 跨域支持
 *
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("websocket-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // 启用简单的消息代理，并设置消息代理的前缀
        config.enableSimpleBroker("/topic", "/queue")
              .setTaskScheduler(taskScheduler())
              .setHeartbeatValue(new long[]{10000, 10000}); // 设置心跳间隔为10秒

        // 设置应用程序的目标前缀
        config.setApplicationDestinationPrefixes("/app");

        // 设置用户目标前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // 注册STOMP端点，支持跨域和SockJS降级
        registry.addEndpoint("/api/ws")
                .setAllowedOriginPatterns("*") // 允许所有来源（生产环境应该限制具体域名）
                .withSockJS()
                .setHeartbeatTime(25000) // SockJS心跳间隔25秒
                .setDisconnectDelay(5000) // 断开延迟5秒
                .setStreamBytesLimit(128 * 1024) // 流字节限制128KB
                .setHttpMessageCacheSize(1000) // HTTP消息缓存大小
                .setSessionCookieNeeded(false); // 不需要session cookie
    }

    @Override
    public void configureWebSocketTransport(@NonNull WebSocketTransportRegistration registry) {
        // 配置WebSocket传输参数
        registry.setMessageSizeLimit(64 * 1024) // 消息大小限制64KB
                .setSendBufferSizeLimit(512 * 1024) // 发送缓冲区大小512KB
                .setSendTimeLimit(20 * 1000) // 发送超时20秒
                .setTimeToFirstMessage(30 * 1000); // 首次消息超时30秒
    }
}
