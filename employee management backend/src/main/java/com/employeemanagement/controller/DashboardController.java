package com.employeemanagement.controller;

import com.employeemanagement.dto.response.AdminDashboardResponse;
import com.employeemanagement.dto.response.EmployeeDashboardResponse;
import com.employeemanagement.dto.response.HrDashboardResponse;
import com.employeemanagement.security.UserPrincipal;
import com.employeemanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> admin() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/hr")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<HrDashboardResponse> hr() {
        return ResponseEntity.ok(dashboardService.getHrDashboard());
    }

    @GetMapping("/employee")
    public ResponseEntity<EmployeeDashboardResponse> employee(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getEmployeeDashboard(principal.getId()));
    }
}
