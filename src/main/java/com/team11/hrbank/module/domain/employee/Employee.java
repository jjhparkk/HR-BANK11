package com.team11.hrbank.module.domain.employee;

import com.team11.hrbank.module.domain.UpdatableEntity;
import com.team11.hrbank.module.domain.department.Department;
import com.team11.hrbank.module.domain.file.File;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "employees")
public class Employee extends UpdatableEntity {

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "employee_number", nullable = false, length = 25)
  private String employeeNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @Column(name = "\"position\"", nullable = false, length = 50)
  private String position;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_image_id")
  private File profileImage;

  @ColumnDefault("'ACTIVE'")
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private EmployeeStatus status;

  public Employee() {

  }

  @Builder
  public Employee(String name, String email, String employeeNumber, Department department,
      String position, LocalDate hireDate, File profileImage, EmployeeStatus status) {
    this.name = name;
    this.email = email;
    this.employeeNumber = employeeNumber;
    this.department = department;
    this.position = position;
    this.hireDate = hireDate;
    this.profileImage = profileImage;
    this.status = status;
  }

  // update 메서드 추가
  public void updateName(String name) {
    this.name = name;
  }

  public void updateEmail(String email) {
    this.email = email;
  }

  public void updateDepartment(Department department) {
    this.department = department;
  }

  public void updatePosition(String position) {
    this.position = position;
  }

  public void updateHireDate(LocalDate hireDate) {
    this.hireDate = hireDate;
  }

  public void updateProfileImage(File profileImage) {
    this.profileImage = profileImage;
  }

  public void updateStatus(EmployeeStatus status) {
    this.status = status;
  }
}
