package com.printercloud.service.impl;

import com.printercloud.dto.response.FileUploadResponse;
import com.printercloud.entity.FileInfo;
import com.printercloud.repository.FileInfoRepository;
import com.printercloud.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileInfoRepository fileInfoRepository;
    private final com.printercloud.service.FilePageCounterService filePageCounterService;

    private static final String UPLOAD_DIR = "uploads";

    @Override
    public FileUploadResponse upload(MultipartFile file, String fileName, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String finalName = (fileName == null || fileName.trim().isEmpty()) ? file.getOriginalFilename() : fileName;
        String storedName = System.currentTimeMillis() + "_" + finalName;

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(dir, storedName);

        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败", e);
        }

        String hash;
        try {
            hash = DigestUtils.md5DigestAsHex(Files.readAllBytes(dest.toPath()));
        } catch (IOException e) {
            hash = "";
        }

        FileInfo fi = new FileInfo();
        fi.setUserId(userId);
        fi.setOriginalName(finalName);
        fi.setStoredName(storedName);
        fi.setFilePath(dest.getAbsolutePath());
        fi.setFileSize(dest.length());
        String type = getTypeFromName(finalName);
        fi.setFileType(type);
        fi.setMimeType(file.getContentType());
        fi.setFileHash(hash);
        fi.setCreatedTime(LocalDateTime.now());
        fi.setUpdatedTime(LocalDateTime.now());
        // 计算页数（解析失败时保存文件但页数为0，交由前端后续手动输入）
        int pages;
        try {
            pages = filePageCounterService.countPages(dest, finalName, file.getContentType());
        } catch (com.printercloud.exception.PageCountException e) {
            pages = 0;
            log.warn("页数解析失败，文件仍保存：{}", e.getMessage());
        }
        fi.setPageCount(pages);
        fileInfoRepository.save(fi);

        FileUploadResponse resp = new FileUploadResponse();
        resp.setId(fi.getId());
        resp.setOriginalName(fi.getOriginalName());
        resp.setFileSize(fi.getFileSize());
        resp.setFormattedFileSize(fi.getFormattedFileSize());
        resp.setFileType(fi.getFileType());
        resp.setFileTypeIcon(fi.getFileTypeIcon());
        resp.setPageCount(fi.getPageCount());
        resp.setWidth(fi.getWidth());
        resp.setHeight(fi.getHeight());
        resp.setFileHash(fi.getFileHash());
        resp.setUploadTime(LocalDateTime.now());
        resp.setIsImage(fi.isImage());
        resp.setIsDocument(fi.isDocument());
        return resp;
    }

    @Override
    public boolean delete(Long fileId, Long userId) {
        return fileInfoRepository.findByIdAndUserId(fileId, userId).map(fi -> {
            File f = new File(fi.getFilePath());
            if (f.exists()) f.delete();
            fileInfoRepository.deleteById(fi.getId());
            return true;
        }).orElse(false);
    }

    @Override
    public FileInfo info(Long fileId, Long userId) {
        return fileInfoRepository.findByIdAndUserId(fileId, userId).orElse(null);
    }

    private String getTypeFromName(String name) {
        if (name == null) return "";
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) return "PDF";
        if (lower.endsWith(".doc")) return "DOC";
        if (lower.endsWith(".docx")) return "DOCX";
        if (lower.endsWith(".xls")) return "XLS";
        if (lower.endsWith(".xlsx")) return "XLSX";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "JPG";
        if (lower.endsWith(".png")) return "PNG";
        return "UNKNOWN";
    }
}
