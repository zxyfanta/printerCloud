package com.printercloud.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 应用程序配置类
 */
@Configuration
public class AppConfig {

    /**
     * 配置WebClient用于HTTP请求
     */
    @Bean
    public WebClient webClient(ApiProperties apiProperties) {
        return WebClient.builder()
                .baseUrl(apiProperties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    /**
     * API配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "app.api")
    public ApiProperties apiProperties() {
        return new ApiProperties();
    }

    /**
     * 应用程序配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "app.settings")
    public AppProperties appProperties() {
        return new AppProperties();
    }

    /**
     * 打印机配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "app.printer")
    public PrinterProperties printerProperties() {
        return new PrinterProperties();
    }

    /**
     * 配置ObjectMapper用于JSON序列化/反序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 配置忽略未知属性，提高兼容性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 注册JavaTimeModule处理Java 8时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置LocalDateTime的序列化和反序列化格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));

        mapper.registerModule(javaTimeModule);

        return mapper;
    }

    /**
     * API配置属性类
     */
    public static class ApiProperties {
        private String baseUrl = "http://localhost:8082/api";
        private Duration timeout = Duration.ofSeconds(30);
        private String authToken;

        // Getters and Setters
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }

        public String getAuthToken() { return authToken; }
        public void setAuthToken(String authToken) { this.authToken = authToken; }
    }

    /**
     * 应用程序配置属性类
     */
    public static class AppProperties {
        private String theme = "light";
        private String language = "zh-CN";
        private boolean autoLogin = false;
        private boolean minimizeToTray = true;
        private int refreshInterval = 30;
        private boolean showNotifications = true;

        // Getters and Setters
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public boolean isAutoLogin() { return autoLogin; }
        public void setAutoLogin(boolean autoLogin) { this.autoLogin = autoLogin; }

        public boolean isMinimizeToTray() { return minimizeToTray; }
        public void setMinimizeToTray(boolean minimizeToTray) { this.minimizeToTray = minimizeToTray; }

        public int getRefreshInterval() { return refreshInterval; }
        public void setRefreshInterval(int refreshInterval) { this.refreshInterval = refreshInterval; }

        public boolean isShowNotifications() { return showNotifications; }
        public void setShowNotifications(boolean showNotifications) { this.showNotifications = showNotifications; }
    }

    /**
     * 打印机配置属性类
     */
    public static class PrinterProperties {
        private String defaultPrinter;
        private int defaultCopies = 1;
        private boolean defaultDuplex = false;
        private String defaultPaperSize = "A4";
        private String defaultQuality = "normal";
        private boolean defaultColor = false;

        // Getters and Setters
        public String getDefaultPrinter() { return defaultPrinter; }
        public void setDefaultPrinter(String defaultPrinter) { this.defaultPrinter = defaultPrinter; }

        public int getDefaultCopies() { return defaultCopies; }
        public void setDefaultCopies(int defaultCopies) { this.defaultCopies = defaultCopies; }

        public boolean isDefaultDuplex() { return defaultDuplex; }
        public void setDefaultDuplex(boolean defaultDuplex) { this.defaultDuplex = defaultDuplex; }

        public String getDefaultPaperSize() { return defaultPaperSize; }
        public void setDefaultPaperSize(String defaultPaperSize) { this.defaultPaperSize = defaultPaperSize; }

        public String getDefaultQuality() { return defaultQuality; }
        public void setDefaultQuality(String defaultQuality) { this.defaultQuality = defaultQuality; }

        public boolean isDefaultColor() { return defaultColor; }
        public void setDefaultColor(boolean defaultColor) { this.defaultColor = defaultColor; }
    }
}
