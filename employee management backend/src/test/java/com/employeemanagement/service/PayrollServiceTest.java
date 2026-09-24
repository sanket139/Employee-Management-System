package com.employeemanagement.service;

import com.employeemanagement.dto.request.PayrollRequest;
import com.employeemanagement.dto.response.PayrollResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Payroll;
import com.employeemanagement.entity.enums.PaymentStatus;
import com.employeemanagement.exception.DuplicateResourceException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.PayrollMapper;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock private PayrollRepository payrollRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private PayrollMapper payrollMapper;

    @InjectMocks
    private PayrollService payrollService;

    private Employee employee;
    private PayrollRequest request;

    @BeforeEach
    void setUp() {
        employee = Employee.builder().id(1L).firstName("John").lastName("Doe").build();

        request = new PayrollRequest();
        request.setEmployeeId(1L);
        request.setMonth(9);
        request.setYear(2026);
        request.setBasicSalary(new BigDecimal("40000"));
        request.setAllowance(new BigDecimal("5000"));
        request.setDeduction(new BigDecimal("2000"));
    }

    @Test
    void createPayroll_success_calculatesNetSalaryOnBackend() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployee_IdAndMonthAndYear(1L, 9, 2026)).thenReturn(false);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(inv -> inv.getArgument(0));
        when(payrollMapper.toResponse(any(Payroll.class))).thenAnswer(inv -> {
            Payroll p = inv.getArgument(0);
            return PayrollResponse.builder().id(1L).netSalary(p.getNetSalary()).build();
        });

        PayrollResponse response = payrollService.createPayroll(request);

        // 40000 + 5000 - 2000 = 43000, never trusts a client-provided net salary
        assertThat(response.getNetSalary()).isEqualByComparingTo("43000");

        ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(captor.capture());
        assertThat(captor.getValue().getNetSalary()).isEqualByComparingTo("43000");
    }

    @Test
    void createPayroll_duplicateForSameMonth_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployee_IdAndMonthAndYear(1L, 9, 2026)).thenReturn(true);

        assertThatThrownBy(() -> payrollService.createPayroll(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(payrollRepository, never()).save(any());
    }

    @Test
    void createPayroll_employeeNotFound_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.createPayroll(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAsPaid_updatesStatusAndPaymentDate() {
        Payroll payroll = Payroll.builder().id(1L).employee(employee)
                .basicSalary(new BigDecimal("40000")).allowance(BigDecimal.ZERO).deduction(BigDecimal.ZERO)
                .netSalary(new BigDecimal("40000")).paymentStatus(PaymentStatus.PENDING).build();

        when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(inv -> inv.getArgument(0));
        when(payrollMapper.toResponse(any(Payroll.class))).thenReturn(
                PayrollResponse.builder().id(1L).paymentStatus(PaymentStatus.PAID).build());

        PayrollResponse response = payrollService.markAsPaid(1L);

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payroll.getPaymentDate()).isNotNull();
    }

    @Test
    void getById_notFound_throwsException() {
        when(payrollRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
