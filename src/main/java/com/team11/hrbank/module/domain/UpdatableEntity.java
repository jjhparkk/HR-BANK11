package com.team11.hrbank.module.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

@MappedSuperclass
@Getter
public class UpdatableEntity extends BaseEntity{
  @LastModifiedDate
  @Column(name = "updated_at")
  protected Instant updatedAt;
}
