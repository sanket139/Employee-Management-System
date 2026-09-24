package com.employeemanagement.dto.response;

import com.employeemanagement.entity.enums.LeaveStatus;
import com.employeemanagement.entity.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long numberOfDays;
    private String reason;
    private LeaveStatus status;
    private LocalDateTime appliedDate;
    private String approvedBy;
}
