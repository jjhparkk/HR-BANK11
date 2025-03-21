package com.team11.hrbank.module.domain.backup.exception;

public class BackupAlreadyInProgressException extends BackupException {

  public BackupAlreadyInProgressException(String message) {
    super(message);
  }

  public BackupAlreadyInProgressException(String message, Throwable cause) {
    super(message, cause);
  }
}
