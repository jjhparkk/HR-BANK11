package com.team11.hrbank.module.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageConfig {

}
