package com.example.erpnextintegration.dto.employee;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesignationDTO {
    private String name;
    private String designation;
    private String description;
    private String department;
}
