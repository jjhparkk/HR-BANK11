package com.team11.hrbank.module.domain.department.dto;

import java.time.LocalDate;

public record DepartmentCreateRequest(
    String name,
    LocalDate establishedDate,
    String description
) {}
