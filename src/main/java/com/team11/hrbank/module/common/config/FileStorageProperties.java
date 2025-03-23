package com.team11.hrbank.module.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {
  private String rootPath;
  private String backupFiles;
  private String errorLogs;
  private String profileImages;
}
