package com.team11.hrbank.module.domain.backup.repository;


import com.team11.hrbank.module.domain.backup.BackupHistory;
import com.team11.hrbank.module.domain.backup.BackupStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 백업 이력 조회용
 */
public class BackupSpecifications {

  public static Specification<BackupHistory> withCriteria(
          String worker,
          BackupStatus status,
          Instant startedAtFrom,
          Instant startedAtTo,
          Long idAfter,
          Instant cursorTimestamp,
          String sortField,
          String sortDirection) {

    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 기본 필터 조건
      if (worker != null && !worker.isEmpty()) {
        predicates.add(criteriaBuilder.like(root.get("worker"), "%" + worker + "%"));
      }

      if (status != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), status));
      }

      if (startedAtFrom != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startAt"), startedAtFrom));
      }

      if (startedAtTo != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startAt"), startedAtTo));
      }

      // 커서 조건
      boolean isAscending = !"DESC".equalsIgnoreCase(sortDirection);

      // 타임스탬프 커서, ID 커서 동시에 지정
      if (cursorTimestamp != null && idAfter != null) {
        // 타임스탬프 기반 정렬 시 타임스탬프 우선
        if ("startAt".equals(sortField)) {
          if (isAscending) {
            Predicate timeGreaterThan = criteriaBuilder.greaterThan(root.get("startAt"), cursorTimestamp);
            Predicate timeEqualAndIdGreater = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("startAt"), cursorTimestamp),
                    criteriaBuilder.greaterThan(root.get("id"), idAfter)
            );
            predicates.add(criteriaBuilder.or(timeGreaterThan, timeEqualAndIdGreater));
          } else {
            Predicate timeLessThan = criteriaBuilder.lessThan(root.get("startAt"), cursorTimestamp);
            Predicate timeEqualAndIdLess = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("startAt"), cursorTimestamp),
                    criteriaBuilder.lessThan(root.get("id"), idAfter)
            );
            predicates.add(criteriaBuilder.or(timeLessThan, timeEqualAndIdLess));
          }
        } else if ("endedAt".equals(sortField)) {
          if (isAscending) {
            Predicate timeGreaterThan = criteriaBuilder.greaterThan(root.get("endedAt"), cursorTimestamp);
            Predicate timeEqualAndIdGreater = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("endedAt"), cursorTimestamp),
                    criteriaBuilder.greaterThan(root.get("id"), idAfter)
            );
            predicates.add(criteriaBuilder.or(timeGreaterThan, timeEqualAndIdGreater));
          } else {
            Predicate timeLessThan = criteriaBuilder.lessThan(root.get("endedAt"), cursorTimestamp);
            Predicate timeEqualAndIdLess = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("endedAt"), cursorTimestamp),
                    criteriaBuilder.lessThan(root.get("id"), idAfter)
            );
            predicates.add(criteriaBuilder.or(timeLessThan, timeEqualAndIdLess));
          }
        } else {
          // 정렬 필드는 ID만 사용
          if (isAscending) {
            predicates.add(criteriaBuilder.greaterThan(root.get("id"), idAfter));
          } else {
            predicates.add(criteriaBuilder.lessThan(root.get("id"), idAfter));
          }
        }
      } else if (cursorTimestamp != null) {
        // 타임스탬프만
        if ("startAt".equals(sortField)) {
          if (isAscending) {
            predicates.add(criteriaBuilder.greaterThan(root.get("startAt"), cursorTimestamp));
          } else {
            predicates.add(criteriaBuilder.lessThan(root.get("startAt"), cursorTimestamp));
          }
        } else if ("endedAt".equals(sortField)) {
          if (isAscending) {
            predicates.add(criteriaBuilder.greaterThan(root.get("endedAt"), cursorTimestamp));
          } else {
            predicates.add(criteriaBuilder.lessThan(root.get("endedAt"), cursorTimestamp));
          }
        }
      } else if (idAfter != null) {
        // ID만
        if (isAscending) {
          predicates.add(criteriaBuilder.greaterThan(root.get("id"), idAfter));
        } else {
          predicates.add(criteriaBuilder.lessThan(root.get("id"), idAfter));
        }
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
