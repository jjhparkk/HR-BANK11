package com.team11.hrbank.module.common.exception;

import com.team11.hrbank.module.common.dto.ErrorResponse;
import com.team11.hrbank.module.domain.backup.exception.BackupAlreadyInProgressException;
import com.team11.hrbank.module.domain.backup.exception.BackupFailedException;
import com.team11.hrbank.module.domain.backup.exception.BackupFileSaveFailedException;
import com.team11.hrbank.module.domain.file.exception.FileDeleteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.UnknownHostException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException e) {
    log.info("Resource not found exception: {}", e.getMessage());
    ErrorResponse errorResponse = ErrorResponse.of(
        HttpStatus.NOT_FOUND.value(),
        "Resource not found",
        e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    log.info("IllegalArgumentException: {}", e.getMessage());

    ErrorResponse errorResponse = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        "Invalid request parameters",
        e.getMessage()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnknownHostException.class)
  public ResponseEntity<ErrorResponse> handleUnknownHostException(UnknownHostException e) {
    log.info("Invalid ipAddress: {}", e.getMessage());
    ErrorResponse errorResponse = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        "Invalid IP address",
        e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BackupAlreadyInProgressException.class)
  public ResponseEntity<ErrorResponse> handleBackupAlreadyInProgressException(
      BackupAlreadyInProgressException e) {
    log.warn("이미 진행중인 백업이 존재하는 예외: {}", e.getMessage());
    ErrorResponse errorResponse = ErrorResponse.of(
        HttpStatus.CONFLICT.value(),
        "백업 진행 중",
        e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(BackupFailedException.class)
  public ResponseEntity<ErrorResponse> handleBackupFailedException(
      BackupFailedException e) {
    log.error("백업 실패 예외: {}", e.getMessage(), e);
    ErrorResponse errorResponse = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "백업 실패",
        e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(BackupFileSaveFailedException.class)
  public ResponseEntity<ErrorResponse> handleBackupFileSaveFailedException(
      BackupFileSaveFailedException e) {
    log.error("백업 파일 저장 예외: {}", e.getMessage(), e);
    ErrorResponse errorResponse = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "백업 파일 저장 실패",
        e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
    log.error("Unexpected error occurred: {}", e.getMessage(), e);
    ErrorResponse errorResponse = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "서버 오류",
        e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(FileDeleteException.class)
  public ResponseEntity<ErrorResponse> handleFileDeleteException(FileDeleteException e) {
    log.error("파일 삭제 실패 예외: {}", e.getMessage(), e);

    ErrorResponse errorResponse = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "파일 삭제 실패",
        e.getMessage()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
