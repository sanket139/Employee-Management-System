package com.employeemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;
    private long totalDepartments;
    private long todaysPresent;
    private long todaysAbsent;
    private long employeesOnLeave;
    private long pendingLeaveRequests;
    private BigDecimal monthlyPayroll;
    private List<EmployeeResponse> recentEmployees;
    private List<LeaveResponse> recentLeaveRequests;
    private Map<String, Long> departmentWiseCount;
}
