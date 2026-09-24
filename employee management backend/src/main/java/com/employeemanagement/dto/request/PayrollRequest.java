package com.employeemanagement.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollRequest {

    @NotNull(message = "Employee is required")
    private Long employeeId;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be valid")
    private Integer year;

    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.0", message = "Basic salary cannot be negative")
    private BigDecimal basicSalary;

    @DecimalMin(value = "0.0", message = "Allowance cannot be negative")
    private BigDecimal allowance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Deduction cannot be negative")
    private BigDecimal deduction = BigDecimal.ZERO;
}
