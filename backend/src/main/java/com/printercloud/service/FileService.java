package com.printercloud.service;

import com.printercloud.entity.PrintFile;
import com.printercloud.repository.PrintFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.UUID;

/**
 * 文件服务类
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class FileService {

    @Autowired
    private PrintFileRepository fileRepository;

    @Autowired
    private FileParseService fileParseService;
    
    @Autowired
    private PdfConversionService pdfConversionService;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 上传文件
     */
    public PrintFile uploadFile(MultipartFile file, Long userId) throws IOException {
        // 验证文件
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        originalFilename = StringUtils.cleanPath(originalFilename);

        // 生成新的文件名
        String fileExtension = getFileExtension(originalFilename);
        String newFileName = UUID.randomUUID().toString() + "." + fileExtension;

        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 先保存文件到磁盘（不计算MD5，快速响应）
        Path targetLocation = uploadDir.resolve(newFileName);
        try (InputStream inputStream = file.getInputStream();
             FileOutputStream fos = new FileOutputStream(targetLocation.toFile())) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        // 创建文件记录（不包含MD5）
        PrintFile printFile = new PrintFile();
        printFile.setUserId(userId);
        printFile.setOriginalName(originalFilename);
        printFile.setFileName(newFileName);
        printFile.setFilePath(targetLocation.toString());
        printFile.setFileSize(file.getSize());
        printFile.setFileType(file.getContentType());
        printFile.setStatus(PrintFile.STATUS_UPLOADED); // 上传成功

        // 保存文件记录
        PrintFile savedFile = fileRepository.save(printFile);

        // 异步处理文件（计算MD5 + 解析文件信息）
        fileParseService.processFileAsync(savedFile.getId());

        // 如果是PDF文件，直接设置预览路径
        if (savedFile.isPdf()) {
            savedFile.setPreviewPath(savedFile.getFilePath());
            fileRepository.save(savedFile);
        }

        return savedFile;
    }

    /**
     * 根据ID获取文件
     */
    public PrintFile getFileById(Long id) {
        return fileRepository.findById(id).orElse(null);
    }

    /**
     * 获取用户文件列表
     */
    public List<PrintFile> getUserFiles(Long userId) {
        return fileRepository.findByUserIdAndDeletedFalseOrderByCreateTimeDesc(userId);
    }

    /**
     * 分页获取文件列表
     */
    public Page<PrintFile> getFileList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return fileRepository.findByDeletedFalseOrderByCreateTimeDesc(pageable);
    }

    /**
     * 分页获取用户文件列表
     */
    public Page<PrintFile> getUserFileList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return fileRepository.findByUserIdAndDeletedFalse(userId, pageable);
    }

    /**
     * 下载文件
     */
    public Resource downloadFile(Long fileId, Long currentUserId, String currentUserRole) throws MalformedURLException {
        PrintFile printFile = getFileById(fileId);
        if (printFile == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 权限检查：用户只能下载自己的文件，管理员可以下载所有文件
        if (!"ADMIN".equals(currentUserRole) && !"SUPER_ADMIN".equals(currentUserRole)) {
            if (!printFile.getUserId().equals(currentUserId)) {
                throw new SecurityException("无权限下载此文件");
            }
        }

        Path filePath = Paths.get(printFile.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IllegalArgumentException("文件不存在或无法读取");
        }
    }

    /**
     * 删除文件（软删除）
     */
    public void deleteFile(Long fileId, Long currentUserId, String currentUserRole) {
        PrintFile printFile = getFileById(fileId);
        if (printFile == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 权限检查：用户只能删除自己的文件，管理员可以删除所有文件
        if (!"ADMIN".equals(currentUserRole) && !"SUPER_ADMIN".equals(currentUserRole)) {
            if (!printFile.getUserId().equals(currentUserId)) {
                throw new SecurityException("无权限删除此文件");
            }
        }

        printFile.setDeleted(true);
        fileRepository.save(printFile);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }



    /**
     * 检查文件是否存在（根据MD5）
     */
    public PrintFile findFileByMd5(String md5) {
        return fileRepository.findByFileMd5AndDeletedFalse(md5).orElse(null);
    }

    /**
     * 获取文件统计信息
     */
    public long getTotalFileCount() {
        return fileRepository.countActiveFiles();
    }

    /**
     * 获取用户文件统计信息
     */
    public long getUserFileCount(Long userId) {
        return fileRepository.countByUserIdAndDeletedFalse(userId);
    }

    /**
     * 获取用户文件总大小
     */
    public long getUserFileTotalSize(Long userId) {
        return fileRepository.sumFileSizeByUserId(userId);
    }

    /**
     * 获取文件资源
     */
    public Resource getFileResource(String filePath) throws MalformedURLException {
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            return null;
        }
    }
}
