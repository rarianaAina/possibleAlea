package com.example.erpnextintegration.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SalaireDTO {
    private String id;
    private String employeId;
    private String nomEmploye;
    private Integer mois;
    private Integer annee;
    private LocalDate datePaiement;
    private BigDecimal salaireBrut;
    private BigDecimal salaireNet;
    private List<ComposanteSalarialeDTO> composantes;
}