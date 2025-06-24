package com.example.erpnextintegration.dto.importation;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ImportSalaireDTO {
    private String mois;
    private String referenceEmploye;
    private BigDecimal salaireBase;
    private String structureSalariale;
}