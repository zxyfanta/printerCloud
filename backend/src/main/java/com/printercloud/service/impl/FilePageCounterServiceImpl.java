package com.printercloud.service.impl;

import com.printercloud.exception.PageCountException;
import com.printercloud.service.FilePageCounterService;
import com.printercloud.service.OnlyOfficeConvertClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilePageCounterServiceImpl implements FilePageCounterService {

    private final OnlyOfficeConvertClient onlyOfficeConvertClient;

    @Override
    public int countPages(File file, String originalName, String contentType) {
        String name = originalName != null ? originalName.toLowerCase() : "";
        try {
            if (name.endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(file)) {
                    return doc.getNumberOfPages();
                }
            }
            if (name.endsWith(".doc") || name.endsWith(".docx")) {
                File pdf = onlyOfficeConvertClient.convertToPdf(file, name.endsWith(".docx") ? "docx" : "doc");
                try (PDDocument doc = PDDocument.load(pdf)) {
                    return doc.getNumberOfPages();
                } finally {
                    // 删除临时PDF
                    if (pdf != null && pdf.exists()) pdf.delete();
                }
            }
            if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                throw new PageCountException("Excel暂不支持，请上传PDF或Word文档");
            }
            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
                return 1;
            }
            throw new PageCountException("不支持的文件类型");
        } catch (PageCountException e) {
            throw e;
        } catch (Exception e) {
            log.warn("页数解析失败: {} -> {}", originalName, e.toString());
            throw new PageCountException("页数解析失败，请重试或上传PDF", e);
        }
    }
}

