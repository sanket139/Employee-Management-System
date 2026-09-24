package com.employeemanagement.controller;

import com.employeemanagement.dto.request.PayrollRequest;
import com.employeemanagement.dto.response.PageResponse;
import com.employeemanagement.dto.response.PayrollResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.security.UserPrincipal;
import com.employeemanagement.service.EmployeeService;
import com.employeemanagement.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PayrollResponse> create(@Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.createPayroll(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PayrollResponse> update(@PathVariable Long id, @Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.ok(payrollService.updatePayroll(id, request));
    }

    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PayrollResponse> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.markAsPaid(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollResponse> getById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        PayrollResponse response = payrollService.getById(id);
        assertOwnerOrStaff(response.getEmployeeId(), principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PageResponse<PayrollResponse>> search(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("year").descending().and(Sort.by("month").descending()));
        return ResponseEntity.ok(payrollService.search(employeeId, month, year, pageable));
    }

    /** Employees can view only their own payroll history. */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollResponse>> getByEmployee(@PathVariable Long employeeId,
                                                                @AuthenticationPrincipal UserPrincipal principal) {
        assertOwnerOrStaff(employeeId, principal);
        return ResponseEntity.ok(payrollService.getByEmployee(employeeId));
    }

    private void assertOwnerOrStaff(Long employeeId, UserPrincipal principal) {
        if (principal.getRole().equals("ADMIN") || principal.getRole().equals("HR")) {
            return;
        }
        Employee employee = employeeService.getEmployeeEntity(employeeId);
        if (employee.getUser() == null || !employee.getUser().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You can only view your own payroll records.");
        }
    }
}
