package com.employeemanagement.service;

import com.employeemanagement.dto.request.AttendanceRequest;
import com.employeemanagement.dto.response.AttendanceResponse;
import com.employeemanagement.dto.response.PageResponse;
import com.employeemanagement.entity.Attendance;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.enums.AttendanceStatus;
import com.employeemanagement.exception.BadRequestException;
import com.employeemanagement.exception.DuplicateResourceException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.AttendanceMapper;
import com.employeemanagement.repository.AttendanceRepository;
import com.employeemanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        if (attendanceRepository.existsByEmployee_IdAndAttendanceDate(request.getEmployeeId(), request.getAttendanceDate())) {
            throw new DuplicateResourceException("Attendance already recorded for this employee on "
                    + request.getAttendanceDate());
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(request.getAttendanceDate())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toResponse(saved);
    }

    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));

        attendance.setCheckIn(request.getCheckIn());
        attendance.setCheckOut(request.getCheckOut());
        attendance.setStatus(request.getStatus());
        attendance.setRemarks(request.getRemarks());

        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
        return attendanceMapper.toResponse(attendance);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> search(Long employeeId, LocalDate date, AttendanceStatus status, Pageable pageable) {
        Page<Attendance> page = attendanceRepository.search(employeeId, date, status, pageable);
        return PageResponse.of(page.map(attendanceMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployee_IdOrderByAttendanceDateDesc(employeeId).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public double getMonthlyAttendancePercentage(Long employeeId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        long totalMarked = attendanceRepository.countByEmployee_IdAndAttendanceDateBetween(employeeId, start, end);
        if (totalMarked == 0) {
            return 0.0;
        }
        long present = attendanceRepository.countByEmployee_IdAndAttendanceDateBetweenAndStatus(
                employeeId, start, end, AttendanceStatus.PRESENT);
        long halfDay = attendanceRepository.countByEmployee_IdAndAttendanceDateBetweenAndStatus(
                employeeId, start, end, AttendanceStatus.HALF_DAY);
        double effectivePresent = present + (halfDay * 0.5);
        return Math.round((effectivePresent / totalMarked) * 10000.0) / 100.0;
    }

    @Transactional(readOnly = true)
    public long countByDateAndStatus(LocalDate date, AttendanceStatus status) {
        return attendanceRepository.countByAttendanceDateAndStatus(date, status);
    }

    @Transactional(readOnly = true)
    public String getTodaysStatus(Long employeeId) {
        return attendanceRepository.findByEmployee_IdAndAttendanceDate(employeeId, LocalDate.now())
                .map(a -> a.getStatus().name())
                .orElse("NOT_MARKED");
    }
}
