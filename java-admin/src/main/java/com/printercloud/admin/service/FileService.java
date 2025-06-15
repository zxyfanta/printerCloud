package com.printercloud.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/**
 * 文件服务
 * 负责文件下载、预览、管理等功能
 */
@Service
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    @Value("${app.api.base-url}")
    private String apiBaseUrl;
    
    @Value("${app.file.storage-path:./files}")
    private String storagePath;
    
    @Autowired
    private WebClient webClient;
    
    /**
     * 获取文件列表
     */
    public Map<String, Object> getFiles(int page, int pageSize, String search, String fileType) {
        try {
            logger.info("调用后端API获取文件列表");
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("pageSize", pageSize);
            if (search != null && !search.trim().isEmpty()) {
                params.put("search", search);
            }
            if (fileType != null && !fileType.trim().isEmpty()) {
                params.put("fileType", fileType);
            }
            
            // 调用后端API
            String response = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/files");
                    params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应，为了简化，返回模拟数据
            return createMockFileList(page, pageSize, search, fileType);
            
        } catch (Exception e) {
            logger.error("获取文件列表失败", e);
            throw new RuntimeException("获取文件列表失败", e);
        }
    }
    
    /**
     * 下载文件
     */
    public Resource downloadFile(Long orderId) {
        try {
            logger.info("下载文件 - orderId: {}", orderId);
            
            // 首先获取文件信息
            String fileName = getFileName(orderId);
            if (fileName == null) {
                logger.error("文件不存在 - orderId: {}", orderId);
                return null;
            }
            
            // 构建文件路径
            Path filePath = Paths.get(storagePath).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                // 如果本地文件不存在，尝试从后端API下载
                return downloadFileFromApi(orderId);
            }
            
        } catch (MalformedURLException e) {
            logger.error("文件路径错误", e);
            return null;
        } catch (Exception e) {
            logger.error("下载文件失败", e);
            return null;
        }
    }
    
    /**
     * 从后端API下载文件
     */
    private Resource downloadFileFromApi(Long orderId) {
        try {
            logger.info("从后端API下载文件 - orderId: {}", orderId);
            
            // 调用后端API下载文件
            byte[] fileData = webClient.get()
                .uri("/files/download/{orderId}", orderId)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(60))
                .block();
            
            if (fileData != null && fileData.length > 0) {
                // 保存文件到本地
                String fileName = getFileName(orderId);
                Path filePath = Paths.get(storagePath).resolve(fileName);
                
                // 确保目录存在
                filePath.getParent().toFile().mkdirs();
                
                // 写入文件
                java.nio.file.Files.write(filePath, fileData);
                
                return new UrlResource(filePath.toUri());
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("从后端API下载文件失败", e);
            return null;
        }
    }
    
    /**
     * 获取文件名
     */
    public String getFileName(Long orderId) {
        try {
            logger.info("获取文件名 - orderId: {}", orderId);
            
            // 调用后端API获取文件信息
            String response = webClient.get()
                .uri("/files/{orderId}/info", orderId)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应获取文件名，为了简化，返回模拟文件名
            return "document_" + orderId + ".pdf";
            
        } catch (Exception e) {
            logger.error("获取文件名失败", e);
            return "unknown_" + orderId + ".pdf";
        }
    }
    
    /**
     * 获取媒体类型
     */
    public MediaType getMediaType(String fileName) {
        if (fileName == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "txt" -> MediaType.TEXT_PLAIN;
            case "doc", "docx" -> MediaType.valueOf("application/msword");
            case "xls", "xlsx" -> MediaType.valueOf("application/vnd.ms-excel");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(Long fileId) {
        try {
            logger.info("删除文件 - fileId: {}", fileId);
            
            // 调用后端API删除文件
            String response = webClient.delete()
                .uri("/files/{fileId}", fileId)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 同时删除本地缓存文件
            String fileName = getFileName(fileId);
            Path filePath = Paths.get(storagePath).resolve(fileName);
            File localFile = filePath.toFile();
            if (localFile.exists()) {
                localFile.delete();
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("删除文件失败", e);
            return false;
        }
    }
    
    /**
     * 获取文件信息
     */
    public Map<String, Object> getFileInfo(Long fileId) {
        try {
            logger.info("获取文件信息 - fileId: {}", fileId);
            
            // 调用后端API获取文件信息
            String response = webClient.get()
                .uri("/files/{fileId}/info", fileId)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应，为了简化，返回模拟数据
            return createMockFileInfo(fileId);
            
        } catch (Exception e) {
            logger.error("获取文件信息失败", e);
            return null;
        }
    }
    
    /**
     * 获取文件统计信息
     */
    public Map<String, Object> getFileStatistics() {
        try {
            logger.info("获取文件统计信息");
            
            // 调用后端API获取统计信息
            String response = webClient.get()
                .uri("/files/statistics")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            // 这里应该解析JSON响应，为了简化，返回模拟数据
            return createMockFileStatistics();
            
        } catch (Exception e) {
            logger.error("获取文件统计信息失败", e);
            throw new RuntimeException("获取文件统计信息失败", e);
        }
    }
    
    // 以下是模拟数据方法
    
    private Map<String, Object> createMockFileList(int page, int pageSize, String search, String fileType) {
        List<Map<String, Object>> files = new ArrayList<>();
        
        for (int i = 1; i <= pageSize; i++) {
            Map<String, Object> file = new HashMap<>();
            file.put("id", (long) ((page - 1) * pageSize + i));
            file.put("fileName", "document_" + i + ".pdf");
            file.put("fileType", "pdf");
            file.put("fileSize", 1024 * 1024 * 2); // 2MB
            file.put("uploadTime", new Date());
            file.put("orderId", (long) i);
            
            files.add(file);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", files);
        result.put("totalElements", 500L);
        result.put("totalPages", (500 + pageSize - 1) / pageSize);
        result.put("number", page - 1);
        result.put("size", pageSize);
        
        return result;
    }
    
    private Map<String, Object> createMockFileInfo(Long fileId) {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("id", fileId);
        fileInfo.put("fileName", "document_" + fileId + ".pdf");
        fileInfo.put("fileType", "pdf");
        fileInfo.put("fileSize", 1024 * 1024 * 2);
        fileInfo.put("uploadTime", new Date());
        fileInfo.put("orderId", fileId);
        fileInfo.put("pages", 5);
        
        return fileInfo;
    }
    
    private Map<String, Object> createMockFileStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalFiles", 2500);
        statistics.put("totalSize", 1024L * 1024 * 1024 * 5); // 5GB
        statistics.put("pdfFiles", 1800);
        statistics.put("imageFiles", 500);
        statistics.put("documentFiles", 200);
        statistics.put("todayUploads", 45);
        
        return statistics;
    }
}
