package com.team11.hrbank.module.domain.department.mapper;

import com.team11.hrbank.module.domain.department.Department;
import com.team11.hrbank.module.domain.department.dto.DepartmentCreateRequest;
import com.team11.hrbank.module.domain.department.dto.DepartmentDto;
import com.team11.hrbank.module.domain.department.dto.DepartmentUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface DepartmentMapper {

  Department toDepartment(DepartmentCreateRequest request);

  // 엔티티를 DTO로 변환 (employeeCount는 별도로 처리)
  @Mapping(target = "employeeCount", ignore = true)
  DepartmentDto toDepartmentDto(Department department);

  // DTO로 변환하면서 employeeCount 설정 (수동으로 호출할 메서드)
  @Mapping(target = "employeeCount", source = "employeeCount")
  DepartmentDto toDepartmentDtoWithEmployeeCount(Department department, Long employeeCount);

  // 업데이트 요청으로 엔티티 업데이트
  Department updateDepartmentFromRequest(@MappingTarget Department department, DepartmentUpdateRequest request);
}