package com.team11.hrbank.module.domain.backup.mapper;

import com.team11.hrbank.module.domain.backup.BackupHistory;
import com.team11.hrbank.module.domain.backup.dto.BackupDto;
import com.team11.hrbank.module.domain.file.File;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface BackupMapper {

  @Mapping(source = "startAt", target = "startedAt")
  @Mapping(source = "file.id", target = "fileId", qualifiedByName = "extractFileId")
  BackupDto toDto(BackupHistory backupHistory);

  List<BackupDto> toDtoList(List<BackupHistory> backupHistories);

  @Named("extractFileId")
  default Long extractFileId(Object file) {
    if (file == null) {
      return null;
    }

    if (file instanceof Long) {
      return (Long) file;
    }

    if (file instanceof File) {
      return ((File) file).getId();
    }

    return null;
  }

}
