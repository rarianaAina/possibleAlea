package com.example.erpnextintegration.dto.payroll;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructureDTO {
    private String name;
    private String salaryStructureName;
    private double netPay;
    private String payrollFrequency; // "Monthly", "Bi-Monthly", "Weekly", etc.
    private String company;
    private List<SalaryComponentDTO> earnings;
    private List<SalaryComponentDTO> deductions;
    private String currency;
    private boolean isActive;
    private String fromDate;

}