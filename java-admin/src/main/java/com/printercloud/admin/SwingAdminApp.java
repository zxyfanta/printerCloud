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
        
        // 检查是否为命令行打印模式
        if (args.length > 0 && "print".equals(args[0])) {
            runPrintMode(args);
            return;
        }
        
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
     * 命令行打印模式
     */
    private static void runPrintMode(String[] args) {
        logger.info("启动命令行打印模式");
        
        try {
            // 启动Spring上下文（仅加载必要的服务）
            System.setProperty("java.awt.headless", "true");
            springContext = SpringApplication.run(SwingAdminApp.class, args);
            
            // 获取打印服务
            PrintService printService = springContext.getBean(PrintService.class);
            
            // 解析命令行参数并执行打印
            if (args.length >= 2) {
                String filePath = args[1];
                
                // 解析打印参数
                String printerName = null;
                int copies = 1;
                boolean duplex = false;
                String paperSize = "A4";
                boolean color = false;
                
                for (int i = 2; i < args.length; i++) {
                    String arg = args[i];
                    if (arg.startsWith("--printer=")) {
                        printerName = arg.substring(10);
                    } else if (arg.startsWith("--copies=")) {
                        copies = Integer.parseInt(arg.substring(9));
                    } else if (arg.equals("--duplex")) {
                        duplex = true;
                    } else if (arg.startsWith("--paper=")) {
                        paperSize = arg.substring(8);
                    } else if (arg.equals("--color")) {
                        color = true;
                    }
                }
                
                logger.info("打印文件: {}", filePath);
                logger.info("打印机: {}", printerName != null ? printerName : "默认");
                logger.info("份数: {}", copies);
                logger.info("双面: {}", duplex);
                logger.info("纸张: {}", paperSize);
                logger.info("彩色: {}", color);
                
                // 创建打印设置
                com.printercloud.admin.model.PrintSettings settings = new com.printercloud.admin.model.PrintSettings();
                settings.setPrinterName(printerName);
                settings.setCopies(copies);
                settings.setDuplex(duplex);
                settings.setPaperSize(paperSize);
                settings.setColor(color);

                // 执行打印
                boolean success = printService.printFile(filePath, settings);
                
                if (success) {
                    logger.info("打印任务提交成功");
                    System.out.println("打印任务提交成功");
                } else {
                    logger.error("打印任务提交失败");
                    System.err.println("打印任务提交失败");
                    System.exit(1);
                }
                
            } else {
                logger.error("缺少文件路径参数");
                System.err.println("用法: java -jar printer-admin.jar print <文件路径> [选项]");
                System.err.println("选项:");
                System.err.println("  --printer=<打印机名称>  指定打印机");
                System.err.println("  --copies=<份数>        打印份数 (默认: 1)");
                System.err.println("  --duplex              双面打印");
                System.err.println("  --paper=<纸张大小>     纸张大小 (默认: A4)");
                System.err.println("  --color               彩色打印");
                System.exit(1);
            }
            
        } catch (Exception e) {
            logger.error("命令行打印模式执行失败", e);
            System.err.println("打印失败: " + e.getMessage());
            System.exit(1);
        } finally {
            if (springContext != null) {
                springContext.close();
            }
        }
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
