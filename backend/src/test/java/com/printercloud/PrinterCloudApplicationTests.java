package com.printercloud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用程序启动测试
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
class PrinterCloudApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否能正常加载
    }

    @Test
    void applicationStarts() {
        // 测试应用程序是否能正常启动
        // 如果能到达这里，说明应用程序启动成功
        assert true;
    }
}
