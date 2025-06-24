package com.example.erpnextintegration.dto.imports;


import lombok.Data;

@Data
public class RapportErreur {
    private String fichier;
    private String ligne;
    private String raison;
    private String valeur;
}