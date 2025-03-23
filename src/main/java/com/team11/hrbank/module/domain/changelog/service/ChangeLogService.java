package com.team11.hrbank.module.domain.changelog.service;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.changelog.HistoryType;
import com.team11.hrbank.module.domain.changelog.dto.ChangeLogDto;
import com.team11.hrbank.module.domain.changelog.dto.DiffDto;

import java.time.Instant;
import java.util.List;

public interface ChangeLogService {
  CursorPageResponse<ChangeLogDto> getAllChangeLogs(
      String employeeNumber, HistoryType type, String memo,
      String ipAddress, Instant atFrom, Instant atTo,
      Long idAfter, String cursor, int size,
      String sortField, String sortDirection);

  List<DiffDto> getChangeLogDiffs(Long id);

  long getChangeLogsCount(Instant fromDate, Instant toDate);
}
