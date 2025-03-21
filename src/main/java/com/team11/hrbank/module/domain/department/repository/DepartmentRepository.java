package com.team11.hrbank.module.domain.department.repository;

import com.team11.hrbank.module.domain.department.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
  boolean existsByName(String name);

  // 커서 기반 페이지네이션 (ID 이후) - 오름차순
  @Query("SELECT d FROM Department d WHERE d.id > :id ORDER BY d.establishedDate ASC, d.name ASC")
  Page<Department> findAllWithCursorAsc(@Param("id") Long id, Pageable pageable);

  // 커서 기반 페이지네이션 (ID 이전) - 내림차순
  @Query("SELECT d FROM Department d WHERE d.id < :id ORDER BY d.establishedDate DESC, d.name DESC")
  Page<Department> findAllWithCursorDesc(@Param("id") Long id, Pageable pageable);

  // 이름 또는 설명으로 검색 + 페이지네이션 지원
  @Query("SELECT d FROM Department d WHERE " +
      "(LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<Department> searchByNameOrDescription(
      @Param("search") String search, Pageable pageable);

  // 오름차순 + 커서 + 검색어 (이름 또는 설명)
  @Query("SELECT d FROM Department d WHERE " +
      "d.id > :id AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
      "ORDER BY d.establishedDate ASC, d.name ASC")
  Page<Department> searchWithCursorAsc(
      @Param("id") Long id, @Param("search") String search, Pageable pageable);

  // 내림차순 + 커서 + 검색어 (이름 또는 설명)
  @Query("SELECT d FROM Department d WHERE " +
      "d.id < :id AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
      "ORDER BY d.establishedDate DESC, d.name DESC")
  Page<Department> searchWithCursorDesc(
      @Param("id") Long id, @Param("search") String search, Pageable pageable);
}