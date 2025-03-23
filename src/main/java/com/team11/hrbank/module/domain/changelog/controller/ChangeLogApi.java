package com.team11.hrbank.module.domain.changelog.controller;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.changelog.HistoryType;
import com.team11.hrbank.module.domain.changelog.dto.ChangeLogDto;
import com.team11.hrbank.module.domain.changelog.dto.DiffDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;

@RequestMapping("/api/change-logs")
@Tag(name = "직원 정보 수정 이력 관리", description = "직원 정보 수정 이력 관리 API")
public interface ChangeLogApi {

  @Operation(
      summary = "직원 정보 수정 이력 목록 조회",
      description = "직원 정보 수정 이력 목록을 조회합니다. 상세 변경 내용은 포함되지 않습니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  @GetMapping
  ResponseEntity<CursorPageResponse<ChangeLogDto>> getAllChangeLogs(
      @Parameter(description = "대상 직원 사번") @RequestParam(required = false) String employeeNumber,
      @Parameter(description = "이력 유형 (CREATED, UPDATED, DELETED)") @RequestParam(required = false) HistoryType type,
      @Parameter(description = "내용 메모") @RequestParam(required = false) String memo,
      @Parameter(description = "IP 주소") @RequestParam(required = false) String ipAddress,
      @Parameter(description = "수정 일시 시작") @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant atFrom,
      @Parameter(description = "수정 일시 종료") @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant atTo,
      @Parameter(description = "이전 페이지 마지막 요소 ID") @RequestParam(required = false) Long idAfter,
      @Parameter(description = "커서 (이전 페이지의 마지막 ID)") @RequestParam(required = false) String cursor,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "정렬 필드 (ipAddress, at)") @RequestParam(defaultValue = "at") String sortField,
      @Parameter(description = "정렬 방향 (asc, desc)") @RequestParam(defaultValue = "desc") String sortDirection
  ) throws UnknownHostException;

  @Operation(
      summary = "직원 정보 수정 이력 상세 조회",
      description = "직원 정보 수정 이력의 상세 정보를 조회합니다. 변경 상세 내용이 포함됩니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "404", description = "이력을 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  @GetMapping("/{id}/diffs")
  ResponseEntity<List<DiffDto>> getChangeLogDiffs(
      @Parameter(description = "이력 ID") @PathVariable Long id
  );

  @Operation(
      summary = "수정 이력 건수 조회",
      description = "직원 정보 수정 이력 건수를 조회합니다. 파라미터를 제공하지 않으면 최근 일주일 데이터를 반환합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효하지 않은 날짜 범위"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  @GetMapping("/count")
  ResponseEntity<Long> getChangeLogsCount(
      @Parameter(description = "시작 일시") @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant fromDate,
      @Parameter(description = "종료 일시") @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant toDate
  );
}