package com.team11.hrbank.module.domain.changelog.repository;

import com.team11.hrbank.module.domain.changelog.ChangeLog;
import com.team11.hrbank.module.domain.changelog.HistoryType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ChangeLogSpecification {

  public static Specification<ChangeLog> withFilters(
          String employeeNumber,
          HistoryType type,
          String memo,
          String ipAddress,
          Instant fromDate,
          Instant toDate,
          Long idAfter,
          Instant cursorAt,
          String cursorIpAddress,
          String sortField,
          String sortDirection) {

    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 기본 필터 조건 추가
      if (employeeNumber != null && !employeeNumber.isEmpty()) {
        predicates.add(criteriaBuilder.like(root.get("employeeNumber"), "%" + employeeNumber + "%"));
      }

      if (type != null) {
        predicates.add(criteriaBuilder.equal(root.get("type"), type));
      }

      if (memo != null && !memo.isEmpty()) {
        predicates.add(criteriaBuilder.like(root.get("memo"), "%" + memo + "%"));
      }

      if (ipAddress != null && !ipAddress.isEmpty()) {
        predicates.add(criteriaBuilder.like(root.get("ipAddress"), "%" + ipAddress + "%"));
      }

      if (fromDate != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
      }

      if (toDate != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
      }

      boolean isAscending = "asc".equalsIgnoreCase(sortDirection);

      if (idAfter != null) {
        // at 필드 정렬 + cursorAt이 있는 경우
        if ("at".equals(sortField) && cursorAt != null) {
          if (isAscending) {
            // 오름차순: cursorAt보다 큰 값 (또는 같은 경우 ID가 더 큰 값)
            Predicate greaterThan = criteriaBuilder.greaterThan(root.get("createdAt"), cursorAt);
            Predicate equalAtGreaterId = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("createdAt"), cursorAt),
                    criteriaBuilder.greaterThan(root.get("id"), idAfter)
            );
            predicates.add(criteriaBuilder.or(greaterThan, equalAtGreaterId));

            log.debug("오름차순 at 필터링 적용");
          } else {
            // 내림차순: cursorAt보다 작은 값 (또는 같은 경우 ID가 더 작은 값)
            Predicate lessThan = criteriaBuilder.lessThan(root.get("createdAt"), cursorAt);
            Predicate equalAtLesserId = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("createdAt"), cursorAt),
                    criteriaBuilder.lessThan(root.get("id"), idAfter)
            );
            predicates.add(criteriaBuilder.or(lessThan, equalAtLesserId));

            log.debug("내림차순 at 필터링 적용");
          }
        }
        // ipAddress 필드 정렬 시에는 항상 ID 기준으로 필터링
        else if ("ipAddress".equals(sortField)) {
          if (isAscending) {
            // 오름차순: ID가 더 큰 값
            predicates.add(criteriaBuilder.greaterThan(root.get("id"), idAfter));
            log.debug("오름차순 ID 필터링 적용 (ipAddress 정렬)");
          } else {
            // 내림차순: ID가 더 작은 값
            predicates.add(criteriaBuilder.lessThan(root.get("id"), idAfter));
            log.debug("내림차순 ID 필터링 적용 (ipAddress 정렬)");
          }
        }
        // 그 외 필드 정렬 또는 cursorAt이 없는 경우
        else {
          if (isAscending) {
            predicates.add(criteriaBuilder.greaterThan(root.get("id"), idAfter));
            log.debug("오름차순 ID 필터링 적용 (기본)");
          } else {
            predicates.add(criteriaBuilder.lessThan(root.get("id"), idAfter));
            log.debug("내림차순 ID 필터링 적용 (기본)");
          }
        }
      } else {
        log.debug("커서 필터링 없음 (첫 페이지)");
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}