package com.team11.hrbank.module.domain.backup.exception;

public class BackupFailedException extends BackupException {

  public BackupFailedException(String message) {
    super(message);
  }

  public BackupFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
