package com.team11.hrbank.module.domain.department.service;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.department.dto.DepartmentCreateRequest;
import com.team11.hrbank.module.domain.department.dto.DepartmentDto;
import com.team11.hrbank.module.domain.department.dto.DepartmentUpdateRequest;
import jakarta.validation.Valid;

public interface DepartmentService {

  // 부서 생성
  DepartmentDto createDepartment(DepartmentCreateRequest request);

  //부서 수정
  DepartmentDto updateDepartment(Long id, @Valid DepartmentUpdateRequest request);


  //부서 삭제
  void deleteDepartment(Long id);

  //개별 상세 조회
  DepartmentDto getDepartmentById(Long id);


  //부서 전체 조회 및 페이지네이션
  CursorPageResponse<DepartmentDto> getAllDepartments(
      String nameOrDescription,
      Long idAfter,
      String cursor,
      Integer size,
      String sortField,
      String sortDirection);
}