package com.example.erpnextintegration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongeDTO {
    private String id;
    private String employeId;
    private String nomEmploye;
    private String typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nombreJours;
    private String motif;
    private String statut;
    private LocalDate dateCreation;
    private String approbateur;
    private LocalDate dateApprobation;
    private String commentaires;
}