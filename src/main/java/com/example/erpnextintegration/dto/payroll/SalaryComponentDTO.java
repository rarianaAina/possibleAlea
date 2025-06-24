package com.example.erpnextintegration.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryComponentDTO {
    private String name;
    private String salaryComponent;
    private String componentType; // "Earning" ou "Deduction"
    private double amount;
    private double formulaAmount;
    private String formula;
    private boolean isVariable;
    private boolean isTaxable;
    private boolean isAdditionalComponent;
    private String type;
    private String condition;
    private String description;
    private boolean isActive;
}