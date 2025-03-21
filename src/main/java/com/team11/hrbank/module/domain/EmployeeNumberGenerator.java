package com.team11.hrbank.module.domain;

import java.security.SecureRandom;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class EmployeeNumberGenerator {

  private final SecureRandom secureRandom = new SecureRandom();

  public String generateEmployeeNumber() {
    int year = LocalDate.now().getYear();
    StringBuilder stringBuilder = new StringBuilder();

    for (int i = 0; i < 14; i++) {
      stringBuilder.append(secureRandom.nextInt(10));
    }

    return String.format("EMP-%d-%s", year, stringBuilder);

  }
}
