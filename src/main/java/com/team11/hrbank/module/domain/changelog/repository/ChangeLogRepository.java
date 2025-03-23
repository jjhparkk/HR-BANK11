package com.team11.hrbank.module.domain.changelog.repository;

import com.team11.hrbank.module.domain.changelog.ChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long>,
    JpaSpecificationExecutor<ChangeLog> {
  //fromDate o, toDate o
  @Query("SELECT COUNT(c) FROM ChangeLog c WHERE c.createdAt >= :fromDate AND c.createdAt <= :toDate")
  long countByDateRangeBoth(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

  // fromDate
  @Query("SELECT COUNT(c) FROM ChangeLog c WHERE c.createdAt >= :fromDate")
  long countByDateRangeFrom(@Param("fromDate") Instant fromDate);

  // toDate
  @Query("SELECT COUNT(c) FROM ChangeLog c WHERE c.createdAt <= :toDate")
  long countByDateRangeTo(@Param("toDate") Instant toDate);

  // fromDate x, toDate x
  @Query("SELECT COUNT(c) FROM ChangeLog c")
  long countAll();

  @Query(value = "SELECT * FROM change_logs c " +
          "WHERE (:type IS NULL OR c.type = :type) " +
          "AND (c.created_at >= COALESCE(CAST(:atFrom AS timestamptz), '-infinity'::timestamptz)) " +
          "AND (c.created_at <= COALESCE(CAST(:atTo AS timestamptz), 'infinity'::timestamptz)) " +
          "AND (:idAfter IS NULL OR c.id < :idAfter) " +
          "AND (:employeeNumber IS NULL OR c.employee_number LIKE :employeeNumberPattern) " +
          "AND (:memo IS NULL OR c.memo LIKE :memoPattern) " +
          "AND (:ipAddress IS NULL OR c.ip_address LIKE :ipAddressPattern) " +
          "ORDER BY c.ip_address DESC, c.id DESC " +
          "LIMIT :limit",
          nativeQuery = true)
  List<ChangeLog> findByIpAddressDescWithCursor(
          @Param("type") String type,
          @Param("atFrom") Instant atFrom,
          @Param("atTo") Instant atTo,
          @Param("idAfter") Long idAfter,
          @Param("employeeNumber") String employeeNumber,
          @Param("employeeNumberPattern") String employeeNumberPattern,
          @Param("memo") String memo,
          @Param("memoPattern") String memoPattern,
          @Param("ipAddress") String ipAddress,
          @Param("ipAddressPattern") String ipAddressPattern,
          @Param("limit") int limit);

  @Query(value = "SELECT * FROM change_logs c " +
          "WHERE (:type IS NULL OR c.type = :type) " +
          "AND (c.created_at >= COALESCE(CAST(:atFrom AS timestamptz), '-infinity'::timestamptz)) " +
          "AND (c.created_at <= COALESCE(CAST(:atTo AS timestamptz), 'infinity'::timestamptz)) " +
          "AND (:idAfter IS NULL OR c.id > :idAfter) " +
          "AND (:employeeNumber IS NULL OR c.employee_number LIKE :employeeNumberPattern) " +
          "AND (:memo IS NULL OR c.memo LIKE :memoPattern) " +
          "AND (:ipAddress IS NULL OR c.ip_address LIKE :ipAddressPattern) " +
          "ORDER BY c.ip_address ASC, c.id ASC " +
          "LIMIT :limit",
          nativeQuery = true)
  List<ChangeLog> findByIpAddressAscWithCursor(
          @Param("type") String type,
          @Param("atFrom") Instant atFrom,
          @Param("atTo") Instant atTo,
          @Param("idAfter") Long idAfter,
          @Param("employeeNumber") String employeeNumber,
          @Param("employeeNumberPattern") String employeeNumberPattern,
          @Param("memo") String memo,
          @Param("memoPattern") String memoPattern,
          @Param("ipAddress") String ipAddress,
          @Param("ipAddressPattern") String ipAddressPattern,
          @Param("limit") int limit);

  @Query(value = "SELECT COUNT(*) FROM change_logs c " +
          "WHERE (:type IS NULL OR c.type = :type) " +
          "AND (c.created_at >= COALESCE(CAST(:atFrom AS timestamptz), '-infinity'::timestamptz)) " +
          "AND (c.created_at <= COALESCE(CAST(:atTo AS timestamptz), 'infinity'::timestamptz)) " +
          "AND (:employeeNumber IS NULL OR c.employee_number LIKE :employeeNumberPattern) " +
          "AND (:memo IS NULL OR c.memo LIKE :memoPattern) " +
          "AND (:ipAddress IS NULL OR c.ip_address LIKE :ipAddressPattern)",
          nativeQuery = true)
  long countByFilters(
          @Param("type") String type,
          @Param("atFrom") Instant atFrom,
          @Param("atTo") Instant atTo,
          @Param("employeeNumber") String employeeNumber,
          @Param("employeeNumberPattern") String employeeNumberPattern,
          @Param("memo") String memo,
          @Param("memoPattern") String memoPattern,
          @Param("ipAddress") String ipAddress,
          @Param("ipAddressPattern") String ipAddressPattern);
}
