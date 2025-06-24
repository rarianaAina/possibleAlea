package com.example.erpnextintegration.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifierComposanteDTO {
    private String id;
    private String nom;
    private String abreviation;
    private String type;
    private String formule;
    private BigDecimal montantFixe;
    private String baseDe;
    private String description;
}