package com.example.erpnextintegration.controller;


import com.example.erpnextintegration.dto.payroll.ComposanteSalarialeDTO;
import com.example.erpnextintegration.dto.payroll.ModifierComposanteDTO;
import com.example.erpnextintegration.dto.StructureSalarialeDTO;
import com.example.erpnextintegration.service.StructureSalarialeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/structures-salariales")
public class StructureSalarialeController {

    private final StructureSalarialeService structureSalarialeService;

    @Autowired
    public StructureSalarialeController(StructureSalarialeService structureSalarialeService) {
        this.structureSalarialeService = structureSalarialeService;
    }

    @GetMapping
    public String listerStructures(Model model) {
        List<StructureSalarialeDTO> structures = structureSalarialeService.obtenirToutesStructures();
        model.addAttribute("structures", structures);
        return "structures/liste";
    }

    @GetMapping("/{id}")
    public String afficherStructure(@PathVariable String id, Model model) {
        StructureSalarialeDTO structure = structureSalarialeService.obtenirStructureParId(id);
        model.addAttribute("structure", structure);
        return "structures/details";
    }

    @GetMapping("/{id}/modifier-composante/{composanteId}")
    public String afficherModifierComposante(@PathVariable String id,
                                             @PathVariable String composanteId,
                                             Model model) {
        StructureSalarialeDTO structure = structureSalarialeService.obtenirStructureParId(id);

        ComposanteSalarialeDTO composante = structure.getComposantes().stream()
                .filter(c -> c.getId().equals(composanteId))
                .findFirst()
                .orElse(null);

        if (composante != null) {
            ModifierComposanteDTO modifierComposante = new ModifierComposanteDTO();
            modifierComposante.setId(composante.getId());
            modifierComposante.setNom(composante.getNom());
            modifierComposante.setAbreviation(composante.getDescription());
            modifierComposante.setType(composante.getType());
            modifierComposante.setMontantFixe(composante.getMontant());

            model.addAttribute("structure", structure);
            model.addAttribute("composante", modifierComposante);
        }

        return "structures/modifier-composante";
    }

    @PostMapping("/{id}/modifier-composante")
    public String modifierComposante(@PathVariable String id,
                                     @ModelAttribute ModifierComposanteDTO composante,
                                     RedirectAttributes redirectAttributes) {
        try {
            structureSalarialeService.modifierComposante(id, composante);
            redirectAttributes.addFlashAttribute("success",
                    "La composante a été modifiée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la modification : " + e.getMessage());
        }

        return "redirect:/structures-salariales/" + id;
    }

    @GetMapping("/{id}/ajouter-composante")
    public String afficherAjouterComposante(@PathVariable String id, Model model) {
        StructureSalarialeDTO structure = structureSalarialeService.obtenirStructureParId(id);
        model.addAttribute("structure", structure);
        model.addAttribute("composante", new ComposanteSalarialeDTO());
        return "structures/ajouter-composante";
    }

    @PostMapping("/{id}/ajouter-composante")
    public String ajouterComposante(@PathVariable String id,
                                    @ModelAttribute ComposanteSalarialeDTO composante,
                                    RedirectAttributes redirectAttributes) {
        try {
            structureSalarialeService.ajouterComposante(id, composante);
            redirectAttributes.addFlashAttribute("success",
                    "La composante a été ajoutée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'ajout : " + e.getMessage());
        }

        return "redirect:/structures-salariales/" + id;
    }
}