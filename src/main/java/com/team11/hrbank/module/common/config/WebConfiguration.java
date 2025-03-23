package com.team11.hrbank.module.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(new PathResourceResolver(){
          @Override
          protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requestedResource = location.createRelative(resourcePath);

            // 요청된 리소스가 존재하면 그대로 반환
            if (requestedResource.exists() && requestedResource.isReadable()) {
              return requestedResource;
            }

            // API 요청 (컨트롤러에 처리 위임)
            if (resourcePath.startsWith("api/")) {
              return null;
            }

            return new ClassPathResource("/static/index.html");
          }
        });

  }
}
