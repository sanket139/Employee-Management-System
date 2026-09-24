package com.employeemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class HrDashboardResponse {
    private long totalEmployees;
    private long activeEmployees;
    private long todaysAttendance;
    private long pendingLeaveRequests;
    private long employeesOnLeave;
    private Map<String, Long> departmentWiseCount;
}
