package com.example.erpnextintegration.controller;

import com.example.erpnextintegration.dto.ClientDTO;
import com.example.erpnextintegration.dto.ProjectDTO;
import com.example.erpnextintegration.dto.TaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erpnextintegration.service.ProjetService;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour le module Projet.
 */
@Controller
@RequestMapping("/projets")
public class ProjetController {

    @Autowired
    private ProjetService projetService;
    
    /**
     * Affiche la liste des projets.
     */
    @GetMapping("")
    public String listeProjets(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        System.out.println(apiKey);
        System.out.println(apiSecret);

        List<ProjectDTO> projets = projetService.getProjets(apiKey, apiSecret);
        System.out.println(projets);
        model.addAttribute("projets", projets);
        
        return "projets/liste";
    }
    
    /**
     * Affiche le formulaire d'ajout de projet.
     */
    @GetMapping("/ajouter")
    public String formulaireAjoutProjet(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        List<ClientDTO> clients = projetService.getClients(apiKey, apiSecret);
        System.out.println(clients);
        model.addAttribute("clients", clients);
        
        return "projets/ajouter";
    }
    
    /**
     * Traite l'ajout d'un nouveau projet.
     */
    @PostMapping("/ajouter")
    public String ajouterProjet(
            @RequestParam Map<String, String> formData,
            HttpSession session,
            Model model) {
        
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        try {
            projetService.ajouterProjet(formData, apiKey, apiSecret);
            return "redirect:/projets";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'ajout du projet : " + e.getMessage());
            
            // Récupérer à nouveau les clients pour le formulaire

            List<ClientDTO> clients = projetService.getClients(apiKey, apiSecret);
            System.out.println(clients);
            model.addAttribute("clients", clients);
            
            return "projets/ajouter";
        }
    }
    
    /**
     * Affiche les détails d'un projet.
     */
    @GetMapping("/{id}")
    public String detailsProjet(@PathVariable String id, HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        ProjectDTO projet = projetService.getProjetById(id, apiKey, apiSecret);
        model.addAttribute("projet", projet);
        
        return "projets/details";
    }
    
    /**
     * Affiche la liste des tâches d'un projet.
     */
    @GetMapping("/{id}/taches")
    public String listeTaches(@PathVariable String id, HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        ProjectDTO projet = projetService.getProjetById(id, apiKey, apiSecret);
        List<TaskDTO> taches = projetService.getTachesByProjet(id, apiKey, apiSecret);

        System.out.println(taches);
        model.addAttribute("projet", projet);
        model.addAttribute("taches", taches);

        return "projets/taches";
    }

    /**
     * Affiche le formulaire d'ajout de tâche.
     */
    @GetMapping("/{id}/taches/ajouter")
    public String formulaireAjoutTache(@PathVariable String id, HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        ProjectDTO projet = projetService.getProjetById(id, apiKey, apiSecret);
        model.addAttribute("projet", projet);
        
        return "projets/ajouter-tache";
    }
    
    /**
     * Traite l'ajout d'une nouvelle tâche.
     */
    @PostMapping("/{id}/taches/ajouter")
    public String ajouterTache(
            @PathVariable String id,
            @RequestParam Map<String, String> formData,
            HttpSession session,
            Model model) {
        
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        try {
            projetService.ajouterTache(id, formData, apiKey, apiSecret);
            return "redirect:/projets/" + id + "/taches";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'ajout de la tâche : " + e.getMessage());
            
            ProjectDTO projet = projetService.getProjetById(id, apiKey, apiSecret);
            model.addAttribute("projet", projet);
            
            return "projets/ajouter-tache";
        }
    }
}