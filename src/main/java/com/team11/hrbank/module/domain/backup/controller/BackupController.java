package com.team11.hrbank.module.domain.backup.controller;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.backup.BackupHistory;
import com.team11.hrbank.module.domain.backup.BackupStatus;
import com.team11.hrbank.module.domain.backup.dto.BackupDto;
import com.team11.hrbank.module.domain.backup.mapper.BackupMapper;
import com.team11.hrbank.module.domain.backup.service.BackupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor

public class BackupController implements BackupApi {

    private final BackupService backupService;
    private final BackupMapper backupMapper;

    /**
     * 최근 백업 정보 조회
     */
    @GetMapping("/latest")
    public ResponseEntity<BackupDto> getLatestBackup(
        @RequestParam(required = false, defaultValue = "COMPLETED") BackupStatus status) {
        BackupHistory latestBackup = backupService.getLatestBackupByStatus(status);

        if (latestBackup == null) {
            // 백업 기록이 없는 경우 빈 객체와 메시지를 반환
            BackupDto emptyResponse = new BackupDto(
                null, null, null, null, BackupStatus.SKIPPED, null
            );

            log.info("백업 기록 없음 - 상태: {}", status);
            return ResponseEntity.ok(emptyResponse); // 200 OK 상태 반환
        }

        return ResponseEntity.ok(backupMapper.toDto(latestBackup));
    }

    @PostMapping
    public ResponseEntity<BackupDto> createBackup(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        log.info("백업 생성 요청: 요청 ip = {}", ipAddress);
        BackupHistory backupHistory = backupService.performBackup(ipAddress);
        return ResponseEntity.ok(backupMapper.toDto(backupHistory));
    }

    /**
     * 백업 이력 조회 API
     * @param worker 작업자 (부분 일치)
     * @param status 백업 상태 (정확한 일치)
     * @param startedAtFrom 시작 시간 범위의 시작 (ISO DATE-TIME 형식)
     * @param startedAtTo 시작 시간 범위의 종료 (ISO DATE-TIME 형식)
     * @param idAfter 이전 페이지 마지막 요소의 ID (커서 기반 페이징)
     * @param size 페이지 크기 (기본값: 10)
     * @param sortField 정렬 필드 (기본값: startAt)
     * @param sortDirection 정렬 방향 (DESC 또는 ASC, 기본값: DESC)
     * @return 조건에 맞게 필터링된 백업 이력 목록을 BackupDto로 변환하여 반환
     */
    @GetMapping
    public ResponseEntity<CursorPageResponse<BackupDto>> getBackupHistories(
        @RequestParam(required = false) String worker,
        @RequestParam(required = false) BackupStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAtFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAtTo,
        @RequestParam(required = false) Long idAfter, // 이전 페이지 마지막 요소 id
        @RequestParam(required = false) String cursor,//커서 (이전 페이지 마지막 id)
        @RequestParam(required = false, defaultValue = "10") int size,
        @RequestParam(required = false, defaultValue = "startAt") String sortField,//startedAt,endedAt,status
        @RequestParam(required = false, defaultValue = "DESC") String sortDirection
    ) {

        CursorPageResponse<BackupDto> response = backupService.getBackupHistoriesWithCursor(
            worker, status, startedAtFrom, startedAtTo, idAfter, cursor, size, sortField, sortDirection);

        return ResponseEntity.ok(response);
    }
}