package com.printercloud.admin;

import com.printercloud.admin.service.PrintService;
import com.printercloud.admin.view.swing.SwingApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Swing版本的云打印管理系统主应用程序
 */
@SpringBootApplication
public class SwingAdminApp implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(SwingAdminApp.class);
    
    @Autowired
    private SwingApplication swingApplication;
    
    @Autowired
    private PrintService printService;
    
    private static ConfigurableApplicationContext springContext;
    
    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("java.awt.headless", "false");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        logger.info("启动云打印管理系统 (Swing版本)...");
        
        // 启动Spring Boot应用程序
        springContext = SpringApplication.run(SwingAdminApp.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Spring Boot 应用程序启动完成");
        
        // 启动Swing GUI
        swingApplication.start();
        
        logger.info("Swing GUI 启动完成");
    }
    
    /**
     * 优雅关闭应用程序
     */
    public static void shutdown() {
        logger.info("关闭应用程序...");
        
        if (springContext != null) {
            SpringApplication.exit(springContext, () -> 0);
        }
        
        System.exit(0);
    }
}
