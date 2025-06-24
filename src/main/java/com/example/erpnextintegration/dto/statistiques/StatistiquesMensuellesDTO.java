package com.example.erpnextintegration.dto.statistiques;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class StatistiquesMensuellesDTO {
    private Integer mois;
    private Integer annee;
    private Integer nombreEmployes;
    private BigDecimal totalSalaires;
    private Map<String, BigDecimal> totalParComposante;
}