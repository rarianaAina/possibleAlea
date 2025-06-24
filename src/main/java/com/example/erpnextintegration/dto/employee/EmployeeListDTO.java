package com.example.erpnextintegration.dto.employee;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListDTO {
    private List<EmployeeDTO> data;
    private int total;
    private int offset;
    private int limit;
}