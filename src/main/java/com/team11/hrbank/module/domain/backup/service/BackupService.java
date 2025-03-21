package com.team11.hrbank.module.domain.backup.service;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.backup.BackupHistory;
import com.team11.hrbank.module.domain.backup.BackupStatus;
import com.team11.hrbank.module.domain.backup.dto.BackupDto;
import com.team11.hrbank.module.domain.backup.mapper.BackupMapper;
import com.team11.hrbank.module.domain.backup.repository.BackupHistoryRepository;
import com.team11.hrbank.module.domain.backup.repository.BackupSpecifications;
import com.team11.hrbank.module.domain.backup.service.data.BackupDataService;
import com.team11.hrbank.module.domain.backup.service.file.BackupFileStorageService;
import com.team11.hrbank.module.domain.file.File;
import com.team11.hrbank.module.domain.file.service.FileService;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final BackupHistoryRepository backupHistoryRepository;
    private final BackupFileStorageService fileStorageService;
    private final BackupDataService backupDataService;
    private final FileService fileService;
    private final BackupMapper backupMapper;
    private final BackupTransactionService backupTxService; //트랜잭션 관련 로직

    /**
     * 백업을 실행하고 결과를 반환
     *
     * @param workerIp 작업자 ip 또는 system
     * @return 생성된 백업 이력
     */
    public BackupHistory performBackup(String workerIp) {
        // 1. 진행 중인 백업 확인
        if (backupTxService.isBackupInProgress()) {
            throw new IllegalStateException("이미 진행 중인 백업이 존재합니다.");
        }

        log.info("백업 실행 요청 받음 - 요청자 IP: {}", workerIp);

        // 2. 변경 사항 확인
        boolean isChanged = backupTxService.checkIfChangesExist();

        // 3. 변경 사항 없는 경우 skip
        if (!isChanged) {
            BackupHistory skippedHistory = backupTxService.saveBackupHistory(workerIp, BackupStatus.SKIPPED, null);
            log.info("백업 불필요 - SKIPPED 상태로 저장, ID: {}", skippedHistory.getId());
            return skippedHistory;
        }

        // 4. 백업 시작
        BackupHistory backupHistory = backupTxService.saveBackupHistory(workerIp, BackupStatus.IN_PROGRESS, null);
        log.info("백업 시작 - 이력 ID: {}", backupHistory.getId());

        File backupFile = null;
        String backupFilePath = null;
        try {
            // 5. 백업 파일 생성 (트랜잭션 외부 작업)
            backupFilePath = fileStorageService.saveBackupToCsv(backupDataService.getAllDataForBackup());
            log.info("백업 파일 생성 완료: {}", backupFilePath);

            // 6. 파일 엔티티 생성
            backupFile = backupTxService.createFileEntity(backupFilePath);

            // 7. 백업 완료 처리
            BackupHistory updatedHistory = backupTxService.updateBackupStatus(
                backupHistory.getId(), BackupStatus.COMPLETED, backupFile);
            log.info("백업 완료 - 저장된 파일: {}", backupFilePath);

            return updatedHistory;
        } catch (IOException e) {
            log.error("백업 실패", e);

            // 8. 실패 처리
            handleBackupFailure(backupHistory.getId(), backupFile, backupFilePath, e);

            throw new RuntimeException("백업 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 백업 실패 처리
     */
    private void handleBackupFailure(Long backupId, File backupFile, String backupFilePath, Exception error) {
        // 1. 이미 생성된 파일 정리
        if (backupFile != null) {
            try {
                fileService.deleteFile(backupFile);
            } catch (Exception e) {
                log.error("백업 파일 삭제 실패: {}", e.getMessage(), e);
            }
        } else if (backupFilePath != null) {
            try {
                fileService.deleteActualFile(backupFilePath);
            } catch (Exception e) {
                log.error("물리적 백업 파일 삭제 실패: {}", e.getMessage(), e);
            }
        }

        // 2. 에러 로그 저장 및 백업 상태 업데이트
        try {
            // 에러 로그 저장 (파일 시스템 작업)
            String errorLogPath = fileStorageService.saveErrorLog(error);

            // 에러 로그 파일 엔티티 생성
            File errorLogFile = backupTxService.createFileEntity(errorLogPath);

            // 백업 이력 실패 업데이트
            backupTxService.updateBackupStatus(backupId, BackupStatus.FAILED, errorLogFile);
        } catch (Exception e) {
            log.error("백업 실패 처리 중 오류: {}", e.getMessage(), e);

            // 최소한의 상태 업데이트 시도
            try {
                backupTxService.updateBackupStatusWithoutFile(backupId, BackupStatus.FAILED);
            } catch (Exception ex) {
                log.error("백업 상태 업데이트 최종 실패", ex);
            }
        }
    }

    /**
     * 지정된 상태의 최근 백업 조회
     * @param status 백업 상태
     * @return 가장 최근 백업 이력
     */
    @Transactional(readOnly = true)
    public BackupHistory getLatestBackupByStatus(BackupStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("backupStatus can not be null");
        }

        return backupHistoryRepository.findTopByStatusOrderByStartAtDesc(status)
            .orElseThrow(() -> new RuntimeException("해당 상태의 백업이 존재하지 않습니다: " + status));
    }

    /**
     * 커서기반 페이징으로 백업 이력 조회
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<BackupDto> getBackupHistoriesWithCursor(
        String worker, BackupStatus status,
        Instant startedAtFrom, Instant startedAtTo,
        Long idAfter, String cursor, int size,
        String sortField, String sortDirection) {

        // 커서에서 ID 추출
        if (cursor != null && !cursor.isEmpty() && idAfter == null) {
            idAfter = CursorPageResponse.extractIdFromCursor(cursor);
        }

        // 정렬 필드 매핑
        String entitySortField = mapSortField(sortField);

        // 정렬 방향 설정
        Sort sort = "DESC".equalsIgnoreCase(sortDirection) ?
            Sort.by(entitySortField).descending() :
            Sort.by(entitySortField).ascending();

        // 페이징 설정
        Pageable pageable = PageRequest.of(0, size, sort);

        // 백업 이력 조회
        Page<BackupHistory> backupHistories = getFilteredBackups(
            worker, status, startedAtFrom, startedAtTo, idAfter, pageable);

        // DTO 변환
        List<BackupDto> backupDtos = backupMapper.toDtoList(backupHistories.getContent());

        // 마지막 ID 추출
        Long lastId = backupHistories.getContent().isEmpty() ?
            null : backupHistories.getContent().get(backupHistories.getContent().size() - 1).getId();

        // 커서 페이지 응답 생성
        return CursorPageResponse.of(
            backupDtos,
            lastId,
            size,
            backupHistories.getTotalElements());
    }

    /**
     * 백업 이력 조회 메서드 (페이징 및 필터링 지원)
     */
    @Transactional(readOnly = true)
    public Page<BackupHistory> getFilteredBackups(String worker, BackupStatus status,
        Instant startedAtFrom, Instant startedAtTo,
        Long idAfter, Pageable pageable) {

        return backupHistoryRepository.findAll(
            BackupSpecifications.withCriteria(worker, status, startedAtFrom, startedAtTo, idAfter),
            pageable);
    }

    private String mapSortField(String sortField) {
        if (sortField == null || sortField.equals("startedAt")) {
            return "startAt";
        }
        return sortField;
    }
}