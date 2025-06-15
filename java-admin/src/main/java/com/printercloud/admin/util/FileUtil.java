package com.printercloud.admin.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

/**
 * 文件工具类
 */
@Component
public class FileUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    
    // 支持的图片格式
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp"
    );
    
    // 支持的文档格式
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf"
    );
    
    // 支持的打印格式
    private static final List<String> PRINTABLE_EXTENSIONS = Arrays.asList(
        "pdf", "jpg", "jpeg", "png", "gif", "bmp", "txt", "doc", "docx"
    );

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(File file) {
        if (file == null || file.getName() == null) {
            return "";
        }
        
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 检查文件是否为图片
     */
    public boolean isImageFile(File file) {
        String extension = getFileExtension(file);
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 检查文件是否为文档
     */
    public boolean isDocumentFile(File file) {
        String extension = getFileExtension(file);
        return DOCUMENT_EXTENSIONS.contains(extension);
    }

    /**
     * 检查文件是否可打印
     */
    public boolean isPrintableFile(File file) {
        String extension = getFileExtension(file);
        return PRINTABLE_EXTENSIONS.contains(extension);
    }

    /**
     * 读取文件为字符串
     */
    public String readFileAsString(File file) throws IOException {
        return readFileAsString(file, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件为字符串（指定编码）
     */
    public String readFileAsString(File file, java.nio.charset.Charset charset) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + file.getAbsolutePath());
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            return IOUtils.toString(fis, charset);
        }
    }

    /**
     * 读取文件为字节数组
     */
    public byte[] readFileAsBytes(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + file.getAbsolutePath());
        }
        
        return Files.readAllBytes(file.toPath());
    }

    /**
     * 写入字符串到文件
     */
    public void writeStringToFile(File file, String content) throws IOException {
        writeStringToFile(file, content, StandardCharsets.UTF_8);
    }

    /**
     * 写入字符串到文件（指定编码）
     */
    public void writeStringToFile(File file, String content, java.nio.charset.Charset charset) throws IOException {
        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.write(content, fos, charset);
        }
    }

    /**
     * 写入字节数组到文件
     */
    public void writeBytesToFile(File file, byte[] data) throws IOException {
        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        Files.write(file.toPath(), data);
    }

    /**
     * 复制文件
     */
    public void copyFile(File source, File destination) throws IOException {
        if (!source.exists()) {
            throw new FileNotFoundException("源文件不存在: " + source.getAbsolutePath());
        }
        
        // 确保目标目录存在
        File parentDir = destination.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        Files.copy(source.toPath(), destination.toPath());
    }

    /**
     * 移动文件
     */
    public void moveFile(File source, File destination) throws IOException {
        if (!source.exists()) {
            throw new FileNotFoundException("源文件不存在: " + source.getAbsolutePath());
        }
        
        // 确保目标目录存在
        File parentDir = destination.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        Files.move(source.toPath(), destination.toPath());
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        try {
            Files.delete(file.toPath());
            return true;
        } catch (IOException e) {
            logger.error("删除文件失败: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * 计算文件MD5
     */
    public String calculateMD5(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + file.getAbsolutePath());
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("计算MD5失败", e);
        }
    }

    /**
     * 格式化文件大小
     */
    public String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 创建目录
     */
    public boolean createDirectory(String path) {
        try {
            Path dirPath = Paths.get(path);
            Files.createDirectories(dirPath);
            return true;
        } catch (IOException e) {
            logger.error("创建目录失败: {}", path, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在且可读
     */
    public boolean isFileReadable(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        File file = new File(filePath);
        return file.exists() && file.isFile() && file.canRead();
    }

    /**
     * 获取文件MIME类型
     */
    public String getMimeType(File file) {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            logger.warn("获取文件MIME类型失败: {}", file.getAbsolutePath(), e);
            return "application/octet-stream";
        }
    }

    /**
     * 获取临时目录
     */
    public String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 创建临时文件
     */
    public File createTempFile(String prefix, String suffix) throws IOException {
        return File.createTempFile(prefix, suffix);
    }

    /**
     * 清理临时文件
     */
    public void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            deleteFile(tempFile);
        }
    }
}
