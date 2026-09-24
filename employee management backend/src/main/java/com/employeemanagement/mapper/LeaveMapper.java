package com.employeemanagement.mapper;

import com.employeemanagement.dto.response.LeaveResponse;
import com.employeemanagement.entity.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public LeaveResponse toResponse(LeaveRequest l) {
        return LeaveResponse.builder()
                .id(l.getId())
                .employeeId(l.getEmployee().getId())
                .employeeName(l.getEmployee().getFirstName() + " " + l.getEmployee().getLastName())
                .leaveType(l.getLeaveType())
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .numberOfDays(l.getNumberOfDays())
                .reason(l.getReason())
                .status(l.getStatus())
                .appliedDate(l.getAppliedDate())
                .approvedBy(l.getApprovedBy())
                .build();
    }
}
