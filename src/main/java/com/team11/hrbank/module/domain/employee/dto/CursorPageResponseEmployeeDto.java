package com.team11.hrbank.module.domain.employee.dto;

import java.util.List;

public record CursorPageResponseEmployeeDto(
    List<EmployeeDto> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

}