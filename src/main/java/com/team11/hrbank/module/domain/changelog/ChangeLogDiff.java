package com.team11.hrbank.module.domain.changelog;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Entity
@Table(name = "change_log_diffs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeLogDiff {

  @Id
  @Column(name = "change_log_id")
  private Long id;

  @MapsId //
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "change_log_id", nullable = false)
  private ChangeLog changeLog;

  @NotNull
  @Column(name = "changes", nullable = false, columnDefinition = "JSONB")
  @JdbcTypeCode(SqlTypes.JSON)
  private List<DiffEntry> changes;

  public static ChangeLogDiff create(ChangeLog changeLog, List<DiffEntry> changes) {
    ChangeLogDiff diff = new ChangeLogDiff();
    diff.changeLog = changeLog;
    diff.changes = changes;
    return diff;
  }

  void setChangeLog(ChangeLog changeLog) {
    this.changeLog = changeLog;
  }

}