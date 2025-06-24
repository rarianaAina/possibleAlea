package com.example.erpnextintegration.dto.payroll;


import lombok.Data;
import java.util.List;

@Data
public class SalaryAdjustmentResultDTO {
    private int affectedEmployees;
    private int cancelledSlips;
    private int newSlipsCreated;
    private List<String> processedEmployees;
    private String message;
}