package com.team11.hrbank.module.domain.employee.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team11.hrbank.module.domain.employee.Employee;
import com.team11.hrbank.module.domain.employee.EmployeeStatus;
import com.team11.hrbank.module.domain.employee.QEmployee;
import com.team11.hrbank.module.domain.employee.dto.EmployeeDistributionDto;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeRepositoryCustomImpl implements EmployeeRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public EmployeeRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
    this.queryFactory = jpaQueryFactory;
  }

  // 직원 목록 조회
  @Override
  public List<Employee> findEmployeesByConditions(String nameOrEmail, String employeeNumber,
      String departmentName, String position, LocalDate hireDateFrom, LocalDate hireDateTo,
      EmployeeStatus status, Long idAfter, String cursor, int size, String sortField,
      String sortDirection) {
    BooleanBuilder builder = new BooleanBuilder();
    QEmployee employee = QEmployee.employee;

    // 조회 조건 적용
    if (nameOrEmail != null) {
      builder.and(employee.name.contains(nameOrEmail))
          .or(employee.email.contains(nameOrEmail));
    }
    if (employeeNumber != null) {
      builder.and(employee.employeeNumber.contains(employeeNumber));
    }
    if (departmentName != null) {
      builder.and(employee.department.name.contains(departmentName));
    }
    if (position != null) {
      builder.and(employee.position.contains(position));
    }
    if (hireDateFrom != null || hireDateTo != null) {
      if (hireDateFrom != null) {
        builder.and(employee.hireDate.goe(hireDateFrom));
      }
      if (hireDateTo != null) {
        builder.and(employee.hireDate.loe(hireDateTo));
      }
    }
    if (status != null) {
      builder.and(employee.status.eq(status));
    }

    // 커서 기반 페이지네이션
    builder.and(buildCursorCondition(idAfter, cursor, sortField, sortDirection, employee));

    // 정렬
    OrderSpecifier<?> orderSpecifier = createOrderSpecifier(sortField, sortDirection, employee);

    return queryFactory
        .selectFrom(employee)
        .where(builder)
        .orderBy(orderSpecifier)
        .limit(size)
        .fetch();
  }

  // 커서 기반 조건 생성
  private BooleanExpression buildCursorCondition(
      Long idAfter,
      String cursor,
      String sortField,
      String sortDirection,
      QEmployee employee) {

    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
    BooleanExpression cursorCondition;

    if (cursor != null) {
      switch (sortField) {
        case "name":
          cursorCondition = isAsc ? employee.name.gt(cursor) : employee.name.lt(cursor);
          break;
        case "employeeNumber":
          cursorCondition =
              isAsc ? employee.employeeNumber.gt(cursor) : employee.employeeNumber.lt(cursor);
          break;
        case "hireDate":
          LocalDate hireDate = LocalDate.parse(cursor);
          cursorCondition = isAsc ? employee.hireDate.gt(hireDate) : employee.hireDate.lt(hireDate);
          break;
        default:
          throw new IllegalArgumentException("sortField(" + sortField + ")는 존재하지 않습니다.");
      }
    } else if (idAfter != null) {
      Employee targetEmployee = queryFactory.selectFrom(employee)
          .where(employee.id.eq(idAfter))
          .fetchOne();

      if (targetEmployee == null) {
        throw new IllegalArgumentException("해당 idAfter에 해당하는 Employee를 찾을 수 없습니다.");
      }

      // 해당 Employee의 필드를 기준으로 비교
      switch (sortField) {
        case "name":
          // idAfter의 Employee의 name을 기준으로 비교
          cursorCondition = isAsc ? employee.name.gt(targetEmployee.getName())
              : employee.name.lt(targetEmployee.getName());
          break;
        case "employeeNumber":
          cursorCondition = isAsc ? employee.employeeNumber.gt(targetEmployee.getEmployeeNumber())
              : employee.employeeNumber.lt(targetEmployee.getEmployeeNumber());
          break;
        case "hireDate":
          LocalDate hireDate = targetEmployee.getHireDate();
          cursorCondition =
              isAsc ? employee.hireDate.gt(hireDate) : employee.hireDate.lt(hireDate);
          break;
        default:
          throw new IllegalArgumentException("sortField(" + sortField + ")는 존재하지 않습니다.");
      }

    } else {
      // 첫 페이지의 경우 (cursor와 idAfter가 없을 때)
      cursorCondition = employee.id.gt(0L);  // id > 0 으로 설정
    }

    return cursorCondition;
  }

  private OrderSpecifier<?> createOrderSpecifier(String sortField, String sortDirection,
      QEmployee employee) {
    Order direction = "asc".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

    switch (sortField) {
      case "name":
        // name 필드 기준 정렬
        return new OrderSpecifier<>(direction, employee.name);
      case "employeeNumber":
        // employeeNumber 필드 기준 정렬
        return new OrderSpecifier<>(direction, employee.employeeNumber);
      case "hireDate":
        // name 필드 기준 정렬
        return new OrderSpecifier<>(direction, employee.hireDate);
      default:
        return new OrderSpecifier<>(direction, employee.name);
    }
  }


  @Override
  public List<EmployeeDistributionDto> findEmployeeDistribution(String groupBy,
      EmployeeStatus status) {
    QEmployee employee = QEmployee.employee;

    // 조건에 따른 필터링
    BooleanBuilder builder = new BooleanBuilder();
    if (status != null) {
      builder.and(employee.status.eq(status));
    }

    // 전체 직원 수
    Long totalCount = queryFactory
        .select(employee.count())
        .from(employee)
        .where(builder)
        .fetchOne();

    if (totalCount == null || totalCount == 0L) {
      // TODO 총 직원이 0명일 때, 어떤 처리를 할지
      // 현재는 빈 리스트를 반환하는 것으로 해두었습니다.
      return Collections.emptyList();
    }

    // 필터링과 그룹화를 통한 반환
    // Projections.constructor은 생성자를 통해 DTO로 접근할 수 있습니다.
    return queryFactory.select(
            Projections.constructor(EmployeeDistributionDto.class,
                groupBy.equals("department") ? employee.department.name : employee.position,
                employee.count(),
                employee.count().multiply(100.0).divide(totalCount).doubleValue()
            )
        )
        .from(employee)
        .where(builder)
        .groupBy(groupBy.equals("department") ? employee.department : employee.position)
        .fetch();
  }

  // 직원 수 조회
  @Override
  public long countByStatusAndHireDateBetween(EmployeeStatus status, LocalDate fromData,
      LocalDate toDate) {

    QEmployee employee = QEmployee.employee;

    // 동적 쿼리 조건 구성
    BooleanBuilder builder = new BooleanBuilder();

    // status 조건 추가
    if (status != null) {
      builder.and(employee.status.eq(status));
    }

    //입사일 범위 조건 추가
    if (fromData != null || toDate != null) { // 둘 중 하나라도 있다면 입사일 조건 추가
      if (fromData != null) {
        builder.and(employee.hireDate.goe(fromData)); // 입사일이 fromDate 이후
      }
      if (toDate != null) {
        builder.and(employee.hireDate.loe(toDate)); // 입사일이 toDate 이전
      }
    }

    // 직원 수 카운트 쿼리
    Long count = queryFactory
        .select(employee.count())
        .from(employee)
        .where(builder)
        .fetchOne();

    return count != null ? count : 0L; // null일 경우 0 반환
  }
}
