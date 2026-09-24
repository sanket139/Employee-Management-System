package com.employeemanagement.service;

import com.employeemanagement.dto.request.LeaveRequestDto;
import com.employeemanagement.dto.response.LeaveResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.LeaveRequest;
import com.employeemanagement.entity.enums.LeaveStatus;
import com.employeemanagement.entity.enums.LeaveType;
import com.employeemanagement.exception.BadRequestException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.LeaveMapper;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private LeaveMapper leaveMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;
    private LeaveRequestDto requestDto;

    @BeforeEach
    void setUp() {
        employee = Employee.builder().id(1L).firstName("John").lastName("Doe").build();

        requestDto = new LeaveRequestDto();
        requestDto.setLeaveType(LeaveType.CASUAL);
        requestDto.setStartDate(LocalDate.now().plusDays(1));
        requestDto.setEndDate(LocalDate.now().plusDays(2));
        requestDto.setReason("Personal work");
    }

    @Test
    void applyLeave_success_calculatesDaysAndNotifies() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.countOverlapping(eq(1L), any(), any())).thenReturn(0L);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveMapper.toResponse(any(LeaveRequest.class))).thenReturn(
                LeaveResponse.builder().id(1L).numberOfDays(2L).status(LeaveStatus.PENDING).build());

        LeaveResponse response = leaveService.applyLeave(1L, requestDto);

        assertThat(response.getNumberOfDays()).isEqualTo(2L);
        verify(notificationService).notifyAllHrAndAdmins(anyString(), any());
        verify(leaveRequestRepository).save(argThat(l -> l.getNumberOfDays() == 2L));
    }

    @Test
    void applyLeave_endDateBeforeStartDate_throwsException() {
        requestDto.setEndDate(requestDto.getStartDate().minusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> leaveService.applyLeave(1L, requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End date cannot be before start date");

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void applyLeave_overlappingRequest_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.countOverlapping(eq(1L), any(), any())).thenReturn(1L);

        assertThatThrownBy(() -> leaveService.applyLeave(1L, requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("overlap");

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void applyLeave_pastStartDate_throwsException() {
        requestDto.setStartDate(LocalDate.now().minusDays(1));
        requestDto.setEndDate(LocalDate.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> leaveService.applyLeave(1L, requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("past");
    }

    @Test
    void approve_pendingRequest_updatesStatusAndNotifies() {
        LeaveRequest leave = LeaveRequest.builder()
                .id(1L).employee(employee).status(LeaveStatus.PENDING)
                .leaveType(LeaveType.CASUAL)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1))
                .build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveMapper.toResponse(any(LeaveRequest.class))).thenReturn(
                LeaveResponse.builder().id(1L).status(LeaveStatus.APPROVED).build());

        LeaveResponse response = leaveService.approve(1L, "hr_manager");

        assertThat(response.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(leave.getApprovedBy()).isEqualTo("hr_manager");
    }

    @Test
    void approve_alreadyProcessedRequest_throwsException() {
        LeaveRequest leave = LeaveRequest.builder()
                .id(1L).employee(employee).status(LeaveStatus.APPROVED).build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leave));

        assertThatThrownBy(() -> leaveService.approve(1L, "hr_manager"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only pending");

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void reject_pendingRequest_updatesStatus() {
        LeaveRequest leave = LeaveRequest.builder()
                .id(1L).employee(employee).status(LeaveStatus.PENDING)
                .leaveType(LeaveType.SICK)
                .startDate(LocalDate.now()).endDate(LocalDate.now())
                .build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveMapper.toResponse(any(LeaveRequest.class))).thenReturn(
                LeaveResponse.builder().id(1L).status(LeaveStatus.REJECTED).build());

        LeaveResponse response = leaveService.reject(1L, "hr_manager");

        assertThat(response.getStatus()).isEqualTo(LeaveStatus.REJECTED);
    }

    @Test
    void applyLeave_employeeNotFound_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.applyLeave(1L, requestDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Long eq(long value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }

    private static String anyString() {
        return org.mockito.ArgumentMatchers.anyString();
    }
}
