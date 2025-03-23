package com.team11.hrbank.module.domain.changelog.service;

import com.team11.hrbank.module.common.dto.CursorPageResponse;
import com.team11.hrbank.module.common.exception.ResourceNotFoundException;
import com.team11.hrbank.module.domain.changelog.ChangeLog;
import com.team11.hrbank.module.domain.changelog.ChangeLogDiff;
import com.team11.hrbank.module.domain.changelog.DiffEntry;
import com.team11.hrbank.module.domain.changelog.HistoryType;
import com.team11.hrbank.module.domain.changelog.dto.ChangeLogDto;
import com.team11.hrbank.module.domain.changelog.dto.DiffDto;
import com.team11.hrbank.module.domain.changelog.mapper.ChangeLogMapper;
import com.team11.hrbank.module.domain.changelog.mapper.DiffMapper;
import com.team11.hrbank.module.domain.changelog.repository.ChangeLogRepository;
import com.team11.hrbank.module.domain.employee.Employee;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ChangeLogServiceImplTest {

  @Mock
  private ChangeLogRepository changeLogRepository;
  @Mock
  private ChangeLogMapper changeLogMapper;
  @Mock
  private DiffMapper diffMapper;
  @InjectMocks
  private ChangeLogServiceImpl changeLogService;

  private ChangeLog changeLog1;
  private ChangeLog changeLog2;
  private List<ChangeLog> changeLogs;
  private List<ChangeLogDto> changeLogDtos;
  private List<DiffEntry> diffEntries;
  private List<DiffDto> diffDtos;
  private Employee employee;
  private ChangeLogDiff changeLogDiff;

  @BeforeEach
  void setUp() throws UnknownHostException {
    //entity
    employee = new Employee();

    changeLog1 = ChangeLog.create(employee,
        "EMP-2025-001",
        "직함 변경",
        InetAddress.getByName("127.0.0.1"),
        HistoryType.UPDATED);

    setId(changeLog1, 1L);

    changeLog2 = ChangeLog.create(
        employee,
        "EMP-2025-002",
        "부서 변경",
        InetAddress.getByName("127.0.0.1"),
        HistoryType.UPDATED
    );

    setId(changeLog2, 2L);

    changeLogs = Arrays.asList(changeLog1, changeLog2);

    //dto
    ChangeLogDto dto1 = new ChangeLogDto(1L,
        "UPDATED",
        "EMP-2025-001",
        "직함 변경",
        "127.0.0.1",
        Instant.now()
    );

    ChangeLogDto dto2 = new ChangeLogDto(
        2L,
        "UPDATED",
        "EMP-2023-002",
        "부서 변경",
        "127.0.0.1",
        Instant.now()
    );

    changeLogDtos = Arrays.asList(dto1, dto2);

    // Diff
    diffEntries = Arrays.asList(
        DiffEntry.of("직함", "사원", "대리"),
        DiffEntry.of("부서", "개발팀", "기획팀")
    );

    diffDtos = Arrays.asList(
        new DiffDto("직함", "사원", "대리"),
        new DiffDto("부서", "개발팀", "기획팀")
    );

    changeLogDiff = ChangeLogDiff.create(changeLog1, diffEntries);
    changeLog1.setChangeLogDiff(changeLogDiff);
  }

  //리플렉션 사용
  private void setId(ChangeLog changeLog, Long id) {
    try{
      Field field = ChangeLog.class.getSuperclass().getDeclaredField("id");
      field.setAccessible(true);
      field.set(changeLog, id);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      log.info("error:{}", e.getMessage());
    }
  }

  @Test
  void getAllChangeLogs_페이징포함_모든_변경로그_반환() throws UnknownHostException{
    // given
    String employeeNumber = "EMP-2025-001";
    HistoryType type = HistoryType.UPDATED;
    String memo = "변경";
//    InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
    String ipAddress ="127.0.0.1";
    Instant atFrom = Instant.now().minus(30, ChronoUnit.DAYS);
    Instant atTo = Instant.now();
    Long idAfter = null;
    String cursor = null;
    int size = 10;
    String sortField = "at";
    String sortDirection = "desc";

    Page<ChangeLog> page = new PageImpl<>(changeLogs);

//    // 부분 일치를 위해 any() 매처 사용-리포지토리는 부분 일치로 구현
//    when(changeLogRepository.findAllWithFilters(
//        anyString(), eq(type), anyString(), any(InetAddress.class),
//        any(Instant.class), any(Instant.class), eq(idAfter), any(Pageable.class)))
//        .thenReturn(page);
    // Specification 사용으로 변경
    when(changeLogRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    when(changeLogMapper.toDtoList(changeLogs)).thenReturn(changeLogDtos);

    // when
    CursorPageResponse<ChangeLogDto> result = changeLogService.getAllChangeLogs(
        employeeNumber, type, memo, ipAddress, atFrom, atTo,
        idAfter, cursor, size, sortField, sortDirection);

    // then
    assertNotNull(result);
    assertEquals(changeLogDtos, result.content());
    assertEquals(2, result.content().size());
    assertEquals(2, result.totalElements());
    assertFalse(result.hasNext());
  }

  @Test
  void getChangeLogDiffs_목록_반환() {
    // given
    Long id = 1L;

    when(changeLogRepository.findById(id)).thenReturn(Optional.of(changeLog1));
    when(diffMapper.toDtoList(diffEntries)).thenReturn(diffDtos);

    // when
    List<DiffDto> result = changeLogService.getChangeLogDiffs(id);

    // then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("직함", result.get(0).propertyName());
    assertEquals("사원", result.get(0).before());
    assertEquals("대리", result.get(0).after());
  }

  @Test
  void getChangeLogDiffs_찾을수없는_경우_예외발생() {
    // given
    Long id = 999L;

    when(changeLogRepository.findById(id)).thenReturn(Optional.empty());

    // then
    assertThrows(ResourceNotFoundException.class, () -> {
      // when
      changeLogService.getChangeLogDiffs(id);
    });
  }

  @Test
  void getChangeLogsCount_지정된_날짜범위_카운트_반환() {
    // given
    Instant fromDate = Instant.now().minus(7, ChronoUnit.DAYS);
    Instant toDate = Instant.now();
    long expectedCount = 10L;

    when(changeLogRepository.countByDateRangeBoth(eq(fromDate), eq(toDate))).thenReturn(expectedCount);

    // when
    long result = changeLogService.getChangeLogsCount(fromDate, toDate);

    // then
    assertEquals(expectedCount, result);
  }

  @Test
  void getChangeLogsCount_fromDate만_있는_경우() {
    // given
    Instant fromDate = Instant.now().minus(7, ChronoUnit.DAYS);
    long expectedCount = 5L;

    when(changeLogRepository.countByDateRangeFrom(eq(fromDate))).thenReturn(expectedCount);

    // when
    long result = changeLogService.getChangeLogsCount(fromDate, null);

    // then
    assertEquals(expectedCount, result);
  }

  @Test
  void getChangeLogsCount_toDate만_있는_경우() {
    // given
    Instant toDate = Instant.now();
    long expectedCount = 15L;

    when(changeLogRepository.countByDateRangeTo(eq(toDate))).thenReturn(expectedCount);

    // when
    long result = changeLogService.getChangeLogsCount(null, toDate);

    // then
    assertEquals(expectedCount, result);
  }

  @Test
  void getChangeLogsCount_날짜가_null인_경우_기본값_사용() {
    // given
    long expectedCount = 10L;

    when(changeLogRepository.countAll()).thenReturn(expectedCount);

    // when
    long result = changeLogService.getChangeLogsCount(null, null);

    // then
    assertEquals(expectedCount, result);
  }

  @Test
  void getChangeLogsCount_시작일이_종료일보다_이후인_경우_예외발생() {
    // given
    Instant fromDate = Instant.now();
    Instant toDate = Instant.now().minus(1, ChronoUnit.DAYS);

    // then
    assertThrows(IllegalArgumentException.class, () -> {
      // when
      changeLogService.getChangeLogsCount(fromDate, toDate);
    });
  }

  @Test
  void getAllChangeLogs_커서기반_페이징_테스트() throws UnknownHostException {
    // given
    String employeeNumber = "EMP-2025-001";
    HistoryType type = HistoryType.UPDATED;
    String memo = "변경";
//    InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
    Instant atFrom = Instant.now().minus(30, ChronoUnit.DAYS);
    Instant atTo = Instant.now();
    Long idAfter = 1L;
    String cursor = "eyJpZCI6MX0="; // {"id":1} Base64 인코딩
    int size = 10;
    String sortField = "at";
    String sortDirection = "desc";

    Page<ChangeLog> page = new PageImpl<>(List.of(changeLog2));

    // Specification 사용으로 변경
    when(changeLogRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    when(changeLogMapper.toDtoList(List.of(changeLog2)))
        .thenReturn(List.of(changeLogDtos.get(1)));

    // when
    CursorPageResponse<ChangeLogDto> result = changeLogService.getAllChangeLogs(employeeNumber, type, memo, "127.0.0.1", atFrom, atTo,
        null, cursor, size, sortField, sortDirection);

    // then
    assertNotNull(result);
    assertEquals(1, result.content().size());
    assertEquals(changeLogDtos.get(1), result.content().get(0));
    assertEquals(1, result.totalElements());
    assertFalse(result.hasNext());
  }

  @Test
  void getAllChangeLogs_잘못된_정렬필드로_예외발생() throws UnknownHostException {
    // given
    String employeeNumber = "EMP-2025-001";
    HistoryType type = HistoryType.UPDATED;
    String memo = "변경";
    InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
    Instant atFrom = Instant.now().minus(30, ChronoUnit.DAYS);
    Instant atTo = Instant.now();
    Long idAfter = null;
    String cursor = null;
    int size = 10;
    String sortField = "잘못된 정렬필드";
    String sortDirection = "desc";

    // then
    assertThrows(IllegalArgumentException.class, () -> {
      // when
      changeLogService.getAllChangeLogs(
          employeeNumber, type, memo, "127.0.0.1", atFrom, atTo,
          idAfter, cursor, size, sortField, sortDirection);
    });
  }
}