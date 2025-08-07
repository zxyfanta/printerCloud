package com.printercloud.controller;

import com.printercloud.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 用于验证系统是否正常工作
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@Tag(name = "系统测试", description = "系统测试相关接口")
public class TestController {

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统是否正常运行")
    public R<Map<String, Object>> health() {
        log.info("健康检查请求");
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "PrinterCloud Backend API");
        healthInfo.put("version", "1.0.0");
        
        return R.success("系统运行正常", healthInfo);
    }

    /**
     * 系统信息接口
     */
    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "获取系统基本信息")
    public R<Map<String, Object>> info() {
        log.info("系统信息请求");
        
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("applicationName", "云打印系统后端API");
        systemInfo.put("version", "1.0.0");
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("serverTime", LocalDateTime.now());
        systemInfo.put("timezone", System.getProperty("user.timezone"));
        
        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvmInfo = new HashMap<>();
        jvmInfo.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        jvmInfo.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        jvmInfo.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        jvmInfo.put("availableProcessors", runtime.availableProcessors());
        
        systemInfo.put("jvm", jvmInfo);
        
        return R.success("系统信息获取成功", systemInfo);
    }

    /**
     * 测试统一响应格式
     */
    @GetMapping("/response")
    @Operation(summary = "响应格式测试", description = "测试统一响应格式R类的各种用法")
    public R<Map<String, Object>> testResponse() {
        log.info("响应格式测试请求");
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("message", "这是测试数据");
        testData.put("timestamp", LocalDateTime.now());
        testData.put("number", 12345);
        testData.put("boolean", true);
        
        return R.success("响应格式测试成功", testData);
    }

    /**
     * 测试错误响应
     */
    @GetMapping("/error")
    @Operation(summary = "错误响应测试", description = "测试错误响应格式")
    public R<Void> testError() {
        log.info("错误响应测试请求");
        return R.error("这是一个测试错误响应");
    }

    /**
     * 测试异常处理
     */
    @GetMapping("/exception")
    @Operation(summary = "异常处理测试", description = "测试全局异常处理")
    public R<Void> testException() {
        log.info("异常处理测试请求");
        throw new RuntimeException("这是一个测试异常");
    }
}
