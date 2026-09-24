package com.employeemanagement.repository;

import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByUser_Id(Long userId);

    long countByStatus(EmployeeStatus status);

    long countByDepartment_Id(Long departmentId);

    List<Employee> findByDepartment_Id(Long departmentId);

    List<Employee> findTop5ByOrderByCreatedAtDesc();

    @Query("""
            SELECT e FROM Employee e
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:departmentId IS NULL OR e.department.id = :departmentId)
              AND (:designation IS NULL OR :designation = '' OR LOWER(e.designation) = LOWER(:designation))
              AND (:status IS NULL OR e.status = :status)
            """)
    Page<Employee> search(@Param("search") String search,
                           @Param("departmentId") Long departmentId,
                           @Param("designation") String designation,
                           @Param("status") EmployeeStatus status,
                           Pageable pageable);
}
