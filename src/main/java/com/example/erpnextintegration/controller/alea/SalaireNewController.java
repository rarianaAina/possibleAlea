package com.example.erpnextintegration.controller.alea;

import com.example.erpnextintegration.dto.payroll.SalaireDTO;
import com.example.erpnextintegration.service.SalaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.erpnextintegration.service.alea.SalaryNewService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/salaire")
public class SalaireNewController {

    @Autowired
    private SalaryNewService salaireService;


    @GetMapping
    public String afficherFormulaire() {
        return "assign/form";
    }


/*    @GetMapping("/component")
    public List<SalaireDTO> getSalaryByComponent(
            @RequestParam("componentName") String componentName,
            @RequestParam("montant") BigDecimal max
    ) {
        List<SalaireDTO> liste = salaireService.getSalarySlipsWithComponent(componentName, max);
        System.out.println(liste);
        return "assign/liste";
    }*/

    @GetMapping("/component")
    public String getSalaryByComponent(
            @RequestParam("componentName") String componentName,
            @RequestParam("montant") BigDecimal max,
            @RequestParam("condition") String condition,
            @RequestParam("pourcentage") Double pourcentage,
            @RequestParam("plusOuMoins") String plusOuMoins,
            Model model
    ) {
        List<SalaireDTO> liste = salaireService.getSalarySlipsWithComponent(componentName, max, condition, pourcentage, plusOuMoins);
        model.addAttribute("salaireList", liste);
        model.addAttribute("componentName", componentName);
        model.addAttribute("montant", max);
        System.out.println(liste);
        return "assign/liste";
    }

    @GetMapping("/search")
    public String searchSalarySlips(
            @RequestParam String componentName,
            @RequestParam BigDecimal montant,
            @RequestParam String condition,
            @RequestParam Double pourcentage,
            @RequestParam String plusOuMoins,
            Model model,
            RedirectAttributes redirectAttributes) {

        List<SalaireDTO> results = salaireService.getSalarySlipsWithComponent(componentName, montant, condition, pourcentage, plusOuMoins);

        model.addAttribute("salaireList", results);
        model.addAttribute("componentName", componentName);
        model.addAttribute("montant", montant);

        if (!results.isEmpty()) {
            redirectAttributes.addFlashAttribute("success",
                    String.format("%d bulletins de salaire ont été trouvés et annulés automatiquement", results.size()));
        } else {
            redirectAttributes.addFlashAttribute("info",
                    "Aucun bulletin de salaire correspondant aux critères n'a été trouvé");
        }

        return "assign/liste";
    }

/*    @GetMapping("/components/{componentName}")
    @ResponseBody
    public List<SalaireDTO> getSalaryByComponent(@PathVariable String componentName, @RequestParam("condition") String condition) {
        BigDecimal max = BigDecimal.valueOf(900000);
        List<SalaireDTO> liste =  salaireService.getSalarySlipsWithComponent(componentName, max, condition);
        System.out.println(liste);
        return liste;
    }*/

    @PostMapping("/cancel")
    public String cancelSalaire(@RequestParam("slipName") String slipName, RedirectAttributes redirectAttributes) {
        try {
            salaireService.cancelSalarySlip(slipName);
            redirectAttributes.addFlashAttribute("success", "Annulation réussie pour " + slipName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }

        return "redirect:/salaire"; // ou autre URL de redirection
    }

    @PostMapping("/cancel-all")
    public String cancelAllSalaires(@RequestParam("slipIds") String slipIds, RedirectAttributes redirectAttributes) {
        try {
            List<String> ids = Arrays.asList(slipIds.split(","));
            for (String id : ids) {
                salaireService.cancelSalarySlip(id.trim());
            }
            redirectAttributes.addFlashAttribute("success", "Annulation réussie pour tous les bulletins");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }

        return "redirect:/salaire"; // ou autre URL de redirection
    }

}
