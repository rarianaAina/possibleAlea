package com.example.erpnextintegration.controller.components;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.erpnextintegration.service.PayrollService;
import com.example.erpnextintegration.dto.payroll.SalaryComponentDTO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/paie/composants")
public class SalaryComponentController {

    @Autowired
    private PayrollService payrollService;

    @GetMapping("")
    public String listeComposants(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("composants", payrollService.getComposantsSalariaux(apiKey, apiSecret));
        return "composants/liste";
    }

    @GetMapping("/{id}")
    public String detailsComposant(@PathVariable String id, HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("composant", payrollService.getComposantSalarialById(id, apiKey, apiSecret));
        return "composants/details";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModificationComposant(@PathVariable String id, HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("composant", payrollService.getComposantSalarialById(id, apiKey, apiSecret));
        return "composants/modifier";
    }

    @PostMapping("/{id}/modifier")
    public String modifierComposant(@PathVariable String id, @ModelAttribute SalaryComponentDTO composant,
                                    HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        try {
            payrollService.modifierComposantSalarial(id, composant, apiKey, apiSecret);
            return "redirect:/paie/composants/" + id;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("composant", composant);
            return "composants/modifier";
        }
    }

    @GetMapping("/ajouter")
    public String formulaireAjoutComposant() {
        return "composants/ajouter";
    }

    @PostMapping("/ajouter")
    public String ajouterComposant(@ModelAttribute SalaryComponentDTO composant,
                                   HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        try {
            payrollService.ajouterComposantSalarial(composant, apiKey, apiSecret);
            return "redirect:/composants";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "composants/ajouter";
        }
    }
}