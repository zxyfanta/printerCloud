package com.printercloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 云打印小程序后端启动类
 *
 * @author PrinterCloud
 * @since 2024-01-01
 */
@SpringBootApplication
public class PrinterCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrinterCloudApplication.class, args);
        System.out.println("云打印小程序后端服务启动成功！");
    }
}
