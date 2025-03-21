package com.team11.hrbank.module.domain.changelog.repository;

import com.team11.hrbank.module.domain.changelog.ChangeLog;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
