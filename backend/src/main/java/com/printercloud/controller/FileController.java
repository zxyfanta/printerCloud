package com.printercloud.controller;

import com.printercloud.common.R;
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
import java.util.Map;
import java.util.HashMap;

/**
 * 文件控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/file")
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
    public R<PrintFile> uploadFile(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                return R.unauthorized("token无效");
            }

            PrintFile printFile = fileService.uploadFile(file, currentUser.getId());
            return R.ok(printFile, "上传成功");

        } catch (Exception e) {
            return R.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{id}")
    public R<PrintFile> getFileInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                return R.unauthorized("token无效");
            }

            PrintFile printFile = fileService.getFileById(id);
            if (printFile == null) {
                return R.notFound("文件不存在");
            }

            // 权限检查：用户只能查看自己的文件，管理员可以查看所有文件
            if (!currentUser.isAdmin() && !printFile.getUserId().equals(currentUser.getId())) {
                return R.forbidden("无权限访问此文件");
            }

            return R.ok(printFile, "获取成功");

        } catch (Exception e) {
            return R.fail("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件基本信息（用于轮询）
     */
    @GetMapping("/info/{id}")
    public R<Map<String, Object>> getFileBasicInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                return R.unauthorized("token无效");
            }

            PrintFile printFile = fileService.getFileById(id);
            if (printFile == null) {
                return R.notFound("文件不存在");
            }

            // 权限检查：用户只能查看自己的文件，管理员可以查看所有文件
            if (!currentUser.isAdmin() && !printFile.getUserId().equals(currentUser.getId())) {
                return R.forbidden("无权限访问此文件");
            }

            // 构建基本信息响应
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("id", printFile.getId());
            fileInfo.put("originalName", printFile.getOriginalName());
            fileInfo.put("fileSize", printFile.getFileSize());
            fileInfo.put("fileType", printFile.getFileType());
            fileInfo.put("pageCount", printFile.getPageCount());
            fileInfo.put("status", printFile.getStatus());
            fileInfo.put("parseError", printFile.getParseError());
            fileInfo.put("createTime", printFile.getCreateTime());
            fileInfo.put("updateTime", printFile.getUpdateTime());

            return R.ok(fileInfo, "获取成功");

        } catch (Exception e) {
            return R.fail("获取文件信息失败: " + e.getMessage());
        }
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
    public R<Page<PrintFile>> getUserFileList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                return R.unauthorized("token无效");
            }

            Page<PrintFile> fileList;
            if (currentUser.isAdmin()) {
                // 管理员可以查看所有文件
                fileList = fileService.getFileList(page, size);
            } else {
                // 普通用户只能查看自己的文件
                fileList = fileService.getUserFileList(currentUser.getId(), page, size);
            }

            return R.ok(fileList, "获取成功");

        } catch (Exception e) {
            return R.fail("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteFile(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                return R.unauthorized("token无效");
            }

            fileService.deleteFile(id, currentUser.getId(), currentUser.getRole());
            return R.ok(null, "删除成功");

        } catch (SecurityException e) {
            return R.forbidden(e.getMessage());
        } catch (Exception e) {
            return R.fail("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")
    public R<Map<String, Object>> getFileStatistics(
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                return R.unauthorized("token无效");
            }

            Map<String, Object> statistics = new HashMap<>();

            if (currentUser.isAdmin()) {
                // 管理员可以查看全局统计
                statistics.put("totalFiles", fileService.getTotalFileCount());
            }

            // 用户自己的统计
            statistics.put("userFiles", fileService.getUserFileCount(currentUser.getId()));
            statistics.put("userFileTotalSize", fileService.getUserFileTotalSize(currentUser.getId()));

            return R.ok(statistics, "获取成功");

        } catch (Exception e) {
            return R.fail("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件预览信息
     */
    @GetMapping("/preview-info/{id}")
    public R<Map<String, Object>> getFilePreviewInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null) {
                return R.unauthorized("token无效");
            }

            PrintFile printFile = fileService.getFileById(id);
            if (printFile == null) {
                return R.notFound("文件不存在");
            }

            // 权限检查：用户只能查看自己的文件，管理员可以查看所有文件
            if (!currentUser.isAdmin() && !printFile.getUserId().equals(currentUser.getId())) {
                return R.forbidden("无权限访问此文件");
            }

            // 构建预览信息
            Map<String, Object> previewInfo = new HashMap<>();
            previewInfo.put("id", printFile.getId());
            previewInfo.put("fileName", printFile.getOriginalName());
            previewInfo.put("fileType", printFile.getFileType());
            previewInfo.put("pageCount", printFile.getPageCount());

            // 只有PDF文件可以预览
            if (printFile.isPdf() && printFile.getPreviewPath() != null && !printFile.getPreviewPath().isEmpty()) {
                // 构建预览URL
                String previewUrl = "/api/file/preview-content/" + printFile.getId();
                previewInfo.put("previewUrl", previewUrl);
                previewInfo.put("previewAvailable", true);
            } else {
                previewInfo.put("previewAvailable", false);
                previewInfo.put("previewMessage", "该文件类型不支持预览");
            }

            return R.ok(previewInfo, "获取成功");

        } catch (Exception e) {
            return R.fail("获取预览信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件预览内容
     */
    @GetMapping("/preview-content/{id}")
    public ResponseEntity<Resource> getFilePreviewContent(
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
            
            // 权限检查：用户只能查看自己的文件，管理员可以查看所有文件
            if (!currentUser.isAdmin() && !printFile.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            // 只有PDF文件可以预览
            if (!printFile.isPdf() || printFile.getPreviewPath() == null || printFile.getPreviewPath().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取预览文件资源
            Resource resource = fileService.getFileResource(printFile.getPreviewPath());
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 设置响应头
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=preview.pdf")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
