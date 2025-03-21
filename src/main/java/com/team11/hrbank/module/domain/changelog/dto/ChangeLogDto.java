package com.team11.hrbank.module.domain.changelog.dto;

import java.time.Instant;

public record ChangeLogDto(
    Long id,
    String type,
    String employeeNumber,
    String memo,
    String ipAddress,
    Instant at
) {
}
