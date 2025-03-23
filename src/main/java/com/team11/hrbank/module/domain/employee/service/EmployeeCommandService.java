package com.team11.hrbank.module.domain.employee.service;

import com.team11.hrbank.module.common.exception.ResourceNotFoundException;
import com.team11.hrbank.module.domain.EmployeeNumberGenerator;
import com.team11.hrbank.module.domain.changelog.ChangeLog;
import com.team11.hrbank.module.domain.changelog.ChangeLogDiff;
import com.team11.hrbank.module.domain.changelog.DiffEntry;
import com.team11.hrbank.module.domain.changelog.HistoryType;
import com.team11.hrbank.module.domain.changelog.repository.ChangeLogRepository;
import com.team11.hrbank.module.domain.department.Department;
import com.team11.hrbank.module.domain.department.repository.DepartmentRepository;
import com.team11.hrbank.module.domain.employee.Employee;
import com.team11.hrbank.module.domain.employee.EmployeeStatus;
import com.team11.hrbank.module.domain.employee.dto.EmployeeCreateRequest;
import com.team11.hrbank.module.domain.employee.dto.EmployeeDto;
import com.team11.hrbank.module.domain.employee.dto.EmployeeUpdateRequest;
import com.team11.hrbank.module.domain.employee.mapper.EmployeeMapper;
import com.team11.hrbank.module.domain.employee.repository.EmployeeRepository;
import com.team11.hrbank.module.domain.file.File;
import com.team11.hrbank.module.domain.file.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeCommandService {

  private final EmployeeRepository employeeRepository;
  private final FileService fileService;
  private final DepartmentRepository departmentRepository;
  private final EmployeeMapper employeeMapper;
  private final EmployeeNumberGenerator employeeNumberGenerator;
  private final ChangeLogRepository changeLogRepository;

  // 직원 생성
  @Transactional
  public EmployeeDto createEmployee(EmployeeCreateRequest employeeCreateRequest,
      MultipartFile file, HttpServletRequest request) throws Exception {

    /** 요구 조건 : 이메일 중복 여부 검증 -> 레포지토리 단에서 email 컬럼만 들고올 수 있는 메서드 작성 **/
    if (employeeRepository.findAllEmails().stream()
            .anyMatch(email -> email.equals(employeeCreateRequest.email()))) {
      throw new IllegalArgumentException("email(" + employeeCreateRequest.email() + ")은 이미 존재합니다.");
    }

    File savedProfileImage = null;
    if (file != null && !file.isEmpty()) {
      savedProfileImage = fileService.uploadFile(file);
      log.info("직원 프로필 이미지 업로드 성공: {}", savedProfileImage.getFileName());
    }

    //부서 검증
    Department department = departmentRepository.findById(employeeCreateRequest.departmentId())
        .orElseThrow(() -> ResourceNotFoundException.of("Department", "departmentId",
            employeeCreateRequest.departmentId()));

    // 직원 생성
    Employee employee = Employee.builder()
        .name(employeeCreateRequest.name())
        .email(employeeCreateRequest.email())
        .employeeNumber(employeeNumberGenerator.generateEmployeeNumber())
        .department(department)
        .position(employeeCreateRequest.position())
        .hireDate(employeeCreateRequest.hireDate())
        .profileImage(savedProfileImage)
        .status(EmployeeStatus.ACTIVE) // 재직중 초기화 조건, 엔티티에 설정된 에노테이션은 DB 레벨에 지정된 것
        .build();

    // 직원 저장
    employeeRepository.save(employee);

    // 직원 변경 이력 생성
    String ipAddress = getIpAddress(request);
    ChangeLog changeLog = ChangeLog.create(employee,
        employee.getEmployeeNumber(),
        employeeCreateRequest.memo(),
        ipAddress,
        HistoryType.CREATED);

    changeLogRepository.save(changeLog);

    return employeeMapper.toDto(employee);
  }

  // 직원 수정
  @Transactional
  public EmployeeDto updateEmployee(Long id, EmployeeUpdateRequest employeeUpdateRequest,
      MultipartFile file, HttpServletRequest request) throws IOException {

    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Employee", "id", id));

    List<DiffEntry> changes = new ArrayList<>();
    boolean hasChanges = false;

    String ipAddress = getIpAddress(request);

    // 이름 변경
    if (employeeUpdateRequest.name() != null && !employeeUpdateRequest.name()
        .equals(employee.getName())) {
      changes.add(DiffEntry.of("이름", employee.getName(), employeeUpdateRequest.name()));
      employee.updateName(employeeUpdateRequest.name());
      hasChanges = true;
    }

    // 이메일 변경
    if (employeeUpdateRequest.email() != null && !employeeUpdateRequest.email()
        .equals(employee.getEmail())) {
      // 중복 검사 (자기 자신 제외)
      if (employeeRepository.findAllEmails().stream()
          .filter(email -> !email.equals(employee.getEmail()))
          .anyMatch(email -> email.equals(employeeUpdateRequest.email()))) {
        throw new IllegalArgumentException(
            "email: " + employeeUpdateRequest.email() + " 은 이미 존재합니다.");
      }

      changes.add(DiffEntry.of("이메일", employee.getEmail(), employeeUpdateRequest.email()));
      employee.updateEmail(employeeUpdateRequest.email());
      hasChanges = true;
    }

    if (employeeUpdateRequest.departmentId() != null && (employee.getDepartment() == null
        || !employeeUpdateRequest.departmentId().equals(employee.getDepartment().getId()))) {
      Department newDepartment = departmentRepository.findById(employeeUpdateRequest.departmentId())
          .orElseThrow(() -> ResourceNotFoundException.of("Department", "id",
              employeeUpdateRequest.departmentId()));

      String oldDeptName =
          employee.getDepartment() != null ? employee.getDepartment().getName() : "";
      changes.add(DiffEntry.of("부서", oldDeptName, newDepartment.getName()));
      employee.updateDepartment(newDepartment);
      hasChanges = true;
    }

    // 직함 변경
    if (employeeUpdateRequest.position() != null && !employeeUpdateRequest.position()
        .equals(employee.getPosition())) {
      changes.add(DiffEntry.of("직함", employee.getPosition(), employeeUpdateRequest.position()));
      employee.updatePosition(employeeUpdateRequest.position());
      hasChanges = true;
    }

    if (employeeUpdateRequest.hireDate() != null && !employeeUpdateRequest.hireDate()
        .equals(employee.getHireDate())) {
      changes.add(DiffEntry.of("입사일",
          employee.getHireDate() != null ? employee.getHireDate().toString() : "",
          employeeUpdateRequest.hireDate().toString()));
      employee.updateHireDate(employeeUpdateRequest.hireDate());
      hasChanges = true;
    }

      // 상태 변경
    if (employeeUpdateRequest.status() != null && !employeeUpdateRequest.status()
        .equals(employee.getStatus())) {
      changes.add(DiffEntry.of("상태",
          employee.getStatus() != null ? employee.getStatus().toString() : "",
          employeeUpdateRequest.status().toString()));
      employee.updateStatus(employeeUpdateRequest.status());
      hasChanges = true;
    }

    // 프로필 이미지 변경
    if (file != null && !file.isEmpty()) {
      log.info("파일 업데이트 - 원본 파일명: {}, 크기: {}bytes",
          file.getOriginalFilename(), file.getSize());

      try {
        // 있으면 업데이트, 없으면 새로 업로드
        File newProfileImage = fileService.updateFile(employee.getProfileImage(), file);

        log.info("새 프로필 이미지 생성됨: ID={}, 파일명={}",
            newProfileImage.getId(), newProfileImage.getFileName());

        String oldFileName = employee.getProfileImage() != null ?
            employee.getProfileImage().getFileName() : "없음";

        changes.add(DiffEntry.of("프로필 이미지", oldFileName, newProfileImage.getFileName()));
        employee.updateProfileImage(newProfileImage);
        hasChanges = true;
      } catch (IOException e) {
        log.error("프로필 이미지 업데이트 실패: {}", e.getMessage(), e);
        throw new IOException("프로필 이미지 업데이트 중 오류 발생", e);
      }
    }

    //변경 이력 저장
    if (hasChanges || employeeUpdateRequest.memo() != null) {
      // 메모만 있는 경우
      if (!hasChanges) {
        changes.add(DiffEntry.of("메모", "", employeeUpdateRequest.memo()));
      }

      // 변경 이력 생성
      String memo = employeeUpdateRequest.memo() != null ? employeeUpdateRequest.memo() : "직원 정보 수정";

      ChangeLog changeLog = ChangeLog.create(
          employee,
          employee.getEmployeeNumber(),
          memo,
          ipAddress,
          HistoryType.UPDATED
      );

      // 변경 세부 내역 저장
      ChangeLogDiff changeLogDiff = ChangeLogDiff.create(changeLog, changes);
      changeLog.setChangeLogDiff(changeLogDiff);

      changeLogRepository.save(changeLog);

    }
    return employeeMapper.toDto(employee);
  }

  @Transactional
  public void deleteEmployee(Long id, HttpServletRequest request) {
    // 직원 조회
    Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Employee","id", id));

    // 직원 정보 추출
    String employeeNumber = employee.getEmployeeNumber();
    String name = employee.getName();
    String position = employee.getPosition();
    String departmentName = employee.getDepartment() != null ? employee.getDepartment().getName() : "";
    String email = employee.getEmail();
    String status = employee.getStatus() != null ? employee.getStatus().toString() : "";
    String hireDate = employee.getHireDate() != null ? employee.getHireDate().toString() : "";

    // 프로필 이미지 정보 추출 및 참조 제거
    File profileImage = employee.getProfileImage();
    if (profileImage != null) {
      employee.updateProfileImage(null);
      employeeRepository.save(employee);
    }

    // 직원 삭제
    employeeRepository.delete(employee);
    log.info("직원 id: {} db에서 완전히 삭제", id);

    // 삭제 이력 생성 (employee 참조 없이 처리)
    try {
      String ipAddress;
      try {
        ipAddress = getIpAddress(request);
      } catch (UnknownHostException e) {
        log.warn("IP 주소 조회 실패, 기본값 사용: {}", e.getMessage());
        ipAddress = "0.0.0.0";
      }

      // employee 파라미터 없이 처리
      ChangeLog changeLog = ChangeLog.createForDelete(
              employeeNumber,
              "직원 삭제 처리",
              ipAddress
      );

      // 변경 세부 내역 생성
      List<DiffEntry> changes = new ArrayList<>();
      changes.add(DiffEntry.of("입사일", hireDate, ""));
      changes.add(DiffEntry.of("이름", name, ""));
      changes.add(DiffEntry.of("직함", position, ""));
      changes.add(DiffEntry.of("부서명", departmentName, ""));
      changes.add(DiffEntry.of("이메일", email, ""));
      changes.add(DiffEntry.of("상태", status, ""));

      ChangeLogDiff changeLogDiff = ChangeLogDiff.create(changeLog, changes);
      changeLog.setChangeLogDiff(changeLogDiff);

      changeLogRepository.save(changeLog);
      log.info("직원 삭제 이력 생성 완료: {}", employeeNumber);

    } catch (Exception e) {
      log.error("직원 삭제 이력 생성 중 오류: {}", e.getMessage(), e);
    }

    // 프로필 이미지 삭제
    if (profileImage != null) {
      try {
        fileService.deleteFile(profileImage);
        log.info("직원 프로필 이미지 삭제 성공: {}", profileImage.getFileName());
      } catch (Exception e) {
        log.error("프로필 이미지 삭제 실패: {}", e.getMessage(), e);
      }
    }
  }

  private String getIpAddress(HttpServletRequest request) throws UnknownHostException {
    String ipAddress = null;

    //프록시 헤더 확인
    String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    for (String header : headers) {
      ipAddress = request.getHeader(header);
      if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
        break;
      }
    }

    // 모든 헤더에서 실패하면 직접 IP 가져오기
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }

    // 로컬호스트 주소 처리
    if (ipAddress == null || ipAddress.isEmpty() || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
      ipAddress = "127.0.0.1";
    }

    // 쉼표로 구분된 여러 IP 주소가 있는 경우 첫 번째 주소만 사용
    if (ipAddress != null && ipAddress.contains(",")) {
      ipAddress = ipAddress.split(",")[0].trim();
    }

    log.info("추출된 클라이언트 IP 주소: {}", ipAddress);

    return ipAddress;
  }

}
