package com.example.erpnextintegration.dto.importation;


import lombok.Data;
import java.time.LocalDate;

@Data
public class ImportEmployeeDTO {
    private String reference;
    private String nom;
    private String prenom;
    private String genre;
    private LocalDate dateEmbauche;
    private LocalDate dateNaissance;
    private String entreprise;
}