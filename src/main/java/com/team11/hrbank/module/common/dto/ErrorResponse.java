package com.team11.hrbank.module.common.dto;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String message,
    String details
) {
  public static ErrorResponse of(int status, String message, String details) {
    return new ErrorResponse(Instant.now(), status, message, details);
  }
}
