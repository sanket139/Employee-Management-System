package com.employeemanagement.repository;

import com.employeemanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentNameIgnoreCase(String departmentName);
    boolean existsByDepartmentNameIgnoreCase(String departmentName);
}
