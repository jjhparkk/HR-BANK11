package com.team11.hrbank.module.domain.department.controller;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.department.dto.DepartmentCreateRequest;
import com.team11.hrbank.module.domain.department.dto.DepartmentDto;
import com.team11.hrbank.module.domain.department.dto.DepartmentUpdateRequest;
import com.team11.hrbank.module.domain.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController implements DepartmentApi {

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
  public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
    departmentService.deleteDepartment(id);
    return ResponseEntity.noContent().build();
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