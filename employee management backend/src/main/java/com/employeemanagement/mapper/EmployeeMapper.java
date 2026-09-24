package com.employeemanagement.mapper;

import com.employeemanagement.dto.response.EmployeeResponse;
import com.employeemanagement.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeCode())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .address(e.getAddress())
                .dateOfBirth(e.getDateOfBirth())
                .gender(e.getGender())
                .joiningDate(e.getJoiningDate())
                .designation(e.getDesignation())
                .salary(e.getSalary())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getDepartmentName() : null)
                .status(e.getStatus())
                .username(e.getUser() != null ? e.getUser().getUsername() : null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
