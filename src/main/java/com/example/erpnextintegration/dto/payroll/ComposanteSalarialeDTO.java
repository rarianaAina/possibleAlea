package com.example.erpnextintegration.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComposanteSalarialeDTO {
    private String id;
    private String nom;
    private String type; // GAIN, DEDUCTION
    private BigDecimal montant;
    private String description;
    private String name;
}