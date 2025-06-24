package com.example.erpnextintegration.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private String name;
    private String departmentName;
    private String parent;
    private String company;
    private String description;
    private String departmentHead;
}