package com.printercloud.controller;

import com.printercloud.entity.PrintFile;
import com.printercloud.entity.User;
import com.printercloud.service.FileService;
import com.printercloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                response.put("code", 401);
                response.put("message", "token无效");
                return ResponseEntity.ok(response);
            }
            
            PrintFile printFile = fileService.uploadFile(file, currentUser.getId());
            response.put("code", 200);
            response.put("message", "上传成功");
            response.put("data", printFile);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "上传失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFileInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                response.put("code", 401);
                response.put("message", "token无效");
                return ResponseEntity.ok(response);
            }
            
            PrintFile printFile = fileService.getFileById(id);
            if (printFile == null) {
                response.put("code", 404);
                response.put("message", "文件不存在");
                return ResponseEntity.ok(response);
            }
            
            // 权限检查：用户只能查看自己的文件，管理员可以查看所有文件
            if (!currentUser.isAdmin() && !printFile.getUserId().equals(currentUser.getId())) {
                response.put("code", 403);
                response.put("message", "无权限访问此文件");
                return ResponseEntity.ok(response);
            }
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", printFile);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取文件信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            PrintFile printFile = fileService.getFileById(id);
            if (printFile == null) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = fileService.downloadFile(id, currentUser.getId(), currentUser.getRole());
            
            // 设置响应头
            String filename = printFile.getOriginalName();
            try {
                filename = URLEncoder.encode(filename, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // 忽略编码异常
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取用户文件列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserFileList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                response.put("code", 401);
                response.put("message", "token无效");
                return ResponseEntity.ok(response);
            }
            
            if (currentUser.isAdmin()) {
                // 管理员可以查看所有文件
                Page<PrintFile> fileList = fileService.getFileList(page, size);
                response.put("code", 200);
                response.put("message", "获取成功");
                response.put("data", fileList);
            } else {
                // 普通用户只能查看自己的文件
                Page<PrintFile> fileList = fileService.getUserFileList(currentUser.getId(), page, size);
                response.put("code", 200);
                response.put("message", "获取成功");
                response.put("data", fileList);
            }
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取文件列表失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                response.put("code", 401);
                response.put("message", "token无效");
                return ResponseEntity.ok(response);
            }
            
            fileService.deleteFile(id, currentUser.getId(), currentUser.getRole());
            response.put("code", 200);
            response.put("message", "删除成功");
            
        } catch (SecurityException e) {
            response.put("code", 403);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除文件失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFileStatistics(
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                response.put("code", 401);
                response.put("message", "token无效");
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> statistics = new HashMap<>();
            
            if (currentUser.isAdmin()) {
                // 管理员可以查看全局统计
                statistics.put("totalFiles", fileService.getTotalFileCount());
            }
            
            // 用户自己的统计
            statistics.put("userFiles", fileService.getUserFileCount(currentUser.getId()));
            statistics.put("userFileTotalSize", fileService.getUserFileTotalSize(currentUser.getId()));
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", statistics);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
