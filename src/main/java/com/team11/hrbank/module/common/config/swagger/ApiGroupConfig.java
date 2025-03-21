package com.team11.hrbank.module.common.config.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGroupConfig {
  @Bean
  public GroupedOpenApi backupApi() {
    return GroupedOpenApi.builder()
        .group("backup-management")
        .pathsToMatch("/api/backups/**")
        .build();
  }

  @Bean
  public GroupedOpenApi changeLogApi() {
    return GroupedOpenApi.builder()
        .group("change-log-management")
        .pathsToMatch("/api/change-logs/**")
        .build();
  }

  @Bean
  public GroupedOpenApi departmentApi() {
    return GroupedOpenApi.builder()
        .group("department-management")
        .pathsToMatch("/api/departments/**")
        .build();
  }

  @Bean
  public GroupedOpenApi employeeApi() {
    return GroupedOpenApi.builder()
        .group("employee-management")
        .pathsToMatch("/api/employees/**")
        .build();
  }

  @Bean
  public GroupedOpenApi fileApi() {
    return GroupedOpenApi.builder()
        .group("file-management")
        .pathsToMatch("/api/files/**")
        .build();
  }
}
