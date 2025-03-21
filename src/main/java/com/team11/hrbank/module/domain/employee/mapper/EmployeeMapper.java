package com.team11.hrbank.module.domain.employee.mapper;

import com.team11.hrbank.module.domain.employee.Employee;
import com.team11.hrbank.module.domain.employee.dto.EmployeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

  @Mapping(source = "department.id", target = "departmentId")
  @Mapping(source = "department.name", target = "departmentName")
  @Mapping(source = "profileImage.id", target = "profileImageId")
  @Mapping(source = "hireDate", target = "hireDate")
  EmployeeDto toDto(Employee employee);

}
