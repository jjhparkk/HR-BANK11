package com.team11.hrbank.module.domain.department.controller;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.department.dto.DepartmentCreateRequest;
import com.team11.hrbank.module.domain.department.dto.DepartmentDto;
import com.team11.hrbank.module.domain.department.dto.DepartmentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/departments")
@Tag(name = "부서 관리", description = "부서 관리 API")
public interface DepartmentApi {

  @Operation(
      summary = "부서 등록",
      description = "새로운 부서를 등록합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "등록 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 중복된 이름"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @PostMapping
  ResponseEntity<DepartmentDto> createDepartment(@RequestBody @Valid DepartmentCreateRequest request);

  @Operation(
      summary = "부서 수정",
      description = "부서 정보를 수정합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "수정 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 중복된 이름"),
          @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @PatchMapping("/{id}")
  ResponseEntity<DepartmentDto> updateDepartment(
      @Parameter(description = "부서 ID", required = true)
      @PathVariable("id") Long id,
      @RequestBody @Valid DepartmentUpdateRequest request);

  @Operation(
      summary = "부서 삭제",
      description = "부서를 삭제합니다.",
      responses = {
          @ApiResponse(responseCode = "204", description = "삭제 성공"),
          @ApiResponse(responseCode = "400", description = "소속 직원이 있는 부서는 삭제할 수 없음"),
          @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @DeleteMapping("/{id}")
  ResponseEntity<Void> deleteDepartment(
      @Parameter(description = "부서 ID", required = true)
      @PathVariable Long id);

  @Operation(
      summary = "부서 상세 조회",
      description = "부서 상세 정보를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping("/{id}")
  ResponseEntity<DepartmentDto> getDepartmentById(
      @Parameter(description = "부서 ID", required = true)
      @PathVariable Long id);

  @Operation(
      summary = "부서 목록 조회",
      description = "부서 목록을 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping
  ResponseEntity<CursorPageResponse<DepartmentDto>> getAllDepartments(
      @Parameter(description = "부서 이름 또는 설명")
      @RequestParam(required = false) String nameOrDescription,

      @Parameter(description = "이전 페이지 마지막 요소 ID")
      @RequestParam(required = false) Long idAfter,

      @Parameter(description = "커서 (다음 페이지 시작점)")
      @RequestParam(required = false) String cursor,

      @Parameter(description = "페이지 크기 (기본값: 10)")
      @RequestParam(required = false, defaultValue = "10") Integer size,

      @Parameter(description = "정렬 필드 (name 또는 establishedDate)")
      @RequestParam(required = false, defaultValue = "establishedDate") String sortField,

      @Parameter(description = "정렬 방향 (asc 또는 desc, 기본값: asc)")
      @RequestParam(required = false, defaultValue = "asc") String sortDirection);
}