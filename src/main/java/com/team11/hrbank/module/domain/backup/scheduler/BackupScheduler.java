package com.team11.hrbank.module.domain.backup.scheduler;

import com.team11.hrbank.module.domain.backup.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 백업을 주기적으로 실행하는 스케줄러
 */
@Component
@RequiredArgsConstructor
public class BackupScheduler {
    private final BackupService backupService;

    @Scheduled(cron = "${backup.schedule.cron:0 0 * * * *}") // 매 시간 정각 실행
    public void scheduledBackup() {
        backupService.performBackup("system");
    }
}
