package com.example.erpnextintegration.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDetailDTO {
    private String name;
    private String salaryComponent;
    private double amount;
    private boolean isStatutory;
    private String description;
}