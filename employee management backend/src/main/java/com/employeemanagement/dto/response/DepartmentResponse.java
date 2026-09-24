package com.employeemanagement.dto.response;

import com.employeemanagement.entity.enums.DepartmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String departmentName;
    private String description;
    private DepartmentStatus status;
    private long employeeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
