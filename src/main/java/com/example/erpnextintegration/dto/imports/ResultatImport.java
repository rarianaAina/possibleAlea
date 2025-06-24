package com.example.erpnextintegration.dto.imports;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResultatImport {
    private List<EmployeData> employesValides;
    private List<GrilleSalaireData> grilleSalaireDatas ;
    private List<SalaireData> salaireDatas;


    private List<RapportErreur> erreursEmploye;
    private List<RapportErreur> erreursGrille;
    private List<RapportErreur> erreursSalaire;
    public ResultatImport() {

        this.employesValides=new ArrayList<>();
        this.grilleSalaireDatas=new ArrayList<>();
        this.salaireDatas=new ArrayList<>();

        this.erreursEmploye=new ArrayList<>();
        this.erreursGrille=new ArrayList<>();
        this.erreursSalaire=new ArrayList<>();

    }

}