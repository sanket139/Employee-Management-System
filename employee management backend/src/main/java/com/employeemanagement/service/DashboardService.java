package com.employeemanagement.service;

import com.employeemanagement.dto.response.AdminDashboardResponse;
import com.employeemanagement.dto.response.EmployeeDashboardResponse;
import com.employeemanagement.dto.response.HrDashboardResponse;
import com.employeemanagement.dto.response.LeaveResponse;
import com.employeemanagement.entity.Department;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.enums.AttendanceStatus;
import com.employeemanagement.entity.enums.EmployeeStatus;
import com.employeemanagement.entity.enums.LeaveStatus;
import com.employeemanagement.mapper.EmployeeMapper;
import com.employeemanagement.mapper.LeaveMapper;
import com.employeemanagement.repository.DepartmentRepository;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final PayrollService payrollService;
    private final NotificationService notificationService;
    private final EmployeeMapper employeeMapper;
    private final LeaveMapper leaveMapper;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        LocalDate today = LocalDate.now();
        long total = employeeRepository.count();
        long active = employeeService.countByStatus(EmployeeStatus.ACTIVE);
        long inactive = employeeService.countByStatus(EmployeeStatus.INACTIVE);
        long totalDepartments = departmentService.countDepartments();
        long todaysPresent = attendanceService.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        long todaysAbsent = attendanceService.countByDateAndStatus(today, AttendanceStatus.ABSENT);
        long onLeave = leaveService.getEmployeesOnLeaveToday().size();
        long pendingLeaves = leaveService.countByStatus(LeaveStatus.PENDING);
        BigDecimal monthlyPayroll = payrollService.getMonthlyPayrollTotal(today.getMonthValue(), today.getYear());

        List<Employee> recentEmployees = employeeRepository.findTop5ByOrderByCreatedAtDesc();
        List<LeaveResponse> recentLeaves = leaveRequestRepository.findAll(PageRequest.of(0, 5,
                        org.springframework.data.domain.Sort.by("appliedDate").descending()))
                .stream().map(leaveMapper::toResponse).toList();

        Map<String, Long> departmentWiseCount = new LinkedHashMap<>();
        for (Department d : departmentRepository.findAll()) {
            departmentWiseCount.put(d.getDepartmentName(), employeeRepository.countByDepartment_Id(d.getId()));
        }

        return AdminDashboardResponse.builder()
                .totalEmployees(total)
                .activeEmployees(active)
                .inactiveEmployees(inactive)
                .totalDepartments(totalDepartments)
                .todaysPresent(todaysPresent)
                .todaysAbsent(todaysAbsent)
                .employeesOnLeave(onLeave)
                .pendingLeaveRequests(pendingLeaves)
                .monthlyPayroll(monthlyPayroll)
                .recentEmployees(recentEmployees.stream().map(employeeMapper::toResponse).toList())
                .recentLeaveRequests(recentLeaves)
                .departmentWiseCount(departmentWiseCount)
                .build();
    }

    @Transactional(readOnly = true)
    public HrDashboardResponse getHrDashboard() {
        LocalDate today = LocalDate.now();
        long total = employeeRepository.count();
        long active = employeeService.countByStatus(EmployeeStatus.ACTIVE);
        long todaysAttendance = attendanceService.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        long pendingLeaves = leaveService.countByStatus(LeaveStatus.PENDING);
        long onLeave = leaveService.getEmployeesOnLeaveToday().size();

        Map<String, Long> departmentWiseCount = new LinkedHashMap<>();
        for (Department d : departmentRepository.findAll()) {
            departmentWiseCount.put(d.getDepartmentName(), employeeRepository.countByDepartment_Id(d.getId()));
        }

        return HrDashboardResponse.builder()
                .totalEmployees(total)
                .activeEmployees(active)
                .todaysAttendance(todaysAttendance)
                .pendingLeaveRequests(pendingLeaves)
                .employeesOnLeave(onLeave)
                .departmentWiseCount(departmentWiseCount)
                .build();
    }

    @Transactional(readOnly = true)
    public EmployeeDashboardResponse getEmployeeDashboard(Long userId) {
        Employee employee = employeeService.getEmployeeEntityByUserId(userId);
        LocalDate today = LocalDate.now();

        double attendancePct = attendanceService.getMonthlyAttendancePercentage(
                employee.getId(), today.getYear(), today.getMonthValue());

        return EmployeeDashboardResponse.builder()
                .profile(employeeMapper.toResponse(employee))
                .todaysAttendanceStatus(attendanceService.getTodaysStatus(employee.getId()))
                .monthlyAttendancePercentage(attendancePct)
                .pendingLeaves(leaveService.countByEmployeeAndStatus(employee.getId(), LeaveStatus.PENDING))
                .approvedLeaves(leaveService.countByEmployeeAndStatus(employee.getId(), LeaveStatus.APPROVED))
                .rejectedLeaves(leaveService.countByEmployeeAndStatus(employee.getId(), LeaveStatus.REJECTED))
                .latestPayroll(payrollService.getLatestForEmployee(employee.getId()))
                .unreadNotifications(notificationService.getUnreadCount(userId))
                .build();
    }
}
