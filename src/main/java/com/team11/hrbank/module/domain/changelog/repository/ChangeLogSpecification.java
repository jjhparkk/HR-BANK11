package com.team11.hrbank.module.domain.changelog.repository;

import com.team11.hrbank.module.domain.changelog.ChangeLog;
import com.team11.hrbank.module.domain.changelog.HistoryType;
import jakarta.persistence.criteria.Predicate;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ChangeLogSpecification {

  public static Specification<ChangeLog> withFilters(
      String employeeNumber,
      HistoryType type,
      String memo,
      InetAddress ipAddress,
      Instant fromDate,
      Instant toDate,
      Long idAfter) {

    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (employeeNumber != null && !employeeNumber.isEmpty()) {
        predicates.add(criteriaBuilder.like(root.get("employeeNumber"), "%" + employeeNumber + "%"));
      }

      if (type != null) {
        predicates.add(criteriaBuilder.equal(root.get("type"), type));
      }

      if (memo != null && !memo.isEmpty()) {
        predicates.add(criteriaBuilder.like(root.get("memo"), "%" + memo + "%"));
      }

      if (ipAddress != null) {
        predicates.add(criteriaBuilder.equal(root.get("ipAddress"), ipAddress));
      }

      if (fromDate != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
      }

      if (toDate != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
      }

      if (idAfter != null) {
        predicates.add(criteriaBuilder.lessThan(root.get("id"), idAfter));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}