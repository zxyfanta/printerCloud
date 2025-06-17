package com.printercloud.service;

import com.printercloud.entity.PrintFile;
import com.printercloud.repository.PrintFileRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件解析服务
 * 用于解析各种文档格式的页数信息
 */
@Service
public class FileParseService {

    @Autowired
    private PrintFileRepository fileRepository;

    /**
     * 异步处理文件（计算MD5 + 解析文件信息）
     */
    @Async
    public void processFileAsync(Long fileId) {
        PrintFile printFile = fileRepository.findById(fileId).orElse(null);
        if (printFile == null) {
            return;
        }

        try {
            // 小文件优化：对于小于1MB的文件，快速处理
            boolean isSmallFile = printFile.getFileSize() != null && printFile.getFileSize() < 1024 * 1024; // 1MB

            if (isSmallFile) {
                // 小文件快速处理：同时计算MD5和解析
                processSmallFileSync(printFile);
            } else {
                // 大文件分步处理
                processLargeFileAsync(printFile);
            }

        } catch (Exception e) {
            // 处理失败
            printFile.setStatus(PrintFile.STATUS_FAILED);
            printFile.setParseError(e.getMessage());
            fileRepository.save(printFile);
        }
    }

    /**
     * 处理小文件（同步快速处理）
     */
    private void processSmallFileSync(PrintFile printFile) throws IOException {
        // 直接设置为解析中状态，跳过MD5计算状态
        printFile.setStatus(PrintFile.STATUS_PARSING);
        fileRepository.save(printFile);

        // 同时计算MD5和解析文件
        String md5 = calculateFileMD5(printFile.getFilePath());
        int pageCount = parseFilePageCount(printFile);

        // 一次性更新所有结果
        printFile.setFileMd5(md5);
        printFile.setPageCount(pageCount);
        printFile.setStatus(PrintFile.STATUS_COMPLETED);
        printFile.setParseError(null);
        fileRepository.save(printFile);
    }

    /**
     * 处理大文件（分步异步处理）
     */
    private void processLargeFileAsync(PrintFile printFile) throws IOException {
        // 第一步：计算MD5
        printFile.setStatus(PrintFile.STATUS_CALCULATING_MD5);
        fileRepository.save(printFile);

        String md5 = calculateFileMD5(printFile.getFilePath());
        printFile.setFileMd5(md5);
        fileRepository.save(printFile);

        // 第二步：解析文件信息
        printFile.setStatus(PrintFile.STATUS_PARSING);
        fileRepository.save(printFile);

        int pageCount = parseFilePageCount(printFile);

        // 更新最终结果
        printFile.setPageCount(pageCount);
        printFile.setStatus(PrintFile.STATUS_COMPLETED);
        printFile.setParseError(null);
        fileRepository.save(printFile);
    }

    /**
     * 异步解析文件信息（保留原方法以兼容）
     */
    @Async
    public void parseFileAsync(Long fileId) {
        try {
            PrintFile printFile = fileRepository.findById(fileId).orElse(null);
            if (printFile == null) {
                return;
            }

            // 更新状态为解析中
            printFile.setStatus(PrintFile.STATUS_PARSING);
            fileRepository.save(printFile);

            // 解析文件
            int pageCount = parseFilePageCount(printFile);

            // 更新解析结果
            printFile.setPageCount(pageCount);
            printFile.setStatus(PrintFile.STATUS_COMPLETED);
            printFile.setParseError(null);
            fileRepository.save(printFile);

        } catch (Exception e) {
            // 解析失败
            PrintFile printFile = fileRepository.findById(fileId).orElse(null);
            if (printFile != null) {
                printFile.setStatus(PrintFile.STATUS_FAILED);
                printFile.setParseError(e.getMessage());
                fileRepository.save(printFile);
            }
        }
    }

    /**
     * 解析文件页数
     */
    private int parseFilePageCount(PrintFile printFile) throws IOException {
        String filePath = printFile.getFilePath();
        String fileExtension = printFile.getFileExtension().toLowerCase();
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        switch (fileExtension) {
            case "pdf":
                return parsePdfPageCount(file);
            case "doc":
                return parseDocPageCount(file);
            case "docx":
                return parseDocxPageCount(file);
            case "xls":
            case "xlsx":
                return parseExcelPageCount(file, fileExtension);
            case "ppt":
                return parsePptPageCount(file);
            case "pptx":
                return parsePptxPageCount(file);
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return 1; // 图片文件默认1页
            default:
                // 未知格式，根据文件大小估算
                return estimatePageCountBySize(printFile.getFileSize());
        }
    }

    /**
     * 解析PDF页数
     */
    private int parsePdfPageCount(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages();
        }
    }

    /**
     * 解析Word 97-2003文档页数
     */
    private int parseDocPageCount(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis)) {
            
            Range range = document.getRange();
            // Word文档页数计算比较复杂，这里使用简单估算
            int charCount = range.text().length();
            return Math.max(1, charCount / 2000); // 假设每页约2000字符
        }
    }

    /**
     * 解析Word 2007+文档页数
     */
    private int parseDocxPageCount(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 获取文档属性中的页数信息
            try {
                return document.getProperties().getExtendedProperties().getUnderlyingProperties().getPages();
            } catch (Exception e) {
                // 如果无法获取页数属性，使用段落数估算
                int paragraphs = document.getParagraphs().size();
                return Math.max(1, paragraphs / 20); // 假设每页约20段落
            }
        }
    }

    /**
     * 解析Excel页数
     */
    private int parseExcelPageCount(File file, String extension) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            if ("xls".equals(extension)) {
                try (HSSFWorkbook workbook = new HSSFWorkbook(fis)) {
                    return workbook.getNumberOfSheets(); // Excel以工作表数作为页数
                }
            } else {
                try (XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
                    return workbook.getNumberOfSheets();
                }
            }
        }
    }

    /**
     * 解析PowerPoint 97-2003页数
     */
    private int parsePptPageCount(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HSLFSlideShow slideShow = new HSLFSlideShow(fis)) {
            return slideShow.getSlides().size();
        }
    }

    /**
     * 解析PowerPoint 2007+页数
     */
    private int parsePptxPageCount(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow slideShow = new XMLSlideShow(fis)) {
            return slideShow.getSlides().size();
        }
    }

    /**
     * 根据文件大小估算页数
     */
    private int estimatePageCountBySize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return 1;
        }
        
        // 假设每页约50KB
        int estimatedPages = (int) Math.ceil(fileSize / (50.0 * 1024));
        return Math.max(1, Math.min(estimatedPages, 1000)); // 限制在1-1000页之间
    }

    /**
     * 计算文件MD5
     */
    private String calculateFileMD5(String filePath) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Path path = Paths.get(filePath);

            try (InputStream is = Files.newInputStream(path);
                 DigestInputStream dis = new DigestInputStream(is, md)) {

                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                    // 只是为了读取文件内容，不需要处理读取的数据
                }
            }

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 同步解析文件（用于小文件或测试）
     */
    public int parseFilePageCountSync(PrintFile printFile) {
        try {
            return parseFilePageCount(printFile);
        } catch (Exception e) {
            // 解析失败时返回估算值
            return estimatePageCountBySize(printFile.getFileSize());
        }
    }
}
