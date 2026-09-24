package com.employeemanagement.controller;

import com.employeemanagement.dto.request.LeaveRequestDto;
import com.employeemanagement.dto.response.LeaveResponse;
import com.employeemanagement.dto.response.PageResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.security.UserPrincipal;
import com.employeemanagement.service.EmployeeService;
import com.employeemanagement.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<LeaveResponse> apply(@Valid @RequestBody LeaveRequestDto request,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        Employee employee = employeeService.getEmployeeEntityByUserId(principal.getId());
        employeeService.assertEmployeeActive(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.applyLeave(employee.getId(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveResponse>> myLeaves(@AuthenticationPrincipal UserPrincipal principal) {
        Employee employee = employeeService.getEmployeeEntityByUserId(principal.getId());
        return ResponseEntity.ok(leaveService.getMyLeaves(employee.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PageResponse<LeaveResponse>> getAll(
            @RequestParam(required = false) Boolean pendingOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (Boolean.TRUE.equals(pendingOnly)) {
            return ResponseEntity.ok(leaveService.getPending(pageable));
        }
        return ResponseEntity.ok(leaveService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<LeaveResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getById(id));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<LeaveResponse> approve(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(leaveService.approve(id, principal.getUsername()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<LeaveResponse> reject(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(leaveService.reject(id, principal.getUsername()));
    }
}
