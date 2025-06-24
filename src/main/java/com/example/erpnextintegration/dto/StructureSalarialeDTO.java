package com.example.erpnextintegration.dto;

import com.example.erpnextintegration.dto.payroll.ComposanteSalarialeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StructureSalarialeDTO {
    private String name;
    private String nom;
    private String company;
    private String currency;
    private String payroll_frequency;
    private List<ComposanteSalarialeDTO> composantes;
    private String statut;
}