package com.team11.hrbank.module.domain.file.controller;

import com.team11.hrbank.module.domain.file.File;
import com.team11.hrbank.module.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController implements FileApi {

    private final FileService fileService;

    /**
     * 파일 다운로드 API
     * 파일을 다운로드하고 원본 파일명을 유지하여 반환.
     * Content-Type을 명확히 지정하여 OpenAPI 명세와 일치하도록 수정.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("id") long id) throws IOException {
        File fileEntity = fileService.getFileById(id);
        log.info("파일 다운로드 요청: {}", fileEntity.getFileName());

        byte[] fileData = fileService.downloadFile(id);
        String encodedFileName = URLEncoder.encode(fileEntity.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream"); // Content-Type 명시적 지정

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}