package com.team11.hrbank.module.domain.changelog.controller;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.changelog.HistoryType;
import com.team11.hrbank.module.domain.changelog.dto.ChangeLogDto;
import com.team11.hrbank.module.domain.changelog.dto.DiffDto;
import com.team11.hrbank.module.domain.changelog.service.ChangeLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
@Tag(name = "Changelog Management", description = "직원 정보 변경 이력 관리 API")
public class ChangeLogController {

  private final ChangeLogService changeLogService;

  @GetMapping//직원 정보 수정 이력 목록 조회. 상세 변경 내용은 포함 x
  public ResponseEntity<CursorPageResponse<ChangeLogDto>> getAllChangeLogs(
      @RequestParam(required = false) String employeeNumber,
      @RequestParam(required = false) HistoryType type,
      @RequestParam(required = false) String memo,
      @RequestParam(required = false) String ipAddress,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant atFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant atTo,
      @RequestParam(required = false) Long idAfter, //이전 페이지 마지막 요소 id
      @RequestParam(required = false) String cursor, // 이전 페이지의 마지막 id
      @RequestParam(defaultValue = "10") int size, //페이지 크기
      @RequestParam(defaultValue = "at") String sortField, //정렬 필드(ipAddress, at)
      @RequestParam(defaultValue = "desc") String sortDirection) //정렬 방향 (asc, desc)
      throws UnknownHostException {

    InetAddress inetAddress = ipAddress != null ? InetAddress.getByName(ipAddress) : null;

      CursorPageResponse<ChangeLogDto> response = changeLogService.getAllChangeLogs(
          employeeNumber,
          type, memo, inetAddress, atFrom, atTo,
          idAfter, cursor, size, sortField, sortDirection);

      return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/diffs") // 직원 정보 수정이력 상세 조회
  public ResponseEntity<List<DiffDto>> getChangeLogDiffs(@PathVariable Long id) {
      List<DiffDto> diffs = changeLogService.getChangeLogDiffs(id);
      return ResponseEntity.ok(diffs);
  }

  @GetMapping("/count") //수정 이력 건수
  public ResponseEntity<Long> getChangeLogsCount(
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant toDate) {


    long changeLogsCount = changeLogService.getChangeLogsCount(fromDate, toDate);
    return ResponseEntity.ok(changeLogsCount);
  }
}
