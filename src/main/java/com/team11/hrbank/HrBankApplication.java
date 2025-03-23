package com.team11.hrbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HrBankApplication {

  public static void main(String[] args) {
    SpringApplication.run(HrBankApplication.class, args);
  }

}

