package com.employeemanagement.dto.response;

import com.employeemanagement.entity.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String gender;
    private LocalDate joiningDate;
    private String designation;
    private BigDecimal salary;
    private Long departmentId;
    private String departmentName;
    private EmployeeStatus status;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
