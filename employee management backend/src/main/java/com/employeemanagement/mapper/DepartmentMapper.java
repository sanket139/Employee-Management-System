package com.employeemanagement.mapper;

import com.employeemanagement.dto.response.DepartmentResponse;
import com.employeemanagement.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department d, long employeeCount) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .departmentName(d.getDepartmentName())
                .description(d.getDescription())
                .status(d.getStatus())
                .employeeCount(employeeCount)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
