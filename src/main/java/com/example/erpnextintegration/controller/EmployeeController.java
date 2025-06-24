package com.example.erpnextintegration.controller;

import com.example.erpnextintegration.dto.employee.EmployeeDTO;
import com.example.erpnextintegration.dto.employee.RechercheFiltreDTO;
import com.example.erpnextintegration.dto.payroll.FiltreSalaireDTO;
import com.example.erpnextintegration.dto.payroll.SalaireDTO;

import com.example.erpnextintegration.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Controller
@RequestMapping("/employes")
public class EmployeeController {

    @Autowired
    private EmployeService employeService;

    @Autowired
    private SalaireService salaireService;

    @Autowired
    private PdfService pdfService;

    @GetMapping
    public String listerEmployes(Model model, RechercheFiltreDTO filtre) {
        System.out.println(filtre);
        System.out.println("tonga");
        List<EmployeeDTO> employes = employeService.rechercherEmployes(filtre);
        model.addAttribute("employes", employes);
        model.addAttribute("filtre", filtre != null ? filtre : new RechercheFiltreDTO());
        model.addAttribute("departements", employeService.obtenirTousDepartements());
        return "employes/liste";
    }

    @GetMapping("/{id}")
    public String afficherEmploye(@PathVariable String id,
                                  Model model,
                                  @RequestParam(required = false) Integer mois,
                                  @RequestParam(required = false) Integer annee) {

        EmployeeDTO employe = employeService.obtenirEmployeParId(id);

        // Valeurs par défaut: mois et année courants
        if (mois == null) {
            mois = LocalDate.now().getMonthValue();
        }
        if (annee == null) {
            annee = LocalDate.now().getYear();
        }

        FiltreSalaireDTO filtre = new FiltreSalaireDTO(mois, annee);
        List<SalaireDTO> salaires = salaireService.obtenirSalairesParEmploye(id, mois, annee);

        model.addAttribute("employe", employe);
        model.addAttribute("salaires", salaires);
        model.addAttribute("filtre", filtre);
        model.addAttribute("moisActuel", mois);
        model.addAttribute("anneeActuelle", annee);

        // Pour les listes déroulantes
        model.addAttribute("mois", Month.values());
        model.addAttribute("annees", salaireService.obtenirAnneesDisponibles());

        return "employes/details";
    }

/*
    @GetMapping("/{id}/salaire/{salaireId}")
    public String afficherSalaire(@PathVariable String id,
                                  @PathVariable String salaireId,
                                  Model model) {
        System.out.println("tonga atoo");
        EmployeeDTO employe = employeService.obtenirEmployeParId(id);
        SalaireDTO salaire = salaireService.obtenirSalaireParId(salaireId);

        model.addAttribute("employe", employe);
        model.addAttribute("salaire", salaire);

        return "employes/salaire";
    }
*/

    @GetMapping("/{id}/salaire")
    public String afficherSalaire(@PathVariable String id,
                                  @RequestParam String salaireId,
                                  Model model) {
        System.out.println("tonga atoo");
        EmployeeDTO employe = employeService.obtenirEmployeParId(id);
        SalaireDTO salaire = salaireService.obtenirSalaireParId(salaireId);

        model.addAttribute("employe", employe);
        model.addAttribute("salaire", salaire);

        return "employes/salaire";
    }

    @GetMapping("/{id}/salaire/pdf")
    public void telechargerFichePaie(@PathVariable String id,
                                     @RequestParam String salaireId,
                                     HttpServletResponse response) throws Exception {
        EmployeeDTO employe = employeService.obtenirEmployeParId(id);
        SalaireDTO salaire = salaireService.obtenirSalaireParId(salaireId);

        pdfService.genererFichePaiePdf(employe, salaire, response);
    }

}