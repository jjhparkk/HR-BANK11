package com.team11.hrbank.module.common.exception;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }


  /* service 계층
    public ChangeLog getChangeLogById(Long id) {
    return changeLogRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("ChangeLog", "id", id));
    }
    으로 던지면 ChangeLog not found with id: 123 형식으로 응답
   */
  public static ResourceNotFoundException of(String resourceName, String fieldName,
      Object fieldValue) {
    return new ResourceNotFoundException(
        String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
  }

}
