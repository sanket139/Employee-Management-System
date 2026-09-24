package com.employeemanagement.service;

import com.employeemanagement.dto.request.PayrollRequest;
import com.employeemanagement.dto.response.PageResponse;
import com.employeemanagement.dto.response.PayrollResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Payroll;
import com.employeemanagement.entity.enums.PaymentStatus;
import com.employeemanagement.exception.DuplicateResourceException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.PayrollMapper;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollMapper payrollMapper;

    @Transactional
    public PayrollResponse createPayroll(PayrollRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        if (payrollRepository.existsByEmployee_IdAndMonthAndYear(request.getEmployeeId(), request.getMonth(), request.getYear())) {
            throw new DuplicateResourceException("Payroll already exists for this employee for "
                    + request.getMonth() + "/" + request.getYear());
        }

        BigDecimal allowance = request.getAllowance() != null ? request.getAllowance() : BigDecimal.ZERO;
        BigDecimal deduction = request.getDeduction() != null ? request.getDeduction() : BigDecimal.ZERO;
        BigDecimal netSalary = request.getBasicSalary().add(allowance).subtract(deduction);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(request.getMonth())
                .year(request.getYear())
                .basicSalary(request.getBasicSalary())
                .allowance(allowance)
                .deduction(deduction)
                .netSalary(netSalary)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Payroll saved = payrollRepository.save(payroll);
        return payrollMapper.toResponse(saved);
    }

    @Transactional
    public PayrollResponse updatePayroll(Long id, PayrollRequest request) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));

        BigDecimal allowance = request.getAllowance() != null ? request.getAllowance() : BigDecimal.ZERO;
        BigDecimal deduction = request.getDeduction() != null ? request.getDeduction() : BigDecimal.ZERO;
        BigDecimal netSalary = request.getBasicSalary().add(allowance).subtract(deduction);

        payroll.setBasicSalary(request.getBasicSalary());
        payroll.setAllowance(allowance);
        payroll.setDeduction(deduction);
        payroll.setNetSalary(netSalary);

        Payroll saved = payrollRepository.save(payroll);
        return payrollMapper.toResponse(saved);
    }

    @Transactional
    public PayrollResponse markAsPaid(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
        payroll.setPaymentStatus(PaymentStatus.PAID);
        payroll.setPaymentDate(LocalDate.now());
        Payroll saved = payrollRepository.save(payroll);
        return payrollMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PayrollResponse getById(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
        return payrollMapper.toResponse(payroll);
    }

    @Transactional(readOnly = true)
    public PageResponse<PayrollResponse> search(Long employeeId, Integer month, Integer year, Pageable pageable) {
        Page<Payroll> page = payrollRepository.search(employeeId, month, year, pageable);
        return PageResponse.of(page.map(payrollMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getByEmployee(Long employeeId) {
        return payrollRepository.findByEmployee_IdOrderByYearDescMonthDesc(employeeId).stream()
                .map(payrollMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PayrollResponse getLatestForEmployee(Long employeeId) {
        List<Payroll> all = payrollRepository.findByEmployee_IdOrderByYearDescMonthDesc(employeeId);
        return all.isEmpty() ? null : payrollMapper.toResponse(all.get(0));
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyPayrollTotal(int month, int year) {
        return payrollRepository.sumNetSalaryForMonth(month, year);
    }
}
