package com.example.erpnextintegration.dto.alea;

import lombok.Data;

@Data
public class SalarySlipFilterDTO {
    private String startDate;
    private String endDate;
    private String salaryComponent;
    private String employee;
    private String department;
    private String status;
}