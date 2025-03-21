package com.team11.hrbank.module.domain.changelog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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