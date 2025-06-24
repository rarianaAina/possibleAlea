package com.example.erpnextintegration.dto.conge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCongeDTO {
    private String employeId;
    private String typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
}