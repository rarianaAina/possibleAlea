package com.example.erpnextintegration.dto.payroll;


import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalarySlipDTO {
    private String name;
    private String employee;
    private String employeeName;
    private String employeeFullName;
    private String designation;
    private String department;
    private String company;
    private String salaryStructure;
    private String letterHead;
    private LocalDate postingDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalWorkingDays;
    private double leaveWithoutPay;
    private double paymentDays;
    private double absences;
    private double grossPay;
    private double totalDeduction;
    private double netPay;
    private double roundedTotalAmount;
    private String status; // "Draft", "Submitted", "Cancelled"
    private String bankName;
    private String bankAccountNo;
    private String mode;
    private List<SalaryDetailDTO> earnings;
    private List<SalaryDetailDTO> deductions;
}