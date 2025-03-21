package com.team11.hrbank.module.domain.changelog.mapper;

import com.team11.hrbank.module.domain.changelog.ChangeLog;
import com.team11.hrbank.module.domain.changelog.dto.ChangeLogDto;
import java.net.InetAddress;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ChangeLogMapper {

  @Mapping(source = "createdAt", target = "at")
  @Mapping(source = "ipAddress", target = "ipAddress", qualifiedByName = "inetAddressToString")
  ChangeLogDto toDto(ChangeLog changeLog);

  List<ChangeLogDto> toDtoList(List<ChangeLog> changeLogs);

  @Named("inetAddressToString")
  default String inetAddressToString(InetAddress ipAddress) {
    return ipAddress != null ? ipAddress.getHostAddress() : null;
  }

}
