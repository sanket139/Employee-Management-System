package com.employeemanagement.repository;

import com.employeemanagement.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    boolean existsByEmployee_IdAndMonthAndYear(Long employeeId, Integer month, Integer year);

    Optional<Payroll> findByEmployee_IdAndMonthAndYear(Long employeeId, Integer month, Integer year);

    List<Payroll> findByEmployee_IdOrderByYearDescMonthDesc(Long employeeId);

    @Query("""
            SELECT p FROM Payroll p
            WHERE (:employeeId IS NULL OR p.employee.id = :employeeId)
              AND (:month IS NULL OR p.month = :month)
              AND (:year IS NULL OR p.year = :year)
            ORDER BY p.year DESC, p.month DESC
            """)
    Page<Payroll> search(@Param("employeeId") Long employeeId,
                          @Param("month") Integer month,
                          @Param("year") Integer year,
                          Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p WHERE p.month = :month AND p.year = :year")
    BigDecimal sumNetSalaryForMonth(@Param("month") Integer month, @Param("year") Integer year);
}
