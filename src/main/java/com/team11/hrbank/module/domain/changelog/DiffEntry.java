package com.team11.hrbank.module.domain.changelog;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DiffEntry {
  private String propertyName;
  private String before;
  private String after;

  public static DiffEntry of(String propertyName, String before, String after) {
    return new DiffEntry(propertyName, before, after);
  }

}
