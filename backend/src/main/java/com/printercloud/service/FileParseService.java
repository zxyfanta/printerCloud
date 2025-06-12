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

/**
 * 文件解析服务
 * 用于解析各种文档格式的页数信息
 */
@Service
public class FileParseService {

    @Autowired
    private PrintFileRepository fileRepository;

    /**
     * 异步解析文件信息
     */
    @Async
    public void parseFileAsync(Long fileId) {
        try {
            PrintFile printFile = fileRepository.findById(fileId).orElse(null);
            if (printFile == null) {
                return;
            }

            // 更新状态为解析中
            printFile.setStatus(2);
            fileRepository.save(printFile);

            // 解析文件
            int pageCount = parseFilePageCount(printFile);
            
            // 更新解析结果
            printFile.setPageCount(pageCount);
            printFile.setStatus(3); // 解析成功
            printFile.setParseError(null);
            
        } catch (Exception e) {
            // 解析失败
            PrintFile printFile = fileRepository.findById(fileId).orElse(null);
            if (printFile != null) {
                printFile.setStatus(4); // 解析失败
                printFile.setParseError(e.getMessage());
                fileRepository.save(printFile);
            }
        }
        
        fileRepository.save(fileRepository.findById(fileId).orElse(null));
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
