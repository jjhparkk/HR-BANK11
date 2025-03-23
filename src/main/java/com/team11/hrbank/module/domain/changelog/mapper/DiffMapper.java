package com.team11.hrbank.module.domain.changelog.mapper;

import com.team11.hrbank.module.domain.changelog.DiffEntry;
import com.team11.hrbank.module.domain.changelog.dto.DiffDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DiffMapper {

  DiffDto toDto(DiffEntry entry);

  List<DiffDto> toDtoList(List<DiffEntry> diffEntries);
}
