package com.employeemanagement.config;

import com.employeemanagement.entity.*;
import com.employeemanagement.entity.enums.*;
import com.employeemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the database with sample Admin/HR/Employee accounts, departments,
 * employees, attendance, leave and payroll records so the app is immediately
 * demoable. Runs only once - it checks whether data already exists first.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        // ---------- Departments ----------
        Department it = departmentRepository.save(Department.builder()
                .departmentName("IT").description("Information Technology").status(DepartmentStatus.ACTIVE).build());
        Department hrDept = departmentRepository.save(Department.builder()
                .departmentName("HR").description("Human Resources").status(DepartmentStatus.ACTIVE).build());
        Department finance = departmentRepository.save(Department.builder()
                .departmentName("Finance").description("Finance and Accounts").status(DepartmentStatus.ACTIVE).build());
        departmentRepository.save(Department.builder()
                .departmentName("Marketing").description("Marketing and Branding").status(DepartmentStatus.ACTIVE).build());
        departmentRepository.save(Department.builder()
                .departmentName("Sales").description("Sales team").status(DepartmentStatus.ACTIVE).build());
        departmentRepository.save(Department.builder()
                .departmentName("Operations").description("Operations team").status(DepartmentStatus.ACTIVE).build());

        // ---------- Admin ----------
        User admin = userRepository.save(User.builder()
                .username("admin").email("admin@ems.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN).enabled(true).build());

        // ---------- HR ----------
        User hrUser = userRepository.save(User.builder()
                .username("hr_manager").email("hr@ems.com")
                .password(passwordEncoder.encode("Hr@12345"))
                .role(Role.HR).enabled(true).build());

        Employee hrEmployee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP1001")
                .firstName("Priya").lastName("Sharma")
                .email("hr@ems.com").phone("9876543210")
                .address("Pune, Maharashtra")
                .dateOfBirth(LocalDate.of(1990, 5, 12))
                .gender("Female")
                .joiningDate(LocalDate.of(2021, 1, 10))
                .designation("HR Manager")
                .salary(new BigDecimal("55000"))
                .department(hrDept)
                .user(hrUser)
                .status(EmployeeStatus.ACTIVE)
                .build());

        // ---------- Employee 1 ----------
        User empUser1 = userRepository.save(User.builder()
                .username("john.doe").email("john.doe@ems.com")
                .password(passwordEncoder.encode("Employee@123"))
                .role(Role.EMPLOYEE).enabled(true).build());

        Employee emp1 = employeeRepository.save(Employee.builder()
                .employeeCode("EMP1002")
                .firstName("John").lastName("Doe")
                .email("john.doe@ems.com").phone("9123456780")
                .address("Nashik, Maharashtra")
                .dateOfBirth(LocalDate.of(1995, 8, 21))
                .gender("Male")
                .joiningDate(LocalDate.of(2022, 3, 1))
                .designation("Software Engineer")
                .salary(new BigDecimal("48000"))
                .department(it)
                .user(empUser1)
                .status(EmployeeStatus.ACTIVE)
                .build());

        // ---------- Employee 2 ----------
        User empUser2 = userRepository.save(User.builder()
                .username("jane.smith").email("jane.smith@ems.com")
                .password(passwordEncoder.encode("Employee@123"))
                .role(Role.EMPLOYEE).enabled(true).build());

        Employee emp2 = employeeRepository.save(Employee.builder()
                .employeeCode("EMP1003")
                .firstName("Jane").lastName("Smith")
                .email("jane.smith@ems.com").phone("9988776655")
                .address("Mumbai, Maharashtra")
                .dateOfBirth(LocalDate.of(1993, 2, 17))
                .gender("Female")
                .joiningDate(LocalDate.of(2021, 7, 15))
                .designation("Accountant")
                .salary(new BigDecimal("42000"))
                .department(finance)
                .user(empUser2)
                .status(EmployeeStatus.ACTIVE)
                .build());

        // Third employee, no login account, inactive - shows activate/deactivate demo
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP1004")
                .firstName("Ravi").lastName("Kumar")
                .email("ravi.kumar@ems.com").phone("9012345678")
                .address("Nagpur, Maharashtra")
                .dateOfBirth(LocalDate.of(1998, 11, 3))
                .gender("Male")
                .joiningDate(LocalDate.of(2023, 6, 1))
                .designation("Sales Executive")
                .salary(new BigDecimal("35000"))
                .department(it)
                .status(EmployeeStatus.INACTIVE)
                .build());

        // ---------- Attendance (last 5 days for emp1 & emp2) ----------
        for (int i = 4; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            attendanceRepository.save(Attendance.builder()
                    .employee(emp1).attendanceDate(date)
                    .checkIn(java.time.LocalTime.of(9, 0))
                    .checkOut(java.time.LocalTime.of(18, 0))
                    .status(AttendanceStatus.PRESENT)
                    .remarks("On time")
                    .build());
            attendanceRepository.save(Attendance.builder()
                    .employee(emp2).attendanceDate(date)
                    .checkIn(java.time.LocalTime.of(9, 15))
                    .checkOut(java.time.LocalTime.of(18, 5))
                    .status(i == 2 ? AttendanceStatus.HALF_DAY : AttendanceStatus.PRESENT)
                    .remarks(i == 2 ? "Left early" : "On time")
                    .build());
        }

        // ---------- Leave requests ----------
        leaveRequestRepository.save(LeaveRequest.builder()
                .employee(emp1)
                .leaveType(LeaveType.CASUAL)
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(4))
                .numberOfDays(2L)
                .reason("Family function")
                .status(LeaveStatus.PENDING)
                .build());

        leaveRequestRepository.save(LeaveRequest.builder()
                .employee(emp2)
                .leaveType(LeaveType.SICK)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().minusDays(9))
                .numberOfDays(2L)
                .reason("Fever")
                .status(LeaveStatus.APPROVED)
                .approvedBy("hr_manager")
                .build());

        // ---------- Payroll ----------
        LocalDate now = LocalDate.now();
        payrollRepository.save(Payroll.builder()
                .employee(emp1).month(now.getMonthValue()).year(now.getYear())
                .basicSalary(new BigDecimal("48000"))
                .allowance(new BigDecimal("5000"))
                .deduction(new BigDecimal("2000"))
                .netSalary(new BigDecimal("51000"))
                .paymentStatus(PaymentStatus.PAID)
                .paymentDate(now.withDayOfMonth(1))
                .build());

        payrollRepository.save(Payroll.builder()
                .employee(emp2).month(now.getMonthValue()).year(now.getYear())
                .basicSalary(new BigDecimal("42000"))
                .allowance(new BigDecimal("3000"))
                .deduction(new BigDecimal("1500"))
                .netSalary(new BigDecimal("43500"))
                .paymentStatus(PaymentStatus.PENDING)
                .build());

        System.out.println("=================================================");
        System.out.println(" Sample data seeded successfully.");
        System.out.println(" Login credentials:");
        System.out.println("   ADMIN    -> username: admin        password: Admin@123");
        System.out.println("   HR       -> username: hr_manager   password: Hr@12345");
        System.out.println("   EMPLOYEE -> username: john.doe     password: Employee@123");
        System.out.println("   EMPLOYEE -> username: jane.smith   password: Employee@123");
        System.out.println("=================================================");
    }
}
