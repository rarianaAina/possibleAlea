package com.example.erpnextintegration.dto.imports;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class EmployeData {
    @JsonProperty("ref")
    @CsvBindByName(column = "Ref")
    private String ref;

    @JsonProperty("nom")
    @CsvBindByName(column = "Nom")
    private String nom;

    @JsonProperty("prenom")
    @CsvBindByName(column = "Prenom")
    private String prenom;

    @JsonProperty("genre")
    @CsvBindByName(column = "genre")
    private String genre;

    @JsonProperty("date_embauche")
    @CsvBindByName(column = "Date embauche")
    private String dateEmbauche;

    @JsonProperty("date_naissance")
    @CsvBindByName(column = "date naissance")
    private String dateNaissance;

    @JsonProperty("company")
    @CsvBindByName(column = "company")
    private String company;
}