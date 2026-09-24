package com.employeemanagement.repository;

import com.employeemanagement.entity.Attendance;
import com.employeemanagement.entity.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployee_IdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);

    boolean existsByEmployee_IdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);

    List<Attendance> findByEmployee_IdOrderByAttendanceDateDesc(Long employeeId);

    List<Attendance> findByEmployee_IdAndAttendanceDateBetween(Long employeeId, LocalDate start, LocalDate end);

    long countByAttendanceDateAndStatus(LocalDate date, AttendanceStatus status);

    long countByEmployee_IdAndAttendanceDateBetweenAndStatus(Long employeeId, LocalDate start, LocalDate end, AttendanceStatus status);

    long countByEmployee_IdAndAttendanceDateBetween(Long employeeId, LocalDate start, LocalDate end);

    @Query("""
            SELECT a FROM Attendance a
            WHERE (:employeeId IS NULL OR a.employee.id = :employeeId)
              AND (:date IS NULL OR a.attendanceDate = :date)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.attendanceDate DESC
            """)
    Page<Attendance> search(@Param("employeeId") Long employeeId,
                             @Param("date") LocalDate date,
                             @Param("status") AttendanceStatus status,
                             Pageable pageable);
}
