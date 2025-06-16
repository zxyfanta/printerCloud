package com.printercloud.service;

import com.printercloud.entity.PrintFile;
import com.printercloud.repository.PrintFileRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * PDF转换服务
 * 用于将各种文档格式转换为PDF
 */
@Service
public class PdfConversionService {

    private static final Logger logger = LoggerFactory.getLogger(PdfConversionService.class);

    @Autowired
    private PrintFileRepository fileRepository;
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    // 添加一个方法来获取支持中文的字体
    private PDType0Font getChineseFont(PDDocument document) throws IOException {
        try {
            // 尝试加载系统中的中文字体
            // 常见的中文字体名称
            String[] fontNames = {
                "/System/Library/Fonts/PingFang.ttc",
                "/System/Library/Fonts/STHeiti Light.ttc",
                "/System/Library/Fonts/Arial Unicode.ttf",
                "/System/Library/Fonts/AppleGothic.ttf",
                "/usr/share/fonts/truetype/arphic/uming.ttc",
                "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/msyh.ttf"
            };
            
            for (String fontPath : fontNames) {
                try {
                    File fontFile = new File(fontPath);
                    if (fontFile.exists()) {
                        return PDType0Font.load(document, fontFile);
                    }
                } catch (Exception e) {
                    logger.warn("无法加载字体: {}", fontPath, e);
                }
            }
            
            // 如果找不到中文字体，创建一个内嵌的基本字体
            logger.warn("未找到中文字体，将使用基本字体，中文可能无法正确显示");
            return PDType0Font.load(document, getClass().getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"));
        } catch (Exception e) {
            logger.error("加载中文字体失败，将使用基本字体", e);
            // 如果所有尝试都失败，返回基本字体
            return PDType0Font.load(document, getClass().getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"));
        }
    }
    
    // 添加一个方法来获取支持中文的粗体字体
    private PDType0Font getChineseBoldFont(PDDocument document) throws IOException {
        try {
            // 尝试加载系统中的中文粗体字体
            String[] fontNames = {
                "/System/Library/Fonts/PingFang.ttc",
                "/System/Library/Fonts/STHeiti Medium.ttc",
                "/System/Library/Fonts/Arial Unicode.ttf",
                "/System/Library/Fonts/AppleGothic.ttf",
                "/usr/share/fonts/truetype/arphic/uming.ttc",
                "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/msyh.ttf"
            };
            
            for (String fontPath : fontNames) {
                try {
                    File fontFile = new File(fontPath);
                    if (fontFile.exists()) {
                        return PDType0Font.load(document, fontFile);
                    }
                } catch (Exception e) {
                    logger.warn("无法加载粗体字体: {}", fontPath, e);
                }
            }
            
            // 如果找不到中文粗体字体，创建一个内嵌的基本粗体字体
            logger.warn("未找到中文粗体字体，将使用基本粗体字体，中文可能无法正确显示");
            return PDType0Font.load(document, getClass().getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Bold.ttf"));
        } catch (Exception e) {
            logger.error("加载中文粗体字体失败，将使用基本粗体字体", e);
            // 如果所有尝试都失败，返回基本粗体字体
            return PDType0Font.load(document, getClass().getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Bold.ttf"));
        }
    }
    
    /**
     * 异步将文件转换为PDF
     */
    @Async
    public void convertToPdfAsync(Long fileId) {
        logger.info("开始异步转换文件为PDF，文件ID: {}", fileId);
        try {
            PrintFile printFile = fileRepository.findById(fileId).orElse(null);
            if (printFile == null) {
                logger.error("文件不存在，ID: {}", fileId);
                return;
            }
            
            // 如果已经是PDF或者是图片，则不需要转换
            if (printFile.isPdf()) {
                logger.info("文件已经是PDF格式，不需要转换，文件ID: {}", fileId);
                // 对于PDF文件，直接将原文件路径设置为预览路径
                printFile.setPreviewPath(printFile.getFilePath());
                fileRepository.save(printFile);
                return;
            }
            
            // 转换文件为PDF
            String pdfPath = convertToPdf(printFile);
            
            // 更新文件记录
            if (pdfPath != null) {
                printFile.setPreviewPath(pdfPath);
                fileRepository.save(printFile);
                logger.info("文件成功转换为PDF，文件ID: {}, PDF路径: {}", fileId, pdfPath);
            }
            
        } catch (Exception e) {
            // 转换失败，记录错误信息
            logger.error("PDF转换失败，文件ID: {}", fileId, e);
            PrintFile printFile = fileRepository.findById(fileId).orElse(null);
            if (printFile != null) {
                printFile.setParseError("PDF转换失败: " + e.getMessage());
                fileRepository.save(printFile);
            }
        }
    }
    
    /**
     * 将文件转换为PDF
     * @return PDF文件路径
     */
    private String convertToPdf(PrintFile printFile) throws IOException {
        String filePath = printFile.getFilePath();
        String fileExtension = printFile.getFileExtension().toLowerCase();
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        // 生成PDF文件名和路径
        String pdfFileName = printFile.getId() + "_preview.pdf";
        Path pdfDir = Paths.get(uploadPath, "pdf_previews");
        if (!Files.exists(pdfDir)) {
            Files.createDirectories(pdfDir);
        }
        Path pdfPath = pdfDir.resolve(pdfFileName);
        
        logger.info("开始转换文件: {} 为PDF: {}", filePath, pdfPath);
        
        // 根据文件类型选择转换方法
        switch (fileExtension) {
            case "doc":
                convertDocToPdf(file, pdfPath.toFile());
                break;
            case "docx":
                convertDocxToPdf(file, pdfPath.toFile());
                break;
            case "xls":
                convertXlsToPdf(file, pdfPath.toFile());
                break;
            case "xlsx":
                convertXlsxToPdf(file, pdfPath.toFile());
                break;
            case "ppt":
                convertPptToPdf(file, pdfPath.toFile());
                break;
            case "pptx":
                convertPptxToPdf(file, pdfPath.toFile());
                break;
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                convertImageToPdf(file, pdfPath.toFile());
                break;
            default:
                // 不支持的格式，创建一个包含错误信息的PDF
                logger.warn("不支持的文件格式: {}", fileExtension);
                createErrorPdf(pdfPath.toFile(), "不支持的文件格式: " + fileExtension);
        }
        
        return pdfPath.toString();
    }
    
    /**
     * 将Word 97-2003文档转换为PDF
     */
    private void convertDocToPdf(File docFile, File pdfFile) throws IOException {
        logger.info("转换DOC文件: {} 为PDF: {}", docFile.getPath(), pdfFile.getPath());
        try (FileInputStream fis = new FileInputStream(docFile);
             HWPFDocument doc = new HWPFDocument(fis);
             PDDocument pdf = new PDDocument()) {
            
            // 获取文档页数和文本
            int pageCount = doc.getSummaryInformation().getPageCount();
            String text = doc.getDocumentText();
            
            // 如果页数未知或为0，估算页数
            if (pageCount <= 0) {
                // 估算页数：假设每页2000个字符
                pageCount = Math.max(1, text.length() / 2000);
            }
            
            // 为每一页创建PDF页面
            for (int i = 0; i < pageCount; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                
                // 计算当前页的文本范围
                int startChar = i * (text.length() / pageCount);
                int endChar = Math.min(startChar + (text.length() / pageCount), text.length());
                String pageText = text.substring(startChar, endChar);
                
                // 添加文本到页面
                try (PDPageContentStream contentStream = new PDPageContentStream(pdf, page)) {
                    contentStream.beginText();
                    PDType0Font font = getChineseFont(pdf);
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(50, 750);
                    
                    // 分行显示文本
                    String[] lines = pageText.split("\\r\\n|\\n|\\r");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        
                        // 处理过长的行
                        int maxCharsPerLine = 100; // 每行最大字符数
                        for (int j = 0; j < line.length(); j += maxCharsPerLine) {
                            String subLine = line.substring(j, Math.min(j + maxCharsPerLine, line.length()));
                            contentStream.showText(subLine);
                            contentStream.newLineAtOffset(0, -12);
                        }
                    }
                    
                    contentStream.endText();
                }
            }
            
            pdf.save(pdfFile);
        } catch (Exception e) {
            logger.error("转换DOC文件失败", e);
            createErrorPdf(pdfFile, "Word 97-2003文档转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Word 2007+文档转换为PDF
     */
    private void convertDocxToPdf(File docxFile, File pdfFile) throws IOException {
        logger.info("转换DOCX文件: {} 为PDF: {}", docxFile.getPath(), pdfFile.getPath());
        try (FileInputStream fis = new FileInputStream(docxFile);
             XWPFDocument doc = new XWPFDocument(fis);
             PDDocument pdf = new PDDocument()) {
            
            // 获取文档页数
            int pageCount = doc.getProperties().getExtendedProperties().getUnderlyingProperties().getPages();
            
            // 如果页数未知或为0，估算页数
            if (pageCount <= 0) {
                // 估算页数：假设每个段落占用0.2页
                pageCount = Math.max(1, doc.getParagraphs().size() / 5);
            }
            
            // 为每一页创建PDF页面
            for (int i = 0; i < pageCount; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(pdf, page)) {
                    contentStream.beginText();
                    // 使用中文字体替换西文字体
                    PDType0Font font = getChineseFont(pdf);
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(50, 750);
                    
                    // 添加页码信息
                    contentStream.showText("第 " + (i + 1) + " 页，共 " + pageCount + " 页");
                    contentStream.newLineAtOffset(0, -20);
                    
                    // 如果是第一页，添加文档信息
                    if (i == 0) {
                        PDType0Font boldFont = getChineseBoldFont(pdf);
                        contentStream.setFont(boldFont, 12);
                        contentStream.showText("文档: " + docxFile.getName());
                        contentStream.newLineAtOffset(0, -20);
                        contentStream.setFont(font, 10);
                    }
                    
                    contentStream.endText();
                }
            }
            
            pdf.save(pdfFile);
        } catch (Exception e) {
            logger.error("转换DOCX文件失败", e);
            createErrorPdf(pdfFile, "Word 2007+文档转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Excel 97-2003文档转换为PDF
     */
    private void convertXlsToPdf(File xlsFile, File pdfFile) throws IOException {
        logger.info("转换XLS文件: {} 为PDF: {}", xlsFile.getPath(), pdfFile.getPath());
        try (FileInputStream fis = new FileInputStream(xlsFile);
             HSSFWorkbook workbook = new HSSFWorkbook(fis);
             PDDocument pdf = new PDDocument()) {
            
            // 获取工作表数量
            int sheetCount = workbook.getNumberOfSheets();
            
            // 为每个工作表创建一个PDF页面
            for (int i = 0; i < sheetCount; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(pdf, page)) {
                    contentStream.beginText();
                    PDType0Font boldFont = getChineseBoldFont(pdf);
                    contentStream.setFont(boldFont, 12);
                    contentStream.newLineAtOffset(50, 750);
                    
                    // 添加工作表名称
                    String sheetName = workbook.getSheetName(i);
                    contentStream.showText("工作表: " + sheetName);
                    contentStream.newLineAtOffset(0, -20);
                    
                    // 添加文件信息
                    if (i == 0) {
                        contentStream.showText("Excel文件: " + xlsFile.getName());
                        contentStream.newLineAtOffset(0, -20);
                    }
                    
                    contentStream.endText();
                }
            }
            
            pdf.save(pdfFile);
        } catch (Exception e) {
            logger.error("转换XLS文件失败", e);
            createErrorPdf(pdfFile, "Excel 97-2003文档转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Excel 2007+文档转换为PDF
     */
    private void convertXlsxToPdf(File xlsxFile, File pdfFile) throws IOException {
        logger.info("转换XLSX文件: {} 为PDF: {}", xlsxFile.getPath(), pdfFile.getPath());
        try (FileInputStream fis = new FileInputStream(xlsxFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis);
             PDDocument pdf = new PDDocument()) {
            
            // 获取工作表数量
            int sheetCount = workbook.getNumberOfSheets();
            
            // 为每个工作表创建一个PDF页面
            for (int i = 0; i < sheetCount; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(pdf, page)) {
                    contentStream.beginText();
                    PDType0Font boldFont = getChineseBoldFont(pdf);
                    contentStream.setFont(boldFont, 12);
                    contentStream.newLineAtOffset(50, 750);
                    
                    // 添加工作表名称
                    String sheetName = workbook.getSheetName(i);
                    contentStream.showText("工作表: " + sheetName);
                    contentStream.newLineAtOffset(0, -20);
                    
                    // 添加文件信息
                    if (i == 0) {
                        contentStream.showText("Excel文件: " + xlsxFile.getName());
                        contentStream.newLineAtOffset(0, -20);
                    }
                    
                    contentStream.endText();
                }
            }
            
            pdf.save(pdfFile);
        } catch (Exception e) {
            logger.error("转换XLSX文件失败", e);
            createErrorPdf(pdfFile, "Excel 2007+文档转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 将PowerPoint 97-2003文档转换为PDF
     */
    private void convertPptToPdf(File pptFile, File pdfFile) throws IOException {
        logger.info("转换PPT文件: {} 为PDF: {}", pptFile.getPath(), pdfFile.getPath());
        try (FileInputStream fis = new FileInputStream(pptFile);
             HSLFSlideShow ppt = new HSLFSlideShow(fis);
             PDDocument pdf = new PDDocument()) {
            
            // 获取幻灯片数量
            int slideCount = ppt.getSlides().size();
            
            // 为每个幻灯片创建一个PDF页面
            for (int i = 0; i < slideCount; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(pdf, page)) {
                    contentStream.beginText();
                    PDType0Font boldFont = getChineseBoldFont(pdf);
                    contentStream.setFont(boldFont, 12);
                    contentStream.newLineAtOffset(50, 750);
                    
                    // 添加幻灯片信息
                    contentStream.showText("幻灯片 " + (i + 1) + " / " + slideCount);
                    contentStream.newLineAtOffset(0, -20);
                    
                    // 添加文件信息
                    if (i == 0) {
                        contentStream.showText("PowerPoint文件: " + pptFile.getName());
                        contentStream.newLineAtOffset(0, -20);
                    }
                    
                    contentStream.endText();
                }
            }
            
            pdf.save(pdfFile);
        } catch (Exception e) {
            logger.error("转换PPT文件失败", e);
            createErrorPdf(pdfFile, "PowerPoint 97-2003文档转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 将PowerPoint 2007+文档转换为PDF
     */
    private void convertPptxToPdf(File pptxFile, File pdfFile) throws IOException {
        logger.info("转换PPTX文件: {} 为PDF: {}", pptxFile.getPath(), pdfFile.getPath());
        try (FileInputStream fis = new FileInputStream(pptxFile);
             XMLSlideShow ppt = new XMLSlideShow(fis);
             PDDocument pdf = new PDDocument()) {
            
            // 获取幻灯片数量
            int slideCount = ppt.getSlides().size();
            
            // 为每个幻灯片创建一个PDF页面
            for (int i = 0; i < slideCount; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(pdf, page)) {
                    contentStream.beginText();
                    PDType0Font boldFont = getChineseBoldFont(pdf);
                    contentStream.setFont(boldFont, 12);
                    contentStream.newLineAtOffset(50, 750);
                    
                    // 添加幻灯片信息
                    contentStream.showText("幻灯片 " + (i + 1) + " / " + slideCount);
                    contentStream.newLineAtOffset(0, -20);
                    
                    // 添加文件信息
                    if (i == 0) {
                        contentStream.showText("PowerPoint文件: " + pptxFile.getName());
                        contentStream.newLineAtOffset(0, -20);
                    }
                    
                    contentStream.endText();
                }
            }
            
            pdf.save(pdfFile);
        } catch (Exception e) {
            logger.error("转换PPTX文件失败", e);
            createErrorPdf(pdfFile, "PowerPoint 2007+文档转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 将图片转换为PDF
     */
    private void convertImageToPdf(File imageFile, File pdfFile) throws IOException {
        logger.info("转换图片: {} 为PDF: {}", imageFile.getPath(), pdfFile.getPath());
        try (PDDocument document = new PDDocument()) {
            // 创建页面
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            // 加载图片
            PDImageXObject image = PDImageXObject.createFromFileByContent(imageFile, document);
            
            // 计算图片在页面上的位置和大小
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float imageWidth = image.getWidth();
            float imageHeight = image.getHeight();
            
            // 计算缩放比例，使图片适应页面
            float scale = Math.min(pageWidth / imageWidth, pageHeight / imageHeight) * 0.9f;
            float scaledWidth = imageWidth * scale;
            float scaledHeight = imageHeight * scale;
            
            // 计算图片在页面上的位置（居中）
            float x = (pageWidth - scaledWidth) / 2;
            float y = (pageHeight - scaledHeight) / 2;
            
            // 创建内容流并绘制图片
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(image, x, y, scaledWidth, scaledHeight);
            }
            
            // 保存PDF
            document.save(pdfFile);
        } catch (Exception e) {
            logger.error("转换图片失败", e);
            createErrorPdf(pdfFile, "图片转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建包含错误信息的PDF
     */
    private void createErrorPdf(File pdfFile, String errorMessage) throws IOException {
        logger.info("创建错误提示PDF: {}, 错误信息: {}", pdfFile.getPath(), errorMessage);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                // 使用中文粗体字体
                PDType0Font boldFont = getChineseBoldFont(document);
                contentStream.setFont(boldFont, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("文件转换错误");
                contentStream.newLineAtOffset(0, -20);
                // 使用中文常规字体
                PDType0Font font = getChineseFont(document);
                contentStream.setFont(font, 10);
                contentStream.showText(errorMessage);
                contentStream.endText();
            }
            
            document.save(pdfFile);
        }
    }
}