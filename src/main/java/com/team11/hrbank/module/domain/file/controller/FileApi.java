package com.team11.hrbank.module.domain.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@RequestMapping("/api/files")
@Tag(name = "파일 관리", description = "파일 관리 API")
public interface FileApi {

  /**
   * 파일 다운로드 API
   * 파일을 다운로드하고 원본 파일명을 유지하여 반환.
   * Content-Type을 명확히 지정하여 OpenAPI 명세와 일치하도록 수정.
   */
  @Operation(
      summary = "파일 다운로드",
      description = "파일을 다운로드합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "다운로드 성공"),
          @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  @GetMapping("/{id}/download")
  ResponseEntity<byte[]> downloadFile(
      @Parameter(description = "파일 ID", required = true)
      @PathVariable("id") long id) throws IOException;
}