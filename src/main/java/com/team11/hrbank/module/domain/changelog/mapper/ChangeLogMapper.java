package com.team11.hrbank.module.domain.changelog.mapper;

import com.team11.hrbank.module.domain.changelog.ChangeLog;
import com.team11.hrbank.module.domain.changelog.dto.ChangeLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChangeLogMapper {

  @Mapping(source = "createdAt", target = "at")
  ChangeLogDto toDto(ChangeLog changeLog);

  List<ChangeLogDto> toDtoList(List<ChangeLog> changeLogs);


}
