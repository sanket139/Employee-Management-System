package com.employeemanagement.mapper;

import com.employeemanagement.dto.response.AttendanceResponse;
import com.employeemanagement.entity.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeName(a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName())
                .employeeCode(a.getEmployee().getEmployeeCode())
                .attendanceDate(a.getAttendanceDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())
                .status(a.getStatus())
                .remarks(a.getRemarks())
                .build();
    }
}
