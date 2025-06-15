package com.printercloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Tag(name = "系统监控", description = "系统健康检查接口")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查", description = "检查系统运行状态")
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "printer-cloud-backend");
        data.put("version", "1.0.0");
        data.put("message", "云打印小程序后端服务运行正常");
        
        return data;
    }
}
