package com.team11.hrbank.module.domain.changelog.service;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.common.exception.ResourceNotFoundException;
import com.team11.hrbank.module.domain.changelog.ChangeLog;
import com.team11.hrbank.module.domain.changelog.HistoryType;
import com.team11.hrbank.module.domain.changelog.dto.ChangeLogDto;
import com.team11.hrbank.module.domain.changelog.dto.DiffDto;
import com.team11.hrbank.module.domain.changelog.mapper.ChangeLogMapper;
import com.team11.hrbank.module.domain.changelog.mapper.DiffMapper;
import com.team11.hrbank.module.domain.changelog.repository.ChangeLogRepository;
import com.team11.hrbank.module.domain.changelog.repository.ChangeLogSpecification;
import java.net.InetAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChangeLogServiceImpl implements ChangeLogService{

  private final ChangeLogRepository changeLogRepository;
  private final ChangeLogMapper changeLogMapper;
  private final DiffMapper diffMapper;


  public CursorPageResponse<ChangeLogDto> getAllChangeLogs(String employeeNumber, HistoryType type, String memo, InetAddress ipAddress, Instant atFrom, Instant atTo, Long idAfter,
      String cursor, int size, String sortField, String sortDirection) {

    //커서 디코딩
    if (cursor != null && !cursor.isEmpty() && idAfter == null) {
      String decoded = new String(Base64.getDecoder().decode(cursor));
      idAfter = Long.parseLong(decoded.replace("{\"id\":", "").replace("}", ""));
    }

    //정렬 필드 유효성 검사
    if (!isValidSortField(sortField)) {
      throw new IllegalArgumentException("Invalid sort field: " + sortField);
    }

    String dbField = convertToDbField(sortField);

    //정렬 방향 설정
    Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
        ? Direction.DESC : Direction.ASC;

    //페이징 및 정렬 설정
    PageRequest pageRequest = PageRequest.of(0, size, Sort.by(direction, dbField));

    // Specification 사용
    Specification<ChangeLog> spec = ChangeLogSpecification.withFilters(
        employeeNumber, type, memo, ipAddress, atFrom, atTo, idAfter);

    Page<ChangeLog> page = changeLogRepository.findAll(spec, pageRequest);

    // 응답 생성
    List<ChangeLog> content = page.getContent();
    List<ChangeLogDto> dtoList = changeLogMapper.toDtoList(content);

    // 마지막 요소 ID 추출
    Long lastId = !content.isEmpty() ? content.get(content.size() - 1).getId() : null;

    return CursorPageResponse.of(
        dtoList,
        lastId,
        size,
        page.getTotalElements()
    );
  }

  public List<DiffDto> getChangeLogDiffs(Long id) {

    ChangeLog changeLog = changeLogRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("ChangeLog", "id", id));

    if (changeLog.getChangeLogDiff() == null) {
      return List.of();
    }

    return diffMapper.toDtoList(changeLog.getChangeLogDiff().getChanges());
  }

  public long getChangeLogsCount(Instant fromDate, Instant toDate) {
    //시작 일시 (기본값: 7일 전)
    Instant from = fromDate != null ? fromDate : Instant.now().minus(7, ChronoUnit.DAYS);
    Instant to = toDate != null ? toDate : Instant.now();

    //기간 유효성 검증
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("fromDate must be before toDate");
    }

    if (fromDate != null && toDate != null) {
      return changeLogRepository.countByDateRangeBoth(from, to);
    } else if (fromDate != null) {
      return changeLogRepository.countByDateRangeFrom(from);
    } else if (toDate != null) {
      return changeLogRepository.countByDateRangeTo(to);
    } else {
      return changeLogRepository.countAll();
    }
  }

  private boolean isValidSortField(String field) {
    return field != null && (field.equals("ipAddress") || field.equals("at"));
  }

  private String convertToDbField(String sortField) {
    if ("at".equals(sortField)) {
      return "createdAt";
    }

    return sortField;
  }
}
