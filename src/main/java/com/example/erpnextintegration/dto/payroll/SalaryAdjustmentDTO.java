package com.example.erpnextintegration.dto.payroll;


import lombok.Data;

@Data
public class SalaryAdjustmentDTO {
    private String salaryComponent;
    private String comparisonOperator; // "lt", "gt", "eq", "lte", "gte"
    private Double comparisonAmount;
    private String adjustmentType; // "plus" ou "minus"
    private Double adjustmentPercentage;
    private String startDate;
    private String endDate;
}