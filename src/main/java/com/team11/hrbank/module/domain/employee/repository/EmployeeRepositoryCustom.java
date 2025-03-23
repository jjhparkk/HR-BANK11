package com.team11.hrbank.module.domain.employee.repository;

import com.team11.hrbank.module.domain.employee.Employee;
import com.team11.hrbank.module.domain.employee.EmployeeStatus;
import com.team11.hrbank.module.domain.employee.dto.EmployeeDistributionDto;

import java.time.LocalDate;
import java.util.List;


public interface EmployeeRepositoryCustom {


  List<Employee> findEmployeesByConditions(
      String nameOrEmail,
      String employeeNumber,
      String departmentName,
      String position,
      LocalDate hireDateFrom,
      LocalDate hireDateTo,
      EmployeeStatus status,
      Long idAfter,
      String cursor,
      int size,
      String sortField,
      String sortDirection);

  List<EmployeeDistributionDto> findEmployeeDistribution(String groupBy, EmployeeStatus status);

  long countByStatusAndHireDateBetween(
      EmployeeStatus status,
      LocalDate fromData,
      LocalDate toDate
  );

}
