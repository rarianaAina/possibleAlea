package com.example.erpnextintegration.dto.hr;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    private String name;
    private String employeeName;
    private String employeeFullName;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private double totalLeaveDays;
    private String description;
    private String status; // "Open", "Approved", "Rejected", "Cancelled"
    private String reason;
    private String postingDate;
    private String company;
}