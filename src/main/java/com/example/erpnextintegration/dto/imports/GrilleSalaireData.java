package com.example.erpnextintegration.dto.imports;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class GrilleSalaireData {
    @JsonProperty("salary_structure")
    @CsvBindByName(column = "salary structure")
    private String salaryStructure;
    @JsonProperty("name")
    @CsvBindByName(column = "name")
    private String name;
    @JsonProperty("abbr")
    @CsvBindByName(column = "Abbr")
    private String abbr;
    @JsonProperty("type")
    @CsvBindByName(column = "type")
    private String type;
    @JsonProperty("valeur")
    @CsvBindByName(column = "valeur")
    private String valeur;
    @JsonProperty("company")
    @CsvBindByName(column = "company")
    private String company;
}