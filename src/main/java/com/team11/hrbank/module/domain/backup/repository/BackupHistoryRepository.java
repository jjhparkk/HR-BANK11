package com.team11.hrbank.module.domain.backup.repository;

import com.team11.hrbank.module.domain.backup.BackupHistory;
import com.team11.hrbank.module.domain.backup.BackupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

/**
 * 백업 이력을 조회하는 Repository 인터페이스.
 * 기본 CRUD 기능은 JpaRepository가 제공,
 * Specification 기반 동적 쿼리를 사용하기 위해 JpaSpecificationExecutor를 상속.
 */
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long>, JpaSpecificationExecutor<BackupHistory> {

    /**
     * 가장 최근 완료된 백업의 시작 시간을 조회하는 메서드.
     * 상태가 COMPLETED인 백업 중 가장 최근의 startAt 값을 반환합니다.
     *
     * @return 가장 최근 완료된 백업의 시작 시간(LocalDateTime)
     */
    @Query("SELECT MAX(b.startAt) FROM BackupHistory b WHERE b.status = 'COMPLETED'")
    Instant findLatestCompletedBackupTime();

    /**
     * 현재 진행 중인 백업의 개수를 조회하는 메서드.
     * @return 진행 중인 백업의 개수(Long)
     */
    @Query("SELECT COUNT(b) FROM BackupHistory b WHERE b.status = 'IN_PROGRESS'")
    Long countInProgressBackups();

    /**
     * 조건에 맞는 상태의 최신 백업 조회
     */
    Optional<BackupHistory> findTopByStatusOrderByStartAtDesc(BackupStatus status);
}
