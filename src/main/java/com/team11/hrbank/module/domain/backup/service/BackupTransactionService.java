package com.team11.hrbank.module.domain.backup.service;

import com.team11.hrbank.module.common.exception.ResourceNotFoundException;
import com.team11.hrbank.module.domain.backup.BackupHistory;
import com.team11.hrbank.module.domain.backup.BackupStatus;
import com.team11.hrbank.module.domain.backup.repository.BackupHistoryRepository;
import com.team11.hrbank.module.domain.changelog.repository.ChangeLogRepository;
import com.team11.hrbank.module.domain.file.File;
import com.team11.hrbank.module.domain.file.service.FileService;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 백업 - 트랜잭션 로직 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BackupTransactionService {

  private final BackupHistoryRepository backupHistoryRepository;
  private final ChangeLogRepository changeLogRepository;
  private final FileService fileService;

  @Transactional(readOnly = true)
  public boolean isBackupInProgress() {
    return backupHistoryRepository.countInProgressBackups() > 0;
  }

  @Transactional(readOnly = true)
  public boolean checkIfChangesExist() {
    Instant lastBackupTime = backupHistoryRepository.findLatestCompletedBackupTime();
    if (lastBackupTime == null) {
      log.info("첫 백업 실행");
      return true;
    } else {
      long changesCount = changeLogRepository.countByDateRangeFrom(lastBackupTime);
      log.info("last backup time: {}, changes count: {}", lastBackupTime, changesCount);
      return changesCount > 0;
    }
  }

  public BackupHistory saveBackupHistory(String worker, BackupStatus status, File file) {
    BackupHistory backupHistory = new BackupHistory();
    backupHistory.setWorker(worker);
    backupHistory.setStartAt(Instant.now());
    backupHistory.setStatus(status);
    backupHistory.setFile(file);
    return backupHistoryRepository.save(backupHistory);
  }

  public BackupHistory updateBackupStatus(Long backupId, BackupStatus status, File file) {
    BackupHistory backupHistory = backupHistoryRepository.findById(backupId)
        .orElseThrow(() -> ResourceNotFoundException.of("BackupHistory", "id", backupId));

    backupHistory.setStatus(status);
    backupHistory.setEndedAt(Instant.now());
    backupHistory.setFile(file);

    return backupHistoryRepository.save(backupHistory);
  }

  public void updateBackupStatusWithoutFile(Long backupId, BackupStatus status) {
    BackupHistory backupHistory = backupHistoryRepository.findById(backupId)
        .orElseThrow(() -> ResourceNotFoundException.of("BackupHistory", "id", backupId));

    backupHistory.setStatus(status);
    backupHistory.setEndedAt(Instant.now());

    backupHistoryRepository.save(backupHistory);
  }

  @Transactional(readOnly = true)
  public BackupHistory getBackupById(Long backupId) {
    return backupHistoryRepository.findById(backupId)
        .orElseThrow(() -> ResourceNotFoundException.of("BackupHistory", "id", backupId));
  }

  public File createFileEntity(String filePath) throws IOException {
    if (filePath == null || filePath.isEmpty()) {
      throw new IllegalArgumentException("파일 경로가 유효하지 않습니다.");
    }

    java.io.File actualFile = new java.io.File(filePath);
    if (!actualFile.exists()) {
      throw new IOException("파일이 존재하지 않습니다: " + filePath);
    }

    File file = new File();
    file.setFileName(Paths.get(filePath).getFileName().toString());
    file.setFilePath(filePath);

    String fileName = file.getFileName();
    int lastDotIndex = fileName.lastIndexOf('.');
    String format = lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toUpperCase() : "";
    file.setFormat(format);

    file.setSize(actualFile.length());
    return fileService.saveFile(file);
  }
}