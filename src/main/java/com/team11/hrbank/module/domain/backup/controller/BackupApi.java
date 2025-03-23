package com.team11.hrbank.module.domain.backup.controller;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.backup.BackupStatus;
import com.team11.hrbank.module.domain.backup.dto.BackupDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@RequestMapping("/api/backups")
@Tag(name = "데이터 백업 관리", description = "백업 관리 API")
public interface BackupApi {

  @Operation(
      summary = "최근 백업 정보 조회",
      description = "지정된 상태의 가장 최근 백업 정보를 조회합니다. 상태를 지정하지 않으면 성공적으로 완료된(COMPLETED) 백업을 반환합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효하지 않은 상태값"),
          @ApiResponse(responseCode = "404", description = "해당 상태의 백업을 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  @GetMapping("/latest")
  ResponseEntity<BackupDto> getLatestBackup(
      @RequestParam(required = false, defaultValue = "COMPLETED") BackupStatus status
  );

  @Operation(
      summary = "데이터 백업 생성",
      description = "데이터 백업을 생성합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "백업 생성 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "409", description = "이미 진행 중인 백업이 있음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  @PostMapping
  ResponseEntity<BackupDto> createBackup(HttpServletRequest request);

  @Operation(
      summary = "데이터 백업 목록 조회",
      description = "백업 이력을 필터링 조건과 커서 기반으로 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 정렬 필드"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  @GetMapping
  ResponseEntity<CursorPageResponse<BackupDto>> getBackupHistories(
      @RequestParam(required = false) String worker,
      @RequestParam(required = false) BackupStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant startedAtFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant startedAtTo,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "startedAt") String sortField,
      @RequestParam(defaultValue = "DESC") String sortDirection
  );
}