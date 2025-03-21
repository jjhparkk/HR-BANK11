package com.team11.hrbank.module.domain.backup.repository;


import com.team11.hrbank.module.domain.backup.BackupHistory;
import com.team11.hrbank.module.domain.backup.BackupStatus;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

/**
 * 백업 이력 조회용
 */
public class BackupSpecifications {

  /**
   * 작업자
   */
  public static Specification<BackupHistory> withWorker(String worker) {
    return (root, query, cb) ->
        worker == null ? cb.conjunction() : cb.like(root.get("worker"), "%" + worker + "%");
  }

  /**
   * 상태
   */
  public static Specification<BackupHistory> withStatus(BackupStatus status) {
    return (root, query, cb) ->
        status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
  }

  /**
   * 시작일시 이후
   */
  public static Specification<BackupHistory> withStartDateFrom(Instant startedAtFrom) {
    return (root, query, cb) ->
        startedAtFrom == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("startAt"), startedAtFrom);
  }

  /**
   * 시작일시 이전
   */
  public static Specification<BackupHistory> withStartDateTo(Instant startedAtTo) {
    return (root, query, cb) ->
        startedAtTo == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("startAt"), startedAtTo);
  }

  /**
   * ID 이후-커서 페이징
   */
  public static Specification<BackupHistory> withIdAfter(Long idAfter) {
    return (root, query, cb) ->
        idAfter == null ? cb.conjunction() : cb.greaterThan(root.get("id"), idAfter);
  }

  public static Specification<BackupHistory> withCriteria(
      String worker, BackupStatus status,
      Instant startedAtFrom, Instant startedAtTo, Long idAfter) {

    return Specification.where(withWorker(worker))
        .and(withStatus(status))
        .and(withStartDateFrom(startedAtFrom))
        .and(withStartDateTo(startedAtTo))
        .and(withIdAfter(idAfter));
  }
}
