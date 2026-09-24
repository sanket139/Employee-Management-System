package com.employeemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EmployeeDashboardResponse {
    private EmployeeResponse profile;
    private String todaysAttendanceStatus;
    private double monthlyAttendancePercentage;
    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;
    private PayrollResponse latestPayroll;
    private long unreadNotifications;
}
