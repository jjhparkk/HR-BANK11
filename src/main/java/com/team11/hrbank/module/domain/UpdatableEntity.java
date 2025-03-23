package com.team11.hrbank.module.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@MappedSuperclass
@Getter
public class UpdatableEntity extends BaseEntity{
  @LastModifiedDate
  @Column(name = "updated_at")
  protected Instant updatedAt;
}
