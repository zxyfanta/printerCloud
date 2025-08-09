package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.dto.response.FileUploadResponse;
import com.printercloud.entity.FileInfo;
import com.printercloud.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@Tag(name = "文件", description = "文件管理相关接口")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public R<FileUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "fileName", required = false) String fileName,
                                        @RequestAttribute("uid") Long uid) {
        FileUploadResponse resp = fileService.upload(file, fileName, uid);
        return R.success("上传成功", resp);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件")
    public R<Void> delete(@RequestParam("fileId") Long fileId, @RequestAttribute("uid") Long uid) {
        boolean ok = fileService.delete(fileId, uid);
        return ok ? R.success("删除成功") : R.notFound("文件不存在");
    }

    @GetMapping("/info")
    @Operation(summary = "文件信息")
    public R<FileInfo> info(@RequestParam("fileId") Long fileId, @RequestAttribute("uid") Long uid) {
        FileInfo info = fileService.info(fileId, uid);
        return info != null ? R.success(info) : R.notFound("文件不存在");
    }
}
