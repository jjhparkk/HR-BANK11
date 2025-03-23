package com.team11.hrbank.module.domain.file.exception;

/**
 * 파일 삭제 실패 시 예외
 */
public class FileDeleteException extends FileException {

  public FileDeleteException(String message, Throwable cause) {
    super(message, cause);
  }
}
