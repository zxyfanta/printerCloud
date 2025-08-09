package com.printercloud.service;

import com.printercloud.dto.response.FileUploadResponse;
import com.printercloud.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResponse upload(MultipartFile file, String fileName, Long userId);
    boolean delete(Long fileId, Long userId);
    FileInfo info(Long fileId, Long userId);
}
