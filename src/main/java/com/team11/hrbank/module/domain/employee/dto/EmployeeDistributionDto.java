package com.team11.hrbank.module.domain.employee.dto;

public record EmployeeDistributionDto(
    String groupKey,
    Long count,
    double percentage
) {

}
