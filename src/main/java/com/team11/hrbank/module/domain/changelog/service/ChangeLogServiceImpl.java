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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChangeLogServiceImpl implements ChangeLogService{

  private final ChangeLogRepository changeLogRepository;
  private final ChangeLogMapper changeLogMapper;
  private final DiffMapper diffMapper;

  public CursorPageResponse<ChangeLogDto> getAllChangeLogs(String employeeNumber, HistoryType type, String memo, String ipAddress, Instant atFrom, Instant atTo, Long idAfter,
                                                           String cursor, int size, String sortField, String sortDirection) {

    try {
      log.debug("변경 로그 조회 요청 - employeeNumber: {}, type: {}, sortField: {}, sortDirection: {}, cursor: {}, idAfter: {}",
              employeeNumber, type, sortField, sortDirection, cursor, idAfter);

      // 커서 디코딩
      if (cursor != null && !cursor.isEmpty() && idAfter == null) {
        try {
          // 숫자로 된 커서라면 바로 ID로 처리
          if (cursor.matches("\\d+")) {
            idAfter = Long.parseLong(cursor);
            log.debug("숫자 커서: {} -> ID: {}", cursor, idAfter);
          }
          // Base64 인코딩된 형식인지 확인
          else if (cursor.matches("^[A-Za-z0-9+/=]+$")) {
            try {
              String decoded = new String(Base64.getDecoder().decode(cursor));
              idAfter = Long.parseLong(decoded.replace("{\"id\":", "").replace("}", ""));
              log.debug("Base64 커서 디코딩: {} -> ID: {}", cursor, idAfter);
            } catch (Exception e) {
              log.warn("Base64 커서 디코딩 실패: {}", cursor);
            }
          }
        } catch (Exception e) {
          log.warn("커서 디코딩 실패: {}", cursor);
        }
      }

      // 정렬 필드 유효성 검사
      if (!isValidSortField(sortField)) {
        throw new IllegalArgumentException("유효하지 않은 정렬 필드: " + sortField);
      }

      // IP 주소 정렬인 경우 네이티브 쿼리 사용
      if ("ipAddress".equals(sortField)) {
        return getChangeLogsByIpAddress(
                employeeNumber, type, memo, ipAddress,
                atFrom, atTo, idAfter, size, sortDirection);
      }

      String dbField = convertToDbField(sortField);

      // 정렬 방향 설정
      Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
              ? Direction.DESC : Direction.ASC;

      // 페이징 및 정렬 설정
      PageRequest pageRequest = PageRequest.of(0, size+1, Sort.by(direction, dbField));

      Instant cursorAt = null;
      String cursorIpAddress = null;

      if (cursor != null && !cursor.isEmpty()) {
        if ("at".equals(sortField)) {
          try {
            // at 필드 기준 커서인 경우
            cursorAt = Instant.parse(cursor);
            log.debug("at 필드 커서 파싱 성공: {}", cursorAt);
          } catch (Exception e) {
            log.warn("at 필드 커서 파싱 실패: {}", cursor);
          }
        } else if ("ipAddress".equals(sortField)) {
          if (!cursor.matches("\\d+")) {
            cursorIpAddress = cursor;
            log.debug("ipAddress 커서 사용: {}", cursorIpAddress);
          }
        }
      }

      // 전체 개수 먼저 계산 (커서 필터링 없이)
      Specification<ChangeLog> countSpec = ChangeLogSpecification.withFilters(
              employeeNumber, type, memo, ipAddress, atFrom, atTo,
              null, null, null, sortField, sortDirection);

      long totalElements = changeLogRepository.count(countSpec);
      log.debug("전체 데이터 수: {}", totalElements);

      // 조회 Specification 생성
      Specification<ChangeLog> spec;
      boolean isAscending = "asc".equalsIgnoreCase(sortDirection);

      // 페이지를 가져오는 방식 간소화 - Department 서비스의 패턴을 따라서
      if (idAfter != null) {
        // ID 기준 필터링
        spec = ChangeLogSpecification.withFilters(
                employeeNumber, type, memo, ipAddress, atFrom, atTo, idAfter,
                cursorAt, cursorIpAddress, sortField, sortDirection);
        log.debug("ID 기준 필터링으로 조회 - idAfter: {}", idAfter);
      } else {
        // 처음부터 조회 (첫 페이지)
        spec = ChangeLogSpecification.withFilters(
                employeeNumber, type, memo, ipAddress, atFrom, atTo,
                null, null, null, sortField, sortDirection);
        log.debug("첫 페이지 조회 - 필터링 없음");
      }

      // 데이터 조회
      Page<ChangeLog> page = changeLogRepository.findAll(spec, pageRequest);
      List<ChangeLog> content = new ArrayList<>(page.getContent());
      log.debug("조회된 데이터 수: {}", content.size());

      boolean hasNext = content.size() > size;

      // 다음 페이지 있으면 마지막 항목 제거
      if (hasNext && content.size() > 0) {
        content.remove(content.size() - 1);
        log.debug("다음 페이지 존재 - 마지막 항목 제거 후 데이터 수: {}", content.size());
      }

      // 빈 결과 처리
      if (content.isEmpty()) {
        log.debug("조회 결과 없음");
        return CursorPageResponse.of(
                List.of(),
                null,
                null,
                size,
                totalElements,
                false
        );
      }

      List<ChangeLogDto> dtoList = changeLogMapper.toDtoList(content);

      // 커서 값과 nextIdAfter 설정
      String nextCursor = null;
      Long nextIdAfter = null;

      if (!content.isEmpty()) {
        ChangeLog lastItem = content.get(content.size() - 1);
        nextIdAfter = lastItem.getId();
        log.debug("마지막 항목 ID: {}", nextIdAfter);

        // 정렬 필드에 따라 커서 값 설정
        if("at".equals(sortField)){
          if (lastItem.getCreatedAt() != null) {
            nextCursor = lastItem.getCreatedAt().toString();
            log.debug("다음 at 커서: {}", nextCursor);
          } else {
            nextCursor = lastItem.getId().toString();
            log.debug("다음 ID 커서(at null): {}", nextCursor);
          }
        } else if("ipAddress".equals(sortField)){
          // IP 주소 정렬 시에는 항상 ID를 커서로 사용
          nextCursor = lastItem.getId().toString();
          log.debug("다음 ID 커서(ipAddress 정렬): {}", nextCursor);
        } else {
          nextCursor = lastItem.getId().toString();
          log.debug("다음 ID 커서(기본): {}", nextCursor);
        }
      }

      return CursorPageResponse.of(
              dtoList,
              nextCursor,
              nextIdAfter,
              size,
              totalElements,
              hasNext
      );
    } catch (Exception e) {
      log.error("변경 로그 조회 중 오류 발생", e);
      throw e;
    }
  }

  /**
   * IP 주소로 정렬하여 결과 조회
   */
  private CursorPageResponse<ChangeLogDto> getChangeLogsByIpAddress(
          String employeeNumber, HistoryType type, String memo, String ipAddress,
          Instant atFrom, Instant atTo, Long idAfter, int size, String sortDirection) {

    // HistoryType enum을 String으로 변환
    String typeStr = type != null ? type.name() : null;

    // 패턴 문자열 구성
    String employeeNumberPattern = employeeNumber != null ? "%" + employeeNumber + "%" : "%";
    String memoPattern = memo != null ? "%" + memo + "%" : "%";
    String ipAddressPattern = ipAddress != null ? "%" + ipAddress + "%" : "%";

    // 총 개수 계산
    long totalElements = changeLogRepository.countByFilters(
            typeStr, atFrom, atTo,
            employeeNumber, employeeNumberPattern,
            memo, memoPattern,
            ipAddress ,ipAddressPattern);

    // 데이터 조회
    List<ChangeLog> content;
    if ("desc".equalsIgnoreCase(sortDirection)) {
      content = changeLogRepository.findByIpAddressDescWithCursor(
              typeStr, atFrom, atTo, idAfter,
              employeeNumber, employeeNumberPattern,
              memo, memoPattern,
              ipAddress, ipAddressPattern,
              size + 1);
    } else {
      content = changeLogRepository.findByIpAddressAscWithCursor(
              typeStr, atFrom, atTo, idAfter,
              employeeNumber, employeeNumberPattern,
              memo, memoPattern,
              ipAddress, ipAddressPattern,
              size + 1);
    }


    boolean hasNext = content.size() > size;

    // 다음 페이지 있으면 마지막 항목 제거
    if (hasNext && !content.isEmpty()) {
      content.remove(content.size() - 1);
    }

    // 빈 결과 처리
    if (content.isEmpty()) {
      return CursorPageResponse.of(
              List.of(),
              null,
              null,
              size,
              totalElements,
              false
      );
    }

    List<ChangeLogDto> dtoList = changeLogMapper.toDtoList(content);

    // 커서 값 설정
    String nextCursor = null;
    Long nextIdAfter = null;

    if (!content.isEmpty()) {
      ChangeLog lastItem = content.get(content.size() - 1);
      nextIdAfter = lastItem.getId();
      nextCursor = lastItem.getId().toString();
    }

    return CursorPageResponse.of(
            dtoList,
            nextCursor,
            nextIdAfter,
            size,
            totalElements,
            hasNext
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
