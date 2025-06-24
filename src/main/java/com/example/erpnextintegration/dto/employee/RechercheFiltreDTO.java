package com.example.erpnextintegration.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechercheFiltreDTO {
    private String nom;
    private String departement;
    private String statut;
    private String designation;
}