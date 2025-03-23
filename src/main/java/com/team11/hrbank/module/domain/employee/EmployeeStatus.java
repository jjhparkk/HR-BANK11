package com.team11.hrbank.module.domain.employee;

public enum EmployeeStatus {
  ACTIVE("재직중"),
  ON_LEAVE("휴직중"),
  RESIGNED("퇴사");

  private final String statusName;

  EmployeeStatus(String statusName) {
    this.statusName = statusName;
  }

  public String getDisplayName() {
    return this.statusName;
  }
}
