package com.team11.hrbank.module.domain.backup.exception;

public class BackupFileSaveFailedException extends BackupException {

  public BackupFileSaveFailedException(String message) {
    super(message);
  }

  public BackupFileSaveFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
