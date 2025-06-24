package com.example.erpnextintegration.controller.alea;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.erpnextintegration.service.PayrollService;
import com.example.erpnextintegration.dto.payroll.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/paie")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @GetMapping("/structures")
    public String listeStructuresSalariales(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("structures", payrollService.getStructuresSalariales(apiKey, apiSecret));
        return "paie/structures/liste";
    }

    @GetMapping("/structures/ajouter")
    public String formulaireAjoutStructure(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("composants", payrollService.getComposantsSalariaux(apiKey, apiSecret));
        return "paie/structures/ajouter";
    }

    @PostMapping("/structures/ajouter")
    public String ajouterStructure(@ModelAttribute SalaryStructureDTO structure,
                                   HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        try {
            payrollService.ajouterStructureSalariale(structure, apiKey, apiSecret);
            return "redirect:/paie/structures";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "paie/structures/ajouter";
        }
    }

    @GetMapping("/bulletins")
    public String listeBulletins(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("bulletins", payrollService.getBulletinsPaie(apiKey, apiSecret));
        return "paie/bulletins/liste";
    }

    @GetMapping("/bulletins/generer")
    public String formulaireGenerationBulletin(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        model.addAttribute("employes", payrollService.getEmployesActifs(apiKey, apiSecret));
        model.addAttribute("structures", payrollService.getStructuresSalariales(apiKey, apiSecret));
        return "paie/bulletins/generer";
    }

    @PostMapping("/bulletins/generer")
    public String genererBulletin(@ModelAttribute SalarySlipDTO bulletin,
                                  HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        try {
            payrollService.genererBulletinPaie(bulletin, apiKey, apiSecret);
            return "redirect:/paie/bulletins";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "paie/bulletins/generer";
        }
    }

    @GetMapping("/bulletins/details")
    public String detailsBulletin(@RequestParam String id, HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        SalarySlipDTO salaryy = payrollService.getBulletinPaieById(id, apiKey, apiSecret);
        System.out.println(salaryy);
        model.addAttribute("bulletin", salaryy);
        return "paie/bulletins/details";
    }

    @PostMapping("/bulletins/{id}/soumettre")
    public String soumettreBulletin(@PathVariable String id, HttpSession session) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        payrollService.soumettreBulletinPaie(id, apiKey, apiSecret);
        return "redirect:/paie/bulletins/" + id;
    }
}