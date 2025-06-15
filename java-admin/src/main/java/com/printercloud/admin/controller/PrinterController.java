package com.printercloud.admin.controller;

import com.printercloud.admin.model.PrintJob;
import com.printercloud.admin.model.PrinterInfo;
import com.printercloud.admin.model.PrintSettings;
import com.printercloud.admin.service.PrintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 打印机管理控制器
 * 提供打印机发现、管理、打印任务等功能
 */
@RestController
@RequestMapping("/api/printers")
public class PrinterController {
    
    private static final Logger logger = LoggerFactory.getLogger(PrinterController.class);
    
    @Autowired
    private PrintService printService;
    
    /**
     * 获取可用打印机列表
     */
    @GetMapping
    public ResponseEntity<?> getAvailablePrinters() {
        try {
            logger.info("获取可用打印机列表");
            
            List<PrinterInfo> printers = printService.getAvailablePrinters();
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", printers
            ));
            
        } catch (Exception e) {
            logger.error("获取打印机列表失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取打印机列表失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取打印机详细信息
     */
    @GetMapping("/{printerName}")
    public ResponseEntity<?> getPrinterInfo(@PathVariable String printerName) {
        try {
            logger.info("获取打印机信息 - printerName: {}", printerName);
            
            PrinterInfo printerInfo = printService.getPrinterInfo(printerName);
            if (printerInfo != null) {
                return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "success",
                    "data", printerInfo
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "code", 404,
                    "message", "打印机不存在"
                ));
            }
            
        } catch (Exception e) {
            logger.error("获取打印机信息失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取打印机信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 打印文件
     */
    @PostMapping("/print")
    public ResponseEntity<?> printFile(@RequestBody Map<String, Object> request) {
        try {
            String filePath = (String) request.get("filePath");
            String printerName = (String) request.get("printerName");
            Integer copies = (Integer) request.get("copies");
            Boolean doubleSided = (Boolean) request.get("doubleSided");
            String paperSize = (String) request.get("paperSize");
            String colorMode = (String) request.get("colorMode");
            
            logger.info("打印文件 - filePath: {}, printerName: {}, copies: {}", 
                       filePath, printerName, copies);
            
            // 创建打印设置
            PrintSettings settings = new PrintSettings();
            settings.setPrinterName(printerName);
            settings.setCopies(copies != null ? copies : 1);
            settings.setDoubleSided(doubleSided != null ? doubleSided : false);
            settings.setPaperSize(paperSize != null ? paperSize : "A4");
            settings.setColorMode(colorMode != null ? colorMode : "BLACK_WHITE");
            
            // 异步打印
            printService.printFileAsync(filePath, settings)
                .thenAccept(success -> {
                    if (success) {
                        logger.info("文件打印成功: {}", filePath);
                    } else {
                        logger.error("文件打印失败: {}", filePath);
                    }
                });
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "打印任务已提交"
            ));
            
        } catch (Exception e) {
            logger.error("提交打印任务失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "提交打印任务失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 根据订单打印文件
     */
    @PostMapping("/print-order")
    public ResponseEntity<?> printOrder(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.valueOf(request.get("orderId").toString());
            String printerName = (String) request.get("printerName");
            
            logger.info("打印订单文件 - orderId: {}, printerName: {}", orderId, printerName);
            
            boolean success = printService.printOrderFile(orderId, printerName);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "订单打印成功"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "订单打印失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("打印订单文件失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "打印订单文件失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取打印任务列表
     */
    @GetMapping("/jobs")
    public ResponseEntity<?> getPrintJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        
        try {
            logger.info("获取打印任务列表 - page: {}, pageSize: {}, status: {}", 
                       page, pageSize, status);
            
            Map<String, Object> result = printService.getPrintJobs(page, pageSize, status);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", result
            ));
            
        } catch (Exception e) {
            logger.error("获取打印任务列表失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取打印任务列表失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 取消打印任务
     */
    @PostMapping("/jobs/{jobId}/cancel")
    public ResponseEntity<?> cancelPrintJob(@PathVariable String jobId) {
        try {
            logger.info("取消打印任务 - jobId: {}", jobId);
            
            boolean success = printService.cancelPrintJob(jobId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "打印任务取消成功"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "打印任务取消失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("取消打印任务失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "取消打印任务失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 测试打印机连接
     */
    @PostMapping("/{printerName}/test")
    public ResponseEntity<?> testPrinter(@PathVariable String printerName) {
        try {
            logger.info("测试打印机连接 - printerName: {}", printerName);
            
            boolean success = printService.testPrinter(printerName);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "打印机连接正常"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "打印机连接失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("测试打印机连接失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "测试打印机连接失败: " + e.getMessage()
            ));
        }
    }
}
