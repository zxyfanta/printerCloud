package com.printercloud.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 健康检查控制器测试
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("printer-cloud-backend"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.message").value("云打印小程序后端服务运行正常"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testHealthCheckResponseStructure() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.status").isString())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.service").isString())
                .andExpect(jsonPath("$.version").isString())
                .andExpect(jsonPath("$.message").isString());
    }
}
