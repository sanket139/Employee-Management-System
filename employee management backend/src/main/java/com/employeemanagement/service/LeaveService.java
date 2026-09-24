package com.employeemanagement.service;

import com.employeemanagement.dto.request.LeaveRequestDto;
import com.employeemanagement.dto.response.LeaveResponse;
import com.employeemanagement.dto.response.PageResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.LeaveRequest;
import com.employeemanagement.entity.enums.LeaveStatus;
import com.employeemanagement.entity.enums.NotificationType;
import com.employeemanagement.exception.BadRequestException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.LeaveMapper;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;
    private final NotificationService notificationService;

    @Transactional
    public LeaveResponse applyLeave(Long employeeId, LeaveRequestDto request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date.");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot apply for leave in the past.");
        }

        long overlapping = leaveRequestRepository.countOverlapping(employeeId, request.getStartDate(), request.getEndDate());
        if (overlapping > 0) {
            throw new BadRequestException("You already have a pending or approved leave request that overlaps with these dates.");
        }

        long numberOfDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        LeaveRequest leave = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .numberOfDays(numberOfDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leave);

        notificationService.notifyAllHrAndAdmins(
                employee.getFirstName() + " " + employee.getLastName() + " applied for " + numberOfDays
                        + " day(s) of " + request.getLeaveType() + " leave.",
                NotificationType.LEAVE_APPLIED);

        return leaveMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaves(Long employeeId) {
        return leaveRequestRepository.findByEmployee_IdOrderByAppliedDateDesc(employeeId).stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveResponse getById(Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        return leaveMapper.toResponse(leave);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> getPending(Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.findByStatus(LeaveStatus.PENDING, pageable);
        return PageResponse.of(page.map(leaveMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> getAll(Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.findAll(pageable);
        return PageResponse.of(page.map(leaveMapper::toResponse));
    }

    @Transactional
    public LeaveResponse approve(Long id, String approverUsername) {
        LeaveRequest leave = getEditablePending(id);
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approverUsername);
        LeaveRequest saved = leaveRequestRepository.save(leave);

        if (leave.getEmployee().getUser() != null) {
            notificationService.notifyUser(leave.getEmployee().getUser().getId(),
                    "Your " + leave.getLeaveType() + " leave from " + leave.getStartDate() + " to "
                            + leave.getEndDate() + " has been approved.",
                    NotificationType.LEAVE_APPROVED);
        }

        return leaveMapper.toResponse(saved);
    }

    @Transactional
    public LeaveResponse reject(Long id, String approverUsername) {
        LeaveRequest leave = getEditablePending(id);
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(approverUsername);
        LeaveRequest saved = leaveRequestRepository.save(leave);

        if (leave.getEmployee().getUser() != null) {
            notificationService.notifyUser(leave.getEmployee().getUser().getId(),
                    "Your " + leave.getLeaveType() + " leave from " + leave.getStartDate() + " to "
                            + leave.getEndDate() + " has been rejected.",
                    NotificationType.LEAVE_REJECTED);
        }

        return leaveMapper.toResponse(saved);
    }

    private LeaveRequest getEditablePending(Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be approved or rejected. Current status: "
                    + leave.getStatus());
        }
        return leave;
    }

    @Transactional(readOnly = true)
    public long countByStatus(LeaveStatus status) {
        return leaveRequestRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByEmployeeAndStatus(Long employeeId, LeaveStatus status) {
        return leaveRequestRepository.countByEmployee_IdAndStatus(employeeId, status);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getEmployeesOnLeaveToday() {
        return leaveRequestRepository.findEmployeesOnLeave(LocalDate.now());
    }
}
