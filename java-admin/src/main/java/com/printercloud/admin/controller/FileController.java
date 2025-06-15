package com.printercloud.admin.controller;

import com.printercloud.admin.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 文件管理控制器
 * 提供文件下载、预览等功能
 */
@RestController
@RequestMapping("/api/files")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private FileService fileService;
    
    /**
     * 获取文件列表
     */
    @GetMapping
    public ResponseEntity<?> getFiles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fileType) {
        
        try {
            logger.info("获取文件列表 - page: {}, pageSize: {}, search: {}, fileType: {}", 
                       page, pageSize, search, fileType);
            
            Map<String, Object> result = fileService.getFiles(page, pageSize, search, fileType);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", result
            ));
            
        } catch (Exception e) {
            logger.error("获取文件列表失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取文件列表失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/download/{orderId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long orderId) {
        try {
            logger.info("下载文件 - orderId: {}", orderId);
            
            Resource resource = fileService.downloadFile(orderId);
            if (resource != null && resource.exists()) {
                String filename = fileService.getFileName(orderId);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + filename + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("下载文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 预览文件
     */
    @GetMapping("/preview/{orderId}")
    public ResponseEntity<Resource> previewFile(@PathVariable Long orderId) {
        try {
            logger.info("预览文件 - orderId: {}", orderId);
            
            Resource resource = fileService.downloadFile(orderId);
            if (resource != null && resource.exists()) {
                String filename = fileService.getFileName(orderId);
                MediaType mediaType = fileService.getMediaType(filename);
                
                return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "inline; filename=\"" + filename + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("预览文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            logger.info("删除文件 - fileId: {}", fileId);
            
            boolean success = fileService.deleteFile(fileId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "文件删除成功"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "文件删除失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("删除文件失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "删除文件失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取文件信息
     */
    @GetMapping("/{fileId}/info")
    public ResponseEntity<?> getFileInfo(@PathVariable Long fileId) {
        try {
            logger.info("获取文件信息 - fileId: {}", fileId);
            
            Map<String, Object> fileInfo = fileService.getFileInfo(fileId);
            if (fileInfo != null) {
                return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "success",
                    "data", fileInfo
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "code", 404,
                    "message", "文件不存在"
                ));
            }
            
        } catch (Exception e) {
            logger.error("获取文件信息失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取文件信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getFileStatistics() {
        try {
            logger.info("获取文件统计信息");
            
            Map<String, Object> statistics = fileService.getFileStatistics();
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", statistics
            ));
            
        } catch (Exception e) {
            logger.error("获取文件统计信息失败", e);
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }
}
