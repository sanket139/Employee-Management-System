package com.employeemanagement.service;

import com.employeemanagement.dto.request.EmployeeRequest;
import com.employeemanagement.dto.response.EmployeeResponse;
import com.employeemanagement.dto.response.PageResponse;
import com.employeemanagement.entity.Attendance;
import com.employeemanagement.entity.Department;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.LeaveRequest;
import com.employeemanagement.entity.Notification;
import com.employeemanagement.entity.Payroll;
import com.employeemanagement.entity.User;
import com.employeemanagement.entity.enums.EmployeeStatus;
import com.employeemanagement.entity.enums.Role;
import com.employeemanagement.exception.BadRequestException;
import com.employeemanagement.exception.DuplicateResourceException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.EmployeeMapper;
import com.employeemanagement.repository.AttendanceRepository;
import com.employeemanagement.repository.DepartmentRepository;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.LeaveRequestRepository;
import com.employeemanagement.repository.NotificationRepository;
import com.employeemanagement.repository.PayrollRepository;
import com.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An employee with this email already exists: " + request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Employee employee = Employee.builder()
                .employeeCode(generateEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .joiningDate(request.getJoiningDate())
                .designation(request.getDesignation())
                .salary(request.getSalary())
                .department(department)
                .status(EmployeeStatus.ACTIVE)
                .build();

        // Optionally create a linked login account for the employee
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateResourceException("Username already exists: " + request.getUsername());
            }
            String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                    ? request.getPassword() : "Employee@123";
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(rawPassword))
                    .role(Role.EMPLOYEE)
                    .enabled(true)
                    .build();
            user = userRepository.save(user);
            employee.setUser(user);
        }

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    private String generateEmployeeCode() {
        long count = employeeRepository.count();
        long next = count + 1001;
        String code;
        do {
            code = "EMP" + next;
            next++;
        } while (employeeRepository.existsByEmployeeCode(code));
        return code;
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(Long userId) {
        Employee employee = employeeRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for the current user"));
        return employeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(String search, Long departmentId, String designation,
                                                           EmployeeStatus status, Pageable pageable) {
        Page<Employee> page = employeeRepository.search(search, departmentId, designation, status, pageable);
        Page<EmployeeResponse> mapped = page.map(employeeMapper::toResponse);
        return PageResponse.of(mapped);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An employee with this email already exists: " + request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setDesignation(request.getDesignation());
        employee.setSalary(request.getSalary());
        employee.setDepartment(department);

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    @Transactional
    public EmployeeResponse setStatus(Long id, EmployeeStatus status) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setStatus(status);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    /**
     * Permanently deletes an employee and all dependent records in the correct order
     * to avoid orphaned rows and foreign-key violations. Only reachable by ADMIN
     * (enforced at the controller level) and employees cannot delete themselves
     * (also enforced at the controller level using the authenticated principal).
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // 1. Delete attendance records
        List<Attendance> attendanceRecords = attendanceRepository.findByEmployee_IdOrderByAttendanceDateDesc(id);
        attendanceRepository.deleteAll(attendanceRecords);

        // 2. Delete leave requests
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployee_IdOrderByAppliedDateDesc(id);
        leaveRequestRepository.deleteAll(leaveRequests);

        // 3. Delete payroll records
        List<Payroll> payrollRecords = payrollRepository.findByEmployee_IdOrderByYearDescMonthDesc(id);
        payrollRepository.deleteAll(payrollRecords);

        // 4. Delete the linked user account (and its notifications), if any
        User user = employee.getUser();
        if (user != null) {
            List<Notification> notifications = notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
            notificationRepository.deleteAll(notifications);
            employee.setUser(null);
            employeeRepository.save(employee);
            userRepository.delete(user);
        }

        // 5. Finally, delete the employee record itself
        employeeRepository.delete(employee);
    }

    @Transactional(readOnly = true)
    public long countByStatus(EmployeeStatus status) {
        return employeeRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Employee> findByDepartment(Long departmentId) {
        return employeeRepository.findByDepartment_Id(departmentId);
    }

    /** Validates that the given employee is ACTIVE before allowing self-service operations. */
    @Transactional(readOnly = true)
    public void assertEmployeeActive(Employee employee) {
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BadRequestException("This employee account is inactive and cannot perform this operation.");
        }
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeEntityByUserId(Long userId) {
        return employeeRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for the current user"));
    }
}
