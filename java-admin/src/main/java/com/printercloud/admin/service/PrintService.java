package com.printercloud.admin.service;

import com.printercloud.admin.config.AppConfig;
import com.printercloud.admin.model.PrintJob;
import com.printercloud.admin.model.PrinterInfo;
import com.printercloud.admin.model.PrintSettings;
import com.printercloud.admin.model.Order;
import com.printercloud.admin.util.FileUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 打印服务
 * 支持多种文件格式的打印和命令行打印
 */
@Service
public class PrintService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrintService.class);

    @Autowired
    private AppConfig.PrinterProperties printerProperties;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FileService fileService;

    @Autowired
    private WebClient webClient;

    @Value("${app.api.base-url}")
    private String apiBaseUrl;

    // 打印任务队列
    private final Map<String, PrintJob> printJobs = new ConcurrentHashMap<>();

    /**
     * 获取系统可用打印机列表
     */
    public java.util.List<PrinterInfo> getAvailablePrinters() {
        java.util.List<PrinterInfo> printers = new ArrayList<>();
        
        try {
            // 获取所有打印服务
            javax.print.PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            javax.print.PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            
            for (javax.print.PrintService service : printServices) {
                PrinterInfo printer = new PrinterInfo();
                printer.setName(service.getName());
                printer.setDisplayName(service.getName());
                printer.setDefault(service.equals(defaultService));
                printer.setOnline(isServiceAvailable(service));

                // 获取打印机属性
                setPrinterCapabilities(printer, service);

                printers.add(printer);
                logger.debug("发现打印机: {}", printer.getName());
            }
            
        } catch (Exception e) {
            logger.error("获取打印机列表失败", e);
        }
        
        return printers;
    }

    /**
     * 打印文件
     */
    public CompletableFuture<Boolean> printFileAsync(String filePath, PrintSettings settings) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return printFile(filePath, settings);
            } catch (Exception e) {
                logger.error("异步打印文件失败: {}", filePath, e);
                return false;
            }
        });
    }

    /**
     * 同步打印文件
     */
    public boolean printFile(String filePath, PrintSettings settings) {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("文件不存在: {}", filePath);
            return false;
        }

        String extension = fileUtil.getFileExtension(file);
        logger.info("开始打印文件: {}, 格式: {}", filePath, extension);

        try {
            return switch (extension.toLowerCase()) {
                case "pdf" -> printPdf(file, settings);
                case "jpg", "jpeg", "png", "gif", "bmp" -> printImage(file, settings);
                case "txt" -> printText(file, settings);
                case "doc", "docx" -> printWordDocument(file, settings);
                default -> {
                    logger.warn("不支持的文件格式: {}", extension);
                    yield false;
                }
            };
        } catch (Exception e) {
            logger.error("打印文件失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 命令行打印接口
     */
    public boolean printFromCommand(String[] args) {
        try {
            PrintCommandArgs commandArgs = parseCommandArgs(args);
            if (commandArgs == null) {
                printUsage();
                return false;
            }

            if (commandArgs.isBatchMode()) {
                return processBatchPrint(commandArgs.getBatchFile());
            } else {
                PrintSettings settings = createPrintSettings(commandArgs);
                return printFile(commandArgs.getFilePath(), settings);
            }
            
        } catch (Exception e) {
            logger.error("命令行打印失败", e);
            System.err.println("打印失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 打印PDF文件
     */
    private boolean printPdf(File file, PrintSettings settings) throws Exception {
        try (PDDocument document = Loader.loadPDF(file)) {
            PrinterJob job = PrinterJob.getPrinterJob();
            
            // 设置打印机
            if (settings.getPrinterName() != null) {
                javax.print.PrintService printService = findPrintService(settings.getPrinterName());
                if (printService != null) {
                    job.setPrintService(printService);
                }
            }
            
            // 设置打印属性
            PrintRequestAttributeSet attributes = createPrintAttributes(settings);
            
            // 设置页面
            job.setPageable(new PDFPageable(document));
            
            // 执行打印
            job.print(attributes);
            logger.info("PDF文件打印成功: {}", file.getName());
            return true;
        }
    }

    /**
     * 打印图片文件
     */
    private boolean printImage(File file, PrintSettings settings) throws Exception {
        BufferedImage image = javax.imageio.ImageIO.read(file);
        if (image == null) {
            logger.error("无法读取图片文件: {}", file.getName());
            return false;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        
        // 设置打印机
        if (settings.getPrinterName() != null) {
            javax.print.PrintService printService = findPrintService(settings.getPrinterName());
            if (printService != null) {
                job.setPrintService(printService);
            }
        }

        // 设置打印内容
        job.setPrintable(new ImagePrintable(image));
        
        // 设置打印属性
        PrintRequestAttributeSet attributes = createPrintAttributes(settings);
        
        // 执行打印
        job.print(attributes);
        logger.info("图片文件打印成功: {}", file.getName());
        return true;
    }

    /**
     * 打印文本文件
     */
    private boolean printText(File file, PrintSettings settings) throws Exception {
        // 简单的文本打印实现
        String content = fileUtil.readFileAsString(file);
        
        PrinterJob job = PrinterJob.getPrinterJob();
        
        // 设置打印机
        if (settings.getPrinterName() != null) {
            javax.print.PrintService printService = findPrintService(settings.getPrinterName());
            if (printService != null) {
                job.setPrintService(printService);
            }
        }

        // 设置打印内容
        job.setPrintable(new TextPrintable(content));
        
        // 设置打印属性
        PrintRequestAttributeSet attributes = createPrintAttributes(settings);
        
        // 执行打印
        job.print(attributes);
        logger.info("文本文件打印成功: {}", file.getName());
        return true;
    }

    /**
     * 打印Word文档（需要转换为PDF）
     */
    private boolean printWordDocument(File file, PrintSettings settings) throws Exception {
        // 这里可以集成Apache POI将Word转换为PDF后打印
        // 或者使用系统默认程序打印
        logger.warn("Word文档打印功能待实现: {}", file.getName());
        return false;
    }

    /**
     * 图片打印器
     */
    private static class ImagePrintable implements Printable {
        private final BufferedImage image;

        public ImagePrintable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // 计算缩放比例
            double scaleX = pageFormat.getImageableWidth() / image.getWidth();
            double scaleY = pageFormat.getImageableHeight() / image.getHeight();
            double scale = Math.min(scaleX, scaleY);

            // 绘制图片
            int width = (int) (image.getWidth() * scale);
            int height = (int) (image.getHeight() * scale);
            g2d.drawImage(image, 0, 0, width, height, null);

            return PAGE_EXISTS;
        }
    }

    /**
     * 文本打印器
     */
    private static class TextPrintable implements Printable {
        private final String content;

        public TextPrintable(String content) {
            this.content = content;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // 设置字体
            Font font = new Font("SansSerif", Font.PLAIN, 12);
            g2d.setFont(font);
            g2d.setColor(Color.BLACK);

            // 分行打印文本
            String[] lines = content.split("\n");
            int y = 20;
            for (String line : lines) {
                g2d.drawString(line, 10, y);
                y += 15;
                if (y > pageFormat.getImageableHeight() - 20) {
                    break; // 超出页面范围
                }
            }

            return PAGE_EXISTS;
        }
    }

    // 其他辅助方法...
    private void setPrinterCapabilities(PrinterInfo printer, javax.print.PrintService service) {
        // 设置打印机能力信息
    }

    private boolean isServiceAvailable(javax.print.PrintService service) {
        // 检查打印服务是否可用
        return true;
    }

    private javax.print.PrintService findPrintService(String printerName) {
        // 查找指定名称的打印服务
        return null;
    }

    private PrintRequestAttributeSet createPrintAttributes(PrintSettings settings) {
        // 创建打印属性集
        return new HashPrintRequestAttributeSet();
    }

    private PrintCommandArgs parseCommandArgs(String[] args) {
        // 解析命令行参数
        return null;
    }

    private PrintSettings createPrintSettings(PrintCommandArgs commandArgs) {
        // 从命令行参数创建打印设置
        return null;
    }

    private boolean processBatchPrint(String batchFile) {
        // 处理批量打印
        return false;
    }

    private void printUsage() {
        System.out.println("用法: java -jar printer-admin.jar --print --file=<文件路径> [选项]");
        System.out.println("选项:");
        System.out.println("  --printer=<打印机名称>");
        System.out.println("  --copies=<份数>");
        System.out.println("  --duplex=<true|false>");
        System.out.println("  --paper=<纸张大小>");
        System.out.println("  --color=<true|false>");
    }

    // 内部类定义...
    private static class PrintCommandArgs {
        private String filePath;
        private String batchFile;
        private boolean batchMode;
        
        // getters and setters...
        public String getFilePath() { return filePath; }
        public String getBatchFile() { return batchFile; }
        public boolean isBatchMode() { return batchMode; }
    }



    /**
     * 获取打印机详细信息
     */
    public PrinterInfo getPrinterInfo(String printerName) {
        try {
            javax.print.PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

            for (javax.print.PrintService service : printServices) {
                if (service.getName().equals(printerName)) {
                    PrinterInfo printer = new PrinterInfo();
                    printer.setName(service.getName());
                    printer.setDisplayName(service.getName());
                    printer.setOnline(isServiceAvailable(service));
                    setPrinterCapabilities(printer, service);
                    return printer;
                }
            }

            return null;

        } catch (Exception e) {
            logger.error("获取打印机信息失败", e);
            return null;
        }
    }

    /**
     * 根据订单打印文件
     */
    public boolean printOrderFile(Long orderId, String printerName) {
        try {
            logger.info("打印订单文件 - orderId: {}, printerName: {}", orderId, printerName);

            // 获取订单信息
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                logger.error("订单不存在 - orderId: {}", orderId);
                return false;
            }

            // 下载文件
            org.springframework.core.io.Resource resource = fileService.downloadFile(orderId);
            if (resource == null || !resource.exists()) {
                logger.error("文件不存在 - orderId: {}", orderId);
                return false;
            }

            // 创建打印设置
            PrintSettings settings = new PrintSettings();
            settings.setPrinterName(printerName);
            settings.setCopies(order.getCopies() != null ? order.getCopies() : 1);
            settings.setDoubleSided(order.getIsDoubleSide() != null ? order.getIsDoubleSide() : false);
            settings.setPaperSize(order.getPaperSize() != null ? order.getPaperSize() : "A4");
            settings.setColor(order.getIsColor() != null ? order.getIsColor() : false);

            // 执行打印
            String filePath = resource.getFile().getAbsolutePath();
            return printFile(filePath, settings);

        } catch (Exception e) {
            logger.error("打印订单文件失败", e);
            return false;
        }
    }

    /**
     * 获取打印任务列表
     */
    public Map<String, Object> getPrintJobs(int page, int pageSize, String status) {
        try {
            java.util.List<PrintJob> jobs = new ArrayList<>(printJobs.values());

            // 过滤状态
            if (status != null && !status.trim().isEmpty()) {
                jobs = jobs.stream()
                    .filter(job -> status.equals(job.getStatus()))
                    .toList();
            }

            // 分页
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, jobs.size());
            java.util.List<PrintJob> pageJobs = jobs.subList(start, end);

            Map<String, Object> result = new HashMap<>();
            result.put("content", pageJobs);
            result.put("totalElements", (long) jobs.size());
            result.put("totalPages", (jobs.size() + pageSize - 1) / pageSize);
            result.put("number", page - 1);
            result.put("size", pageSize);

            return result;

        } catch (Exception e) {
            logger.error("获取打印任务列表失败", e);
            throw new RuntimeException("获取打印任务列表失败", e);
        }
    }

    /**
     * 取消打印任务
     */
    public boolean cancelPrintJob(String jobId) {
        try {
            logger.info("取消打印任务 - jobId: {}", jobId);

            PrintJob job = printJobs.get(jobId);
            if (job != null) {
                job.setStatus("CANCELLED");
                job.setErrorMessage("用户取消");
                return true;
            }

            return false;

        } catch (Exception e) {
            logger.error("取消打印任务失败", e);
            return false;
        }
    }

    /**
     * 测试打印机连接
     */
    public boolean testPrinter(String printerName) {
        try {
            logger.info("测试打印机连接 - printerName: {}", printerName);

            javax.print.PrintService printService = findPrintService(printerName);
            if (printService == null) {
                logger.error("打印机不存在: {}", printerName);
                return false;
            }

            // 检查打印机状态
            return isServiceAvailable(printService);

        } catch (Exception e) {
            logger.error("测试打印机连接失败", e);
            return false;
        }
    }
}
