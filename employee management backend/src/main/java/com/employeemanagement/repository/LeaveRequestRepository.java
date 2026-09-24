package com.employeemanagement.repository;

import com.employeemanagement.entity.LeaveRequest;
import com.employeemanagement.entity.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee_IdOrderByAppliedDateDesc(Long employeeId);

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    long countByStatus(LeaveStatus status);

    long countByEmployee_IdAndStatus(Long employeeId, LeaveStatus status);

    @Query("""
            SELECT COUNT(l) FROM LeaveRequest l
            WHERE l.employee.id = :employeeId
              AND l.status <> 'REJECTED'
              AND l.startDate <= :endDate
              AND l.endDate >= :startDate
            """)
    long countOverlapping(@Param("employeeId") Long employeeId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT l FROM LeaveRequest l
            WHERE l.employee.department.id IS NOT NULL
              AND l.status = 'APPROVED'
              AND :date BETWEEN l.startDate AND l.endDate
            """)
    List<LeaveRequest> findEmployeesOnLeave(@Param("date") LocalDate date);
}
