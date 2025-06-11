package com.printercloud.performance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 健康检查接口性能测试
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class HealthPerformanceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    void testHealthEndpointConcurrency() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        int numberOfThreads = 10;
        int requestsPerThread = 10;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        try {
                            mockMvc.perform(get("/health"))
                                    .andExpect(status().isOk());
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            e.printStackTrace();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("=== 健康检查接口性能测试结果 ===");
        System.out.println("并发线程数: " + numberOfThreads);
        System.out.println("每线程请求数: " + requestsPerThread);
        System.out.println("总请求数: " + (numberOfThreads * requestsPerThread));
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("失败请求数: " + errorCount.get());
        System.out.println("总耗时: " + duration + "ms");
        System.out.println("平均响应时间: " + (duration / (double)(numberOfThreads * requestsPerThread)) + "ms");
        System.out.println("QPS: " + ((numberOfThreads * requestsPerThread * 1000.0) / duration));

        // 断言所有请求都成功
        assertEquals(numberOfThreads * requestsPerThread, successCount.get(), "所有请求都应该成功");
        assertEquals(0, errorCount.get(), "不应该有失败的请求");
    }

    @Test
    void testHealthEndpointResponseTime() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        int numberOfRequests = 100;
        long totalTime = 0;

        // 预热
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/health"));
        }

        // 测试响应时间
        for (int i = 0; i < numberOfRequests; i++) {
            long startTime = System.nanoTime();
            mockMvc.perform(get("/health"))
                    .andExpect(status().isOk());
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        double averageTimeMs = (totalTime / numberOfRequests) / 1_000_000.0;

        System.out.println("=== 健康检查接口响应时间测试结果 ===");
        System.out.println("请求次数: " + numberOfRequests);
        System.out.println("平均响应时间: " + String.format("%.2f", averageTimeMs) + "ms");

        // 断言平均响应时间应该小于100ms
        assert averageTimeMs < 100 : "平均响应时间应该小于100ms，实际: " + averageTimeMs + "ms";
    }
}
