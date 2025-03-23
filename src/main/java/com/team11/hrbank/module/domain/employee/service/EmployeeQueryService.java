package com.team11.hrbank.module.domain.employee.service;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.common.exception.ResourceNotFoundException;
import com.team11.hrbank.module.domain.employee.Employee;
import com.team11.hrbank.module.domain.employee.EmployeeStatus;
import com.team11.hrbank.module.domain.employee.dto.CursorPageResponseEmployeeDto;
import com.team11.hrbank.module.domain.employee.dto.EmployeeDistributionDto;
import com.team11.hrbank.module.domain.employee.dto.EmployeeDto;
import com.team11.hrbank.module.domain.employee.dto.EmployeeTrendDto;
import com.team11.hrbank.module.domain.employee.mapper.EmployeeMapper;
import com.team11.hrbank.module.domain.employee.repository.EmployeeRepository;
import com.team11.hrbank.module.domain.employee.repository.EmployeeRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeQueryService {

  private final EmployeeRepository employeeRepository;
  private final EmployeeRepositoryCustom employeeRepositoryCustom;
  private final EmployeeMapper employeeMapper;

  // 부서 별 직원 수
  public Long countByDepartmentId(Long departmentId) {
    return employeeRepository.countByDepartmentId(departmentId);
  }

  // 직원 상세 조회
  public EmployeeDto getEmployeeDetails(Long id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Employee", "id", id));
    return employeeMapper.toDto(employee);
  }

  // 직원 목록 조회
  public CursorPageResponseEmployeeDto getListEmployees(
      String nameOrEmail,
      String employeeNumber,
      String departmentName,
      String position,
      LocalDate hireDateFrom,
      LocalDate hireDateTo,
      EmployeeStatus status,
      Long idAfter,
      String cursor,
      Integer size,
      String sortField,
      String sortDirection
  ) {
    /** -- 250323 주석처리
     if (cursor != null && !cursor.isEmpty() && idAfter == null) {
     idAfter = CursorPageResponse.extractIdFromCursor(cursor);
     }

     List<Employee> employees = employeeRepositoryCustom.findEmployeesByConditions(
     nameOrEmail,
     employeeNumber,
     departmentName,
     position,
     hireDateFrom,
     hireDateTo,
     status,
     idAfter,
     cursor,
     size + 1,
     sortField,
     sortDirection);

     Long lastId = null;

     if (employees.size() > size) {
     // size + 1로 조회했기 때문에, 마지막 직원은 실제 목록에 포함되지 않는다.
     employees.remove(employees.size() - 1);  // 마지막 직원 제거
     }

     if (!employees.isEmpty()) {
     lastId = employees.get(employees.size() - 1).getId();
     }

     List<EmployeeDto> employeeDtos = employees.stream().map(employeeMapper::toDto).toList();

     long totalCount = getEmployeeCount(status, hireDateFrom, hireDateTo);

     return CursorPageResponse.of(employeeDtos, lastId, size, totalCount);
     **/

    List<Employee> employees = employeeRepositoryCustom.findEmployeesByConditions(
        nameOrEmail,
        employeeNumber,
        departmentName,
        position,
        hireDateFrom,
        hireDateTo,
        status,
        idAfter,
        cursor,
        size + 1,
        sortField,
        sortDirection);

    if (employees.isEmpty()) {
      throw new ResourceNotFoundException("해당 조건에 맞는 직원이 존재하지 않습니다.");
    }

    // hasNext 계산: size+1로 조회했기 때문에 employees.size() > size 이면 다음 페이지 존재
    boolean hasNext = employees.size() > size;

    // 다음 페이지가 있는 경우 마지막 직원(추가로 조회한 직원) 제거
    if (hasNext) {
      employees.remove(employees.size() - 1);
    }

    // 리스트가 비어있지 않다면 마지막 직원 정보 가져오기
    Employee lastEmployee = employees.get(employees.size() - 1);

    // 다음 조회 시 사용할 idAfter 값 설정
    Long nextIdAfter = hasNext ? lastEmployee.getId() : null;

    // 다음 조회 시 사용할 cursor 값 설정
    String nextCursor = null;
    if (hasNext) {
      switch (sortField) {
        case "name":
          nextCursor = lastEmployee.getName();
          break;
        case "employeeNumber":
          nextCursor = lastEmployee.getEmployeeNumber();
          break;
        case "hireDate":
          nextCursor = lastEmployee.getHireDate().toString();
          break;
        default:
          nextCursor = lastEmployee.getName(); // 기본값 설정
      }
    }

    return new CursorPageResponseEmployeeDto(
        employees.stream()
            .map(employeeMapper::toDto).toList(),
        nextCursor,
        nextIdAfter,
        employees.size(),
        getEmployeeCount(status, hireDateFrom, hireDateTo),
        hasNext
    );
  }

  // 직원 분포 조회
  public List<EmployeeDistributionDto> getEmployeeDistribution(String groupBy, String status) {

    EmployeeStatus employeeStatus = null;
    if (status != null && !status.isEmpty()) {
      try {
        employeeStatus = EmployeeStatus.valueOf(status);
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 상태 값입니다: {}", status);
      }
    }

    return employeeRepositoryCustom.findEmployeeDistribution(groupBy,employeeStatus);
  }

  // 직원 수 추이
  public List<EmployeeTrendDto> getEmployeeTrend(LocalDate from, LocalDate to, String periodType) {
    // 기본값 설정: from과 to가 null일 경우 최근 12개월 데이터 반환
    if (from == null) {
      from = LocalDate.now().minusMonths(12);
    }
    if (to == null) {
      to = LocalDate.now();
    }

    return switch (periodType.toLowerCase()){
      case "day" -> getDailyStats(from, to);
      case "week" -> getWeeklyStats(from, to);
      case "month" -> getMonthlyStats(from, to);
      case "quarter" -> getQuarterlyStats(from, to);
      case "year" -> getYearlyStats(from, to);
      default -> throw new IllegalArgumentException("올바르지 않은 시간 단위입니다:" + periodType);
    };
  }

  //일별 통계 조회
  private List<EmployeeTrendDto> getDailyStats(LocalDate from, LocalDate to) {
    List<EmployeeTrendDto> dailyStats = new ArrayList<>();
    LocalDate current = from;

    while (current.isBefore(to) || current.isEqual(to)) {
      LocalDate nextDay = current.plusDays(1);
      long currentCount = employeeRepository.countEmployeesByHireDateBetween(current, nextDay);

      // 이전 날의 데이터를 구하기 위한 코드
      LocalDate previousDay = current.minusDays(1);
      long previousCount = employeeRepository.countEmployeesByHireDateBetween(previousDay, current);

      long change = currentCount - previousCount;
      double changeRate = previousCount > 0 ? (double) change / previousCount * 100 : 0.0;

      dailyStats.add(new EmployeeTrendDto(
          current.toString(),
          currentCount,
          change,
          Math.round(changeRate * 100) / 100.0
      ));

      current = nextDay;
    }

    return dailyStats;
  }

  // 주별 데이터 처리
  private List<EmployeeTrendDto> getWeeklyStats(LocalDate from, LocalDate to) {
    List<EmployeeTrendDto> weeklyStats = new ArrayList<>();
    LocalDate current = from;
    //주 시작일을 수요일로 조정(프로토타입과 일치)
    current = current.with(DayOfWeek.WEDNESDAY);

    // 주 시작일을 기준으로 진행
    while (current.isBefore(to) || current.isEqual(to)) {
      LocalDate endOfWeek = current.plusWeeks(1);

      long currentCount = employeeRepository.countEmployeesByHireDateBetween(current, endOfWeek);

      // 지난주 데이터
      LocalDate previousWeekStart = current.minusWeeks(1);
      long previousCount = employeeRepository.countEmployeesByHireDateBetween(
          previousWeekStart, current);

      long change = currentCount - previousCount;
      double changeRate = previousCount > 0 ? (double) change / previousCount * 100 : 0.0;

      weeklyStats.add(new EmployeeTrendDto(
          current.toString(),
          currentCount,
          change,
          Math.round(changeRate * 100) / 100.0
      ));

      current = endOfWeek;
    }

    return weeklyStats;
  }

  // 월별 데이터 처리
  private List<EmployeeTrendDto> getMonthlyStats(LocalDate from, LocalDate to) {
    List<EmployeeTrendDto> monthlyStats = new ArrayList<>();
    LocalDate current = from.withDayOfMonth(1);

    while (current.isBefore(to) || current.isEqual(to)) {
      LocalDate nextMonth = current.plusMonths(1);

      long currentCount = employeeRepository.countEmployeesByHireDateBetween(current, nextMonth);

      // 이전 월의 데이터
      LocalDate previousMonth = current.minusMonths(1);
      long previousCount = employeeRepository.countEmployeesByHireDateBetween(previousMonth, current);

      // 변화량 계산
      long change = currentCount - previousCount;
      double changeRate = previousCount > 0 ? (double) change / previousCount * 100 : 0.0;

      monthlyStats.add(new EmployeeTrendDto(
          current.toString(),
          currentCount,
          change,
          Math.round(changeRate * 100) / 100.0
      ));

      current = nextMonth;
    }

    return monthlyStats;
  }

  // 분기별 통계 조회
  private List<EmployeeTrendDto> getQuarterlyStats(LocalDate from, LocalDate to) {
    List<EmployeeTrendDto> quarterlyStats = new ArrayList<>();

    // 분기의 첫날로 조정
    int currentQuarter = (from.getMonthValue() - 1) / 3;
    LocalDate current = from.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1);

    while (current.isBefore(to) || current.isEqual(to)) {
      LocalDate nextQuarter = current.plusMonths(3);

      long currentCount = employeeRepository.countEmployeesByHireDateBetween(current, nextQuarter);

      // 이전 분기의 데이터
      LocalDate previousQuarter = current.minusMonths(3);
      long previousCount = employeeRepository.countEmployeesByHireDateBetween(
          previousQuarter, current);

      // 변화량 계산
      long change = currentCount - previousCount;
      double changeRate = previousCount > 0 ? (double) change / previousCount * 100 : 0.0;

      quarterlyStats.add(new EmployeeTrendDto(
          current.toString(),
          currentCount,
          change,
          Math.round(changeRate * 100) / 100.0
      ));

      current = nextQuarter;
    }

    return quarterlyStats;
  }


  // 연도별 통계
  private List<EmployeeTrendDto> getYearlyStats(LocalDate from, LocalDate to) {
    List<EmployeeTrendDto> yearlyStats = new ArrayList<>();
    LocalDate current = from.withDayOfYear(1);

    while (current.isBefore(to) || current.isEqual(to)) {
      LocalDate nextYear = current.plusYears(1);

      long currentCount = employeeRepository.countEmployeesByHireDateBetween(current, nextYear);

      // 전년도 데이터
      LocalDate previousYear = current.minusYears(1);
      long previousCount = employeeRepository.countEmployeesByHireDateBetween(previousYear, current);

      long change = currentCount - previousCount;
      double changeRate = previousCount > 0 ? (double) change / previousCount * 100 : 0.0;

      yearlyStats.add(new EmployeeTrendDto(
          current.toString(),
          currentCount,
          change,
          Math.round(changeRate * 100) / 100.0
      ));

      current = nextYear;
    }

    return yearlyStats;
  }


  // 직원 수 조회
  public long getEmployeeCount(EmployeeStatus status, LocalDate fromDate, LocalDate toDate) {
    return employeeRepositoryCustom.countByStatusAndHireDateBetween(status, fromDate, toDate);
  }

}
