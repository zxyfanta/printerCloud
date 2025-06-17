package com.printercloud.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * 文件服务性能测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class FileServicePerformanceTest {

    /**
     * 测试MD5计算性能
     */
    @Test
    public void testMD5CalculationPerformance() throws IOException, NoSuchAlgorithmException {
        // 创建测试文件数据（1MB）
        byte[] testData = new byte[1024 * 1024];
        new Random().nextBytes(testData);
        
        System.out.println("测试文件大小: " + testData.length + " bytes");
        
        // 测试原始方法（读取两次）
        long startTime = System.currentTimeMillis();
        String md5Old = calculateMD5Old(testData);
        long oldTime = System.currentTimeMillis() - startTime;
        System.out.println("原始方法耗时: " + oldTime + "ms, MD5: " + md5Old);
        
        // 测试优化方法（只读取一次）
        startTime = System.currentTimeMillis();
        String md5New = calculateMD5Optimized(testData);
        long newTime = System.currentTimeMillis() - startTime;
        System.out.println("优化方法耗时: " + newTime + "ms, MD5: " + md5New);
        
        // 验证结果一致性
        assert md5Old.equals(md5New) : "MD5计算结果不一致";
        
        System.out.println("性能提升: " + ((double)(oldTime - newTime) / oldTime * 100) + "%");
    }
    
    /**
     * 测试不同文件大小的上传性能
     */
    @Test
    public void testUploadPerformanceWithDifferentSizes() throws IOException {
        int[] fileSizes = {1024, 10240, 102400, 1048576}; // 1KB, 10KB, 100KB, 1MB
        
        for (int size : fileSizes) {
            byte[] testData = new byte[size];
            new Random().nextBytes(testData);
            
            MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.pdf", 
                "application/pdf", 
                testData
            );
            
            long startTime = System.currentTimeMillis();
            
            // 模拟文件上传处理
            try {
                simulateFileUpload(file);
            } catch (Exception e) {
                System.err.println("文件上传模拟失败: " + e.getMessage());
            }
            
            long endTime = System.currentTimeMillis();
            
            System.out.println(String.format(
                "文件大小: %d bytes (%.2f KB), 处理耗时: %d ms", 
                size, 
                size / 1024.0, 
                endTime - startTime
            ));
        }
    }
    
    /**
     * 原始MD5计算方法（模拟读取两次文件）
     */
    private String calculateMD5Old(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        
        // 第一次读取：保存文件（模拟）
        // 这里只是模拟，实际中会写入磁盘
        
        // 第二次读取：计算MD5
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * 优化的MD5计算方法（只读取一次）
     */
    private String calculateMD5Optimized(byte[] data) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        
        // 使用DigestInputStream同时保存和计算MD5
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             DigestInputStream dis = new DigestInputStream(bis, md)) {
            
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
                // 这里可以同时写入文件
            }
        }
        
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * 模拟文件上传处理
     */
    private void simulateFileUpload(MockMultipartFile file) throws IOException, NoSuchAlgorithmException {
        // 模拟文件验证
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 模拟文件名处理
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 模拟保存文件并计算MD5
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(file.getInputStream(), md)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
                // 模拟写入文件
            }
        }
        
        // 模拟数据库保存
        Thread.yield(); // 模拟数据库操作耗时
    }
    
    /**
     * 测试大文件处理能力
     */
    @Test
    public void testLargeFileHandling() throws IOException, NoSuchAlgorithmException {
        // 创建10MB测试文件
        int fileSize = 10 * 1024 * 1024; // 10MB
        System.out.println("测试大文件处理，文件大小: " + (fileSize / 1024 / 1024) + "MB");
        
        long startTime = System.currentTimeMillis();
        
        // 模拟大文件MD5计算
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        
        // 分块生成和处理数据，避免内存溢出
        Random random = new Random();
        int totalBytes = 0;
        
        while (totalBytes < fileSize) {
            int chunkSize = Math.min(buffer.length, fileSize - totalBytes);
            random.nextBytes(buffer);
            md.update(buffer, 0, chunkSize);
            totalBytes += chunkSize;
        }
        
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("大文件MD5: " + sb.toString());
        System.out.println("处理耗时: " + (endTime - startTime) + "ms");
        System.out.println("处理速度: " + String.format("%.2f MB/s", 
            (fileSize / 1024.0 / 1024.0) / ((endTime - startTime) / 1000.0)));
    }
}
