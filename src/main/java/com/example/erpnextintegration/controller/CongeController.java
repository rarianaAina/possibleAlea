package com.example.erpnextintegration.controller;


import com.example.erpnextintegration.dto.CongeDTO;
import com.example.erpnextintegration.dto.conge.DemandeCongeDTO;
import com.example.erpnextintegration.service.CongeService;
import com.example.erpnextintegration.service.EmployeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/conges")
public class CongeController {

    private final CongeService congeService;
    private final EmployeService employeService;

    @Autowired
    public CongeController(CongeService congeService, EmployeService employeService) {
        this.congeService = congeService;
        this.employeService = employeService;
    }

    @GetMapping
    public String listerConges(Model model) {
        List<CongeDTO> conges = congeService.obtenirTousConges();
        model.addAttribute("conges", conges);
        return "conges/liste";
    }

    @GetMapping("/employe/{employeId}")
    public String listerCongesEmploye(@PathVariable String employeId, Model model) {
        List<CongeDTO> conges = congeService.obtenirCongesParEmploye(employeId);
        model.addAttribute("conges", conges);
        model.addAttribute("employe", employeService.obtenirEmployeParId(employeId));
        return "conges/liste-employe";
    }

    @GetMapping("/nouveau")
    public String afficherNouvelleDemandeConge(Model model) {
        System.out.println("Nantsoina");
        model.addAttribute("demande", new DemandeCongeDTO());
        model.addAttribute("employes", employeService.rechercherEmployes(null));
        model.addAttribute("typesConge", congeService.obtenirTypesConge());
        return "conges/nouvelle-demande";
    }

    @GetMapping("/employe/{employeId}/nouveau")
    public String afficherNouvelleDemandeCongeEmploye(@PathVariable String employeId, Model model) {
        DemandeCongeDTO demande = new DemandeCongeDTO();
        demande.setEmployeId(employeId);

        model.addAttribute("demande", demande);
        model.addAttribute("employe", employeService.obtenirEmployeParId(employeId));
        model.addAttribute("typesConge", congeService.obtenirTypesConge());
        return "conges/nouvelle-demande-employe";
    }

    @PostMapping("/nouveau")
    public String creerDemandeConge(@ModelAttribute DemandeCongeDTO demande,
                                    RedirectAttributes redirectAttributes) {
        try {
            CongeDTO conge = congeService.creerDemandeConge(demande);
            redirectAttributes.addFlashAttribute("success",
                    "La demande de congé a été créée avec succès.");
            return "redirect:/conges";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création : " + e.getMessage());
            return "redirect:/conges/nouveau";
        }
    }

    @PostMapping("/{id}/approuver")
    public String approuverConge(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            congeService.approuverConge(id);
            redirectAttributes.addFlashAttribute("success",
                    "Le congé a été approuvé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'approbation : " + e.getMessage());
        }

        return "redirect:/conges";
    }

    @PostMapping("/{id}/rejeter")
    public String rejeterConge(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            congeService.rejeterConge(id);
            redirectAttributes.addFlashAttribute("success",
                    "Le congé a été rejeté.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du rejet : " + e.getMessage());
        }

        return "redirect:/conges";
    }
}