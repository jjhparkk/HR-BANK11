package com.team11.hrbank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("HR Bank API team11")
            .description("팀11 HR Bank API 문서입니다.")
            .version("v1.0.0"))
        .servers(List.of(new Server()
            .url("http://localhost:8080")
            .description("Local Server")));
  }
}
