package com.team11.hrbank.module.domain.employee.dto;

import com.team11.hrbank.module.domain.employee.EmployeeStatus;

import java.time.LocalDate;

public record EmployeeUpdateRequest (
    String name,
    String email,
    Long departmentId,
    String position,
    LocalDate hireDate,
    EmployeeStatus status,
    String memo
    ){
}
