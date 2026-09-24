package com.employeemanagement.mapper;

import com.employeemanagement.dto.response.PayrollResponse;
import com.employeemanagement.entity.Payroll;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {

    public PayrollResponse toResponse(Payroll p) {
        return PayrollResponse.builder()
                .id(p.getId())
                .employeeId(p.getEmployee().getId())
                .employeeName(p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName())
                .month(p.getMonth())
                .year(p.getYear())
                .basicSalary(p.getBasicSalary())
                .allowance(p.getAllowance())
                .deduction(p.getDeduction())
                .netSalary(p.getNetSalary())
                .paymentStatus(p.getPaymentStatus())
                .paymentDate(p.getPaymentDate())
                .build();
    }
}
