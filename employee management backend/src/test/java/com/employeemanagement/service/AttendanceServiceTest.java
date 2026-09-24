package com.employeemanagement.service;

import com.employeemanagement.dto.request.AttendanceRequest;
import com.employeemanagement.dto.response.AttendanceResponse;
import com.employeemanagement.entity.Attendance;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.enums.AttendanceStatus;
import com.employeemanagement.exception.DuplicateResourceException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.AttendanceMapper;
import com.employeemanagement.repository.AttendanceRepository;
import com.employeemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AttendanceMapper attendanceMapper;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private AttendanceRequest request;

    @BeforeEach
    void setUp() {
        employee = Employee.builder().id(1L).firstName("John").lastName("Doe").employeeCode("EMP1001").build();

        request = new AttendanceRequest();
        request.setEmployeeId(1L);
        request.setAttendanceDate(LocalDate.now());
        request.setCheckIn(LocalTime.of(9, 0));
        request.setCheckOut(LocalTime.of(18, 0));
        request.setStatus(AttendanceStatus.PRESENT);
        request.setRemarks("On time");
    }

    @Test
    void markAttendance_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.existsByEmployee_IdAndAttendanceDate(1L, request.getAttendanceDate())).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceMapper.toResponse(any(Attendance.class))).thenReturn(
                AttendanceResponse.builder().id(1L).employeeId(1L).status(AttendanceStatus.PRESENT).build());

        AttendanceResponse response = attendanceService.markAttendance(request);

        assertThat(response.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void markAttendance_duplicateForSameDay_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.existsByEmployee_IdAndAttendanceDate(1L, request.getAttendanceDate())).thenReturn(true);

        assertThatThrownBy(() -> attendanceService.markAttendance(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already recorded");

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void markAttendance_employeeNotFound_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.markAttendance(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAttendance_notFound_throwsException() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.updateAttendance(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMonthlyAttendancePercentage_noRecords_returnsZero() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        when(attendanceRepository.countByEmployee_IdAndAttendanceDateBetween(eq(1L), any(), any())).thenReturn(0L);

        double pct = attendanceService.getMonthlyAttendancePercentage(1L, start.getYear(), start.getMonthValue());

        assertThat(pct).isZero();
    }

    private static Long eq(long value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
