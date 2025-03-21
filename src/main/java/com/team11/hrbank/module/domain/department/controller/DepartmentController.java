package com.team11.hrbank.module.domain.department.controller;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.department.dto.DepartmentCreateRequest;
import com.team11.hrbank.module.domain.department.dto.DepartmentDto;
import com.team11.hrbank.module.domain.department.dto.DepartmentUpdateRequest;
import com.team11.hrbank.module.domain.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "부서 관리 API")
public class DepartmentController {

  private final DepartmentService departmentService;

  @PostMapping
  public ResponseEntity<DepartmentDto> createDepartment(@RequestBody @Valid DepartmentCreateRequest request) {
    DepartmentDto department = departmentService.createDepartment(request);
    return ResponseEntity.ok(department);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable("id") Long id, @RequestBody @Valid DepartmentUpdateRequest request) {
    DepartmentDto updateDepartment = departmentService.updateDepartment(id, request);
    return ResponseEntity.ok(updateDepartment);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
    departmentService.deleteDepartment(id);
    return ResponseEntity.ok("부서 ID: " + id + " 삭제되었습니다");
  }

  @GetMapping("/{id}")
  public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
    DepartmentDto department = departmentService.getDepartmentById(id);
    return ResponseEntity.ok(department);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<DepartmentDto>> getAllDepartments(
      @RequestParam(required = false) String nameOrDescription,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false, defaultValue = "establishedDate") String sortField,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {

    CursorPageResponse<DepartmentDto> result = departmentService.getAllDepartments(
        nameOrDescription, idAfter, cursor, size, sortField, sortDirection);

    return ResponseEntity.ok(result);
  }
}