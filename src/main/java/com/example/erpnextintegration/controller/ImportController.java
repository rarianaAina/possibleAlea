package com.example.erpnextintegration.controller;

import com.example.erpnextintegration.service.ImportService;
import com.example.erpnextintegration.service.ImportationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/import")
public class ImportController {

    @Autowired
    private ImportationService importService;


    @GetMapping
    public String afficherPageImport() {
        return "importation/index";
    }

    @PostMapping("/upload")
    public String importerFichiers(
            @RequestParam("employesFile") MultipartFile employesFile,
            @RequestParam("structuresFile") MultipartFile structuresFile,
            @RequestParam("salairesFile") MultipartFile salairesFile,
            RedirectAttributes redirectAttributes) {
        try {
            importService.importerDonnees(employesFile, structuresFile, salairesFile);
            redirectAttributes.addFlashAttribute("success",
                    "L'importation a été effectuée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'importation : " + e.getMessage());
        }
        return "redirect:/import";
    }
}