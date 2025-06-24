package com.example.erpnextintegration.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltreSalaireDTO {
    private Integer mois;
    private Integer annee;
}