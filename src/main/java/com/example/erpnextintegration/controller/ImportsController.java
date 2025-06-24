package com.example.erpnextintegration.controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.erpnextintegration.dto.imports.EmployeData;
import com.example.erpnextintegration.dto.imports.GrilleSalaireData;
import com.example.erpnextintegration.dto.imports.ResultatImport;
import com.example.erpnextintegration.dto.imports.SalaireData;
import com.example.erpnextintegration.service.imports.EmployeeImportService;
import com.example.erpnextintegration.service.imports.GrilleImportService;
import com.example.erpnextintegration.service.imports.ResetService;
import com.example.erpnextintegration.service.imports.SalaireImportService;

import jakarta.servlet.http.HttpSession;

@SuppressWarnings(value = "unused")
@Controller
@RequestMapping("/imports")
public class ImportsController {
    @Autowired
    private EmployeeImportService importService;

    @Autowired
    private GrilleImportService grilleImportService;

    @Autowired
    private SalaireImportService salaireImportService;

    @Autowired
    private ResetService resetService;

    @GetMapping
    public ModelAndView form(HttpSession session){
        ModelAndView modelAndView = new ModelAndView("template");

        modelAndView.addObject("page", "imports/form");

        return modelAndView;
    }

    @PostMapping
    public ModelAndView imports(HttpSession session,@RequestParam("file1") MultipartFile file1,@RequestParam("file2") MultipartFile file2,@RequestParam("file3") MultipartFile file3){
        ModelAndView modelAndView=new ModelAndView("template");
        modelAndView.addObject("page","imports/form");
        ResultatImport resultatImport=new ResultatImport();
        try {
            importService.importEmployesFromCSV(resultatImport,file1);
            grilleImportService.importGrilleSalaireFromCSV(resultatImport, file2);
            salaireImportService.importSalairesFromCSV(resultatImport, file3);

            if(!resultatImport.getErreursEmploye().isEmpty() || !resultatImport.getErreursGrille().isEmpty() || !resultatImport.getErreursSalaire().isEmpty()){
                modelAndView.addObject("erreur1", resultatImport.getErreursEmploye());
                modelAndView.addObject("erreur2", resultatImport.getErreursGrille());
                modelAndView.addObject("erreur3", resultatImport.getErreursSalaire());
            }





            if(resultatImport.getErreursEmploye().isEmpty() && resultatImport.getErreursGrille().isEmpty() && resultatImport.getErreursSalaire().isEmpty()){

                List<EmployeData> employeDatas=resultatImport.getEmployesValides();
                Map<String,String> refEmp=importService.createEmployees(session, employeDatas);

                List<GrilleSalaireData> grilleSalaireDatas=resultatImport.getGrilleSalaireDatas();
                grilleImportService.importGrilleSalaire(session, grilleSalaireDatas);


                List<SalaireData> salaireDatas=salaireImportService.transformeEmploye(resultatImport.getSalaireDatas(), refEmp);
                salaireImportService.importSalaireData(session, salaireDatas);
                modelAndView.addObject("successGlobal", "Importation réussi");
            }

        } catch (Exception e) {
            String erreur=e.getMessage();
            try {
                resetService.resetData(session);
            } catch (Exception e1) {
                erreur+="\n"+e1.getMessage();
            }
            e.printStackTrace();
            modelAndView.addObject("errorGlobal",erreur);

        }
        return modelAndView;
    }
}