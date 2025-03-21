package com.team11.hrbank.module.domain.department.dto;

import java.time.LocalDate;


public record DepartmentDto(
  Long id, //api 명세에 지정되어있음
  String name,
  String description,
  LocalDate establishedDate, //api 명세서에 지정되어있음
  Long employeeCount
){}
