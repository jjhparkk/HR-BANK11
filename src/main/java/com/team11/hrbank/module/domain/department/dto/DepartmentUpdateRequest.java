package com.team11.hrbank.module.domain.department.dto;

import java.time.LocalDate;

public record DepartmentUpdateRequest(
  String name,
  String description,
  LocalDate establishedDate
){}