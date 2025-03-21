package com.team11.hrbank.module.domain.employee.dto;

public record EmployeeTrendDto (
    String date,
    Long count,
    Long change,
    double changeRate
){
}
