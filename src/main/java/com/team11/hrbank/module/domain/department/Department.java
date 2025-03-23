package com.team11.hrbank.module.domain.department;

import com.team11.hrbank.module.domain.UpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "departments")
public class Department extends UpdatableEntity {

  @NotNull
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", length = Integer.MAX_VALUE)
  private String description;

  @Column(name = "established_date", nullable = false) //columnDefinition = "DATE")
  private LocalDate establishedDate;

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}