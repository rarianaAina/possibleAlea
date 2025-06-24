package com.example.erpnextintegration.dto.payroll;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEntryDTO {
    private String name;
    private LocalDate postingDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String payrollFrequency;
    private String company;
    private String branch;
    private String department;
    private String designation;
    private String status; // "Draft", "Submitted"
    private boolean salarySlipsCreated;
    private boolean salarySlipsSubmitted;
    private List<String> employees;
}