package com.employeemanagement.service;

import com.employeemanagement.dto.request.EmployeeRequest;
import com.employeemanagement.dto.response.EmployeeResponse;
import com.employeemanagement.entity.Department;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.User;
import com.employeemanagement.entity.enums.EmployeeStatus;
import com.employeemanagement.entity.enums.Role;
import com.employeemanagement.exception.DuplicateResourceException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.EmployeeMapper;
import com.employeemanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private PayrollRepository payrollRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Department department;
    private EmployeeRequest request;
    private Employee employee;

    @BeforeEach
    void setUp() {
        department = Department.builder().id(1L).departmentName("IT").build();

        request = new EmployeeRequest();
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setEmail("alice@example.com");
        request.setPhone("9999999999");
        request.setDateOfBirth(LocalDate.of(1995, 1, 1));
        request.setGender("Female");
        request.setJoiningDate(LocalDate.now());
        request.setDesignation("Developer");
        request.setSalary(new BigDecimal("40000"));
        request.setDepartmentId(1L);

        employee = Employee.builder()
                .id(1L).employeeCode("EMP1001")
                .firstName("Alice").lastName("Brown")
                .email("alice@example.com")
                .department(department)
                .status(EmployeeStatus.ACTIVE)
                .build();
    }

    @Test
    void createEmployee_success_generatesEmployeeCode() {
        when(employeeRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.count()).thenReturn(0L);
        when(employeeRepository.existsByEmployeeCode(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(
                EmployeeResponse.builder().id(1L).employeeCode("EMP1001").build());

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response.getEmployeeCode()).isEqualTo("EMP1001");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_duplicateEmail_throwsException() {
        when(employeeRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_departmentNotFound_throwsException() {
        when(employeeRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getEmployeeById_notFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void updateEmployee_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(
                EmployeeResponse.builder().id(1L).firstName("Alice").build());

        request.setFirstName("Alicia");
        EmployeeResponse response = employeeService.updateEmployee(1L, request);

        assertThat(response).isNotNull();
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void setStatus_deactivate_updatesStatus() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(
                EmployeeResponse.builder().id(1L).status(EmployeeStatus.INACTIVE).build());

        EmployeeResponse response = employeeService.setStatus(1L, EmployeeStatus.INACTIVE);

        assertThat(response.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
    }

    @Test
    void deleteEmployee_withNoLinkedUser_deletesDependentRecordsAndEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployee_IdOrderByAttendanceDateDesc(1L)).thenReturn(Collections.emptyList());
        when(leaveRequestRepository.findByEmployee_IdOrderByAppliedDateDesc(1L)).thenReturn(Collections.emptyList());
        when(payrollRepository.findByEmployee_IdOrderByYearDescMonthDesc(1L)).thenReturn(Collections.emptyList());

        employeeService.deleteEmployee(1L);

        verify(attendanceRepository).deleteAll(Collections.emptyList());
        verify(leaveRequestRepository).deleteAll(Collections.emptyList());
        verify(payrollRepository).deleteAll(Collections.emptyList());
        verify(userRepository, never()).delete(any());
        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_withLinkedUser_deletesUserAndNotificationsToo() {
        User user = User.builder().id(5L).username("alice.brown").role(Role.EMPLOYEE).build();
        employee.setUser(user);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployee_IdOrderByAttendanceDateDesc(1L)).thenReturn(Collections.emptyList());
        when(leaveRequestRepository.findByEmployee_IdOrderByAppliedDateDesc(1L)).thenReturn(Collections.emptyList());
        when(payrollRepository.findByEmployee_IdOrderByYearDescMonthDesc(1L)).thenReturn(Collections.emptyList());
        when(notificationRepository.findByUser_IdOrderByCreatedAtDesc(5L)).thenReturn(Collections.emptyList());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        employeeService.deleteEmployee(1L);

        verify(notificationRepository).deleteAll(Collections.emptyList());
        verify(userRepository).delete(user);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_notFound_throwsException() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(404L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).delete(any());
    }
}
