package com.team11.hrbank.module.domain.changelog;

import com.team11.hrbank.module.domain.BaseEntity;
import com.team11.hrbank.module.domain.employee.Employee;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.net.InetAddress;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "change_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeLog extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(name = "employee_number", nullable = false, length = 25)
  private String employeeNumber;

  @Column(name = "memo", columnDefinition = "TEXT")
  private String memo;

  @Column(name = "ip_address", nullable = false)
  private InetAddress ipAddress;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private HistoryType type;

  @OneToOne(mappedBy = "changeLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private ChangeLogDiff changeLogDiff;

  public static ChangeLog create(Employee employee, String employeeNumber, String memo,
      InetAddress ipAddress, HistoryType type) {
    ChangeLog changeLog = new ChangeLog();
    changeLog.employee = employee;
    changeLog.employeeNumber = employeeNumber;
    changeLog.memo = memo;
    changeLog.ipAddress = ipAddress;
    changeLog.type = type;
    return changeLog;
  }

  public void setChangeLogDiff(ChangeLogDiff changeLogDiff) {
    this.changeLogDiff = changeLogDiff;
    if (changeLogDiff != null && changeLogDiff.getChangeLog() != this) {
      changeLogDiff.setChangeLog(this);
    }
  }

}