/*
package com.example.erpnextintegration.controller.alea;

import com.example.erpnextintegration.service.alea.SalaryAdjustementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.erpnextintegration.service.PayrollService;
import com.example.erpnextintegration.dto.payroll.SalaryAdjustmentDTO;
import com.example.erpnextintegration.dto.payroll.SalaryAdjustmentResultDTO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/paie/ajustements")
public class SalaryAdjustmentController {

    @Autowired
    private SalaryAdjustementService salaryAdjustmentService;

    @Autowired
    private PayrollService payrollService;

    @GetMapping("")
    public String pageAjustements(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("composants", payrollService.getComposantsSalariaux(apiKey, apiSecret));
        return "paie/ajustements/index";
    }

    @PostMapping("/traiter")
    public String traiterAjustement(@ModelAttribute SalaryAdjustmentDTO ajustement,
                                    HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        try {
            SalaryAdjustmentResultDTO resultat = salaryAdjustmentService.traiterAjustementSalarial(
                    ajustement, apiKey, apiSecret);

            model.addAttribute("resultat", resultat);
            model.addAttribute("success", true);
            return "paie/ajustements/resultat";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du traitement de l'ajustement : " + e.getMessage());
            model.addAttribute("composants", payrollService.getComposantsSalariaux(apiKey, apiSecret));
            return "paie/ajustements/index";
        }
    }

    @GetMapping("/historique")
    public String historiqueAjustements(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("ajustements", salaryAdjustmentService.getHistoriqueAjustements(apiKey, apiSecret));
        return "paie/ajustements/historique";
    }
}*/
