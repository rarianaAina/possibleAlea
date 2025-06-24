package com.example.erpnextintegration.dto.imports;


import com.example.erpnextintegration.utils.CommaToDotDoubleConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import lombok.Data;

@Data
public class SalaireData {
    @JsonProperty("mois")

    // @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "Mois")
    private String mois;

    // @CsvBindByPosition(position = 0)
    @JsonProperty("ref_employe")
    @CsvBindByName(column = "Ref Employe")
    private String refEmploye;

    // @CsvBindByPosition(position = 0)
    @JsonProperty("salaire_base")
    @CsvCustomBindByName(column = "Salaire Base", converter = CommaToDotDoubleConverter.class)
    private Double salaireBase;

    // @CsvBindByPosition(position = 0)
    @JsonProperty("salary_structure")
    @CsvBindByName(column = "Salaire")
    private String salaryStructure;

}