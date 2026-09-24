package com.employeemanagement.controller;

import com.employeemanagement.dto.request.AttendanceRequest;
import com.employeemanagement.dto.response.AttendanceResponse;
import com.employeemanagement.dto.response.PageResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.enums.AttendanceStatus;
import org.springframework.security.access.AccessDeniedException;
import com.employeemanagement.security.UserPrincipal;
import com.employeemanagement.service.AttendanceService;
import com.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<AttendanceResponse> mark(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.markAttendance(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<AttendanceResponse> update(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<AttendanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PageResponse<AttendanceResponse>> search(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        return ResponseEntity.ok(attendanceService.search(employeeId, date, status, pageable));
    }

    /** Employee viewing their own attendance history. */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponse>> getByEmployee(@PathVariable Long employeeId,
                                                                    @AuthenticationPrincipal UserPrincipal principal) {
        assertOwnerOrStaff(employeeId, principal);
        return ResponseEntity.ok(attendanceService.getByEmployee(employeeId));
    }

    @GetMapping("/employee/{employeeId}/percentage")
    public ResponseEntity<Double> getMonthlyPercentage(@PathVariable Long employeeId,
                                                        @RequestParam int year,
                                                        @RequestParam int month,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        assertOwnerOrStaff(employeeId, principal);
        return ResponseEntity.ok(attendanceService.getMonthlyAttendancePercentage(employeeId, year, month));
    }

    private void assertOwnerOrStaff(Long employeeId, UserPrincipal principal) {
        if (principal.getRole().equals("ADMIN") || principal.getRole().equals("HR")) {
            return;
        }
        Employee employee = employeeService.getEmployeeEntity(employeeId);
        if (employee.getUser() == null || !employee.getUser().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You can only view your own attendance records.");
        }
    }
}
