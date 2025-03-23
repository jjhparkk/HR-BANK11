package com.team11.hrbank.module.domain.changelog.dto;

public record DiffDto(
    String propertyName,
    String before,
    String after
) {
}
