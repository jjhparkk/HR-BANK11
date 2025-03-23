package com.team11.hrbank.module.domain.backup.service.data;

import com.team11.hrbank.module.domain.department.Department;
import com.team11.hrbank.module.domain.department.repository.DepartmentRepository;
import com.team11.hrbank.module.domain.employee.Employee;
import com.team11.hrbank.module.domain.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 백업을 위한 데이터 추출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupDataService {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(
      ZoneOffset.UTC);

  /**
   * 직원 데이터를 CSV 포맷 문자열 스트림으로 추출
   * @return 각 라인이 CSV 형식으로 포맷된 문자열 스트림
   */
  @Transactional(readOnly = true)
  public Stream<String[]> getEmployeeDataForBackup() {
    log.info("직원 데이터 백업 추출 시작");

    // 부서 ID를 부서명에 매핑 (N+1 문제 방지)
    Map<Long, String> departmentMap =
        departmentRepository.findAll().stream()
        .collect(Collectors.toMap(Department::getId, Department::getName));

    List<Employee> employees = employeeRepository.findAll();
    log.info("총 직원 수: {}", employees.size());

    return employees.stream()
        .map(employee -> new String[] {
            String.valueOf(employee.getId()),
            employee.getName(),
            employee.getEmail(),
            employee.getEmployeeNumber(),
            employee.getDepartment() != null ? departmentMap.get(employee.getDepartment().getId()) : "",
            employee.getPosition(),
            employee.getHireDate() != null ? DATE_FORMATTER.format(employee.getHireDate()) : "",
            employee.getStatus() != null ? employee.getStatus().name() : "",
            employee.getCreatedAt() != null ? employee.getCreatedAt().toString() : ""
        });
  }

  /**
   * 직원 데이터 CSV 헤더 반환
   * @return 직원 CSV 헤더 배열
   */
  public String[] getEmployeeHeaders() {
    return new String[] {
        "ID", "이름", "이메일", "사원번호", "부서", "직위", "입사일", "상태", "생성일"
    };
  }

  /**
   * 부서 데이터를 CSV 포맷 문자열 스트림으로 추출
   * @return 각 라인이 CSV 형식으로 포맷된 문자열 스트림
   */
  @Transactional(readOnly = true)
  public Stream<String[]> getDepartmentDataForBackup() {
    log.info("부서 데이터 백업 추출 시작");

    List<Department> departments = departmentRepository.findAll();
    log.info("총 부서 수: {}", departments.size());

    return departments.stream()
        .map(dept -> new String[] {
            String.valueOf(dept.getId()),
            dept.getName(),
            dept.getDescription() != null ? dept.getDescription() : "",
            dept.getEstablishedDate() != null ? DATE_FORMATTER.format(dept.getEstablishedDate()) : "",
            dept.getCreatedAt() != null ? dept.getCreatedAt().toString() : ""
        });
  }

  /**
   * 부서 데이터 CSV 헤더 반환
   * @return 부서 CSV 헤더 배열
   */
  public String[] getDepartmentHeaders() {
    return new String[] {
        "ID", "부서명", "설명", "설립일", "생성일"
    };
  }

  /**
   * 모든 백업 데이터를 헤더 정보와 함께 스트림으로 반환
   * @return 모든 엔티티의 데이터를 포함하는 문자열 스트림
   */
  @Transactional(readOnly = true)
  public Stream<String> getAllDataForBackup() {
    // 헤더와 데이터를 직접 CSV 문자열로 변환
    Stream<String> employeeStream = Stream.concat(
        Stream.of(String.join(",", getEmployeeHeaders())),
        getEmployeeDataForBackup().map(row -> String.join(",", Arrays.stream(row)
            .map(BackupDataService::escapeCsvValue)
            .toArray(String[]::new)))
    );

    Stream<String> departmentStream = Stream.concat(
        Stream.of(String.join(",", getDepartmentHeaders())),
        getDepartmentDataForBackup().map(row -> String.join(",", Arrays.stream(row)
            .map(BackupDataService::escapeCsvValue)
            .toArray(String[]::new)))
    );

    // 엔티티 구분을 위한 헤더 + CSV 데이터 스트림
    return Stream.concat(
        Stream.of("## EMPLOYEES ##"),
        Stream.concat(
            employeeStream,
            Stream.concat(
                Stream.of("## DEPARTMENTS ##"),
                departmentStream
            )
        )
    );
  }

  /**
   *  쉼표, 쌍따옴표 등 이스케이프 처리
   */
  private static String escapeCsvValue(String value) {
    if (value == null) {
      return "";
    }

    boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");

    if (needsQuotes) {
      String escaped = value.replace("\"", "\"\"");
      return "\"" + escaped + "\"";
    }

    return value;
  }
}