package com.team11.hrbank.module.domain.backup.exception;

public class BackupException extends RuntimeException {

  public BackupException(String message) {
    super(message);
  }

  public BackupException(String message, Throwable cause) {
    super(message, cause);
  }
}
