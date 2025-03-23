package com.team11.hrbank.module.domain.employee.controller;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.domain.employee.EmployeeStatus;
import com.team11.hrbank.module.domain.employee.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/employees")
@Tag(name = "직원 관리", description = "직원 관리 API")
public interface EmployeeApi {

  @Operation(
      summary = "직원 등록",
      description = "새로운 직원을 등록합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "등록 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 중복된 이메일"),
          @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @PostMapping(consumes = {"multipart/form-data"})
  ResponseEntity<EmployeeDto> createEmployee(
      @RequestPart("employee") EmployeeCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile,
      HttpServletRequest servletRequest
  ) throws Exception;

  @Operation(
      summary = "직원 수정",
      description = "직원 정보를 수정합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "수정 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 중복된 이메일"),
          @ApiResponse(responseCode = "404", description = "직원 또는 부서를 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @PatchMapping(value = "/{id}", consumes = {"multipart/form-data"})
  ResponseEntity<EmployeeDto> updateEmployee(
      @PathVariable Long id,
      @RequestPart("employee") EmployeeUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile,
      HttpServletRequest servletRequest
  ) throws Exception;

  @Operation(
      summary = "직원 삭제",
      description = "직원을 삭제합니다.",
      responses = {
          @ApiResponse(responseCode = "204", description = "삭제 성공"),
          @ApiResponse(responseCode = "404", description = "직원을 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @DeleteMapping("/{id}")
  ResponseEntity<Void> deleteEmployee(@PathVariable Long id, HttpServletRequest request);

  @Operation(
      summary = "직원 상세 조회",
      description = "직원 상세 정보를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "404", description = "직원을 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping("/{id}")
  ResponseEntity<EmployeeDto> getEmployeeDetails(@PathVariable Long id);

  @Operation(
      summary = "직원 목록 조회",
      description = "직원 목록을 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping
  ResponseEntity<CursorPageResponseEmployeeDto> getListEmployees(
      @RequestParam(required = false) String nameOrEmail,
      @RequestParam(required = false) String employeeNumber,
      @RequestParam(required = false) String departmentName,
      @RequestParam(required = false) String position,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateTo,
      @RequestParam(required = false) EmployeeStatus status,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sortField,
      @RequestParam(defaultValue = "asc") String sortDirection
  );

  @Operation(
      summary = "직원 분포 조회",
      description = "부서 또는 직무 기준으로 그룹화된 직원 분포를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 그룹화 기준"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping("/stats/distribution")
  ResponseEntity<List<EmployeeDistributionDto>> getEmployeeDistribution(
      @RequestParam(defaultValue = "department") String groupBy,
      @RequestParam(defaultValue = "ACTIVE") String status
  );

  @Operation(
      summary = "직원 수 추이 조회",
      description = "지정된 기간 및 시간 단위로 그룹화된 직원 수 추이를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 시간 단위"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping("/stats/trend")
  ResponseEntity<List<EmployeeTrendDto>> getEmployeeTrend(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false, defaultValue = "month") String unit
  );

  @Operation(
      summary = "직원 수 조회",
      description = "지정된 조건에 맞는 직원 수를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping("/count")
  ResponseEntity<Long> getEmployeeCount(
      @RequestParam(required = false) EmployeeStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
  );
}