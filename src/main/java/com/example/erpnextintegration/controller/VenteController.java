package com.example.erpnextintegration.controller;

import ch.qos.logback.core.net.server.Client;
import com.example.erpnextintegration.dto.ClientDTO;
import com.example.erpnextintegration.dto.CommandeDTO;
import com.example.erpnextintegration.dto.ItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erpnextintegration.service.VenteService;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour le module Vente.
 */
@Controller
@RequestMapping("/ventes")
public class VenteController {

    @Autowired
    private VenteService venteService;
    
    /**
     * Affiche la liste des clients.
     */
    @GetMapping("/clients")
    public String listeClients(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        List<ClientDTO> clients = venteService.getClients(apiKey, apiSecret);
        model.addAttribute("clients", clients);
        
        return "ventes/clients";
    }
    
    /**
     * Affiche le formulaire d'ajout de client.
     */
    @GetMapping("/clients/ajouter")
    public String formulaireAjoutClient() {
        return "ventes/ajouter-client";
    }
    
    /**
     * Traite l'ajout d'un nouveau client.
     */
    @PostMapping("/clients/ajouter")
    public String ajouterClient(
            @RequestParam Map<String, String> formData,
            HttpSession session,
            Model model) {
        
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        try {
            venteService.ajouterClient(formData, apiKey, apiSecret);
            return "redirect:/ventes/clients";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'ajout du client : " + e.getMessage());
            return "ventes/ajouter-client";
        }
    }
    
    /**
     * Affiche les détails d'un client.
     */
    @GetMapping("/clients/{id}")
    public String detailsClient(@PathVariable String id, HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        Map<String, Object> client = venteService.getClientById(id, apiKey, apiSecret);
        model.addAttribute("client", client);
        
        return "ventes/details-client";
    }
    
    /**
     * Affiche la liste des commandes.
     */
    @GetMapping("/commandes")
    public String listeCommandes(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        List<CommandeDTO> commandes = venteService.getCommandes(apiKey, apiSecret);

        model.addAttribute("commandes", commandes);
        
        return "ventes/commandes";
    }
    
    /**
     * Affiche le formulaire d'ajout de commande.
     */
    @GetMapping("/commandes/ajouter")
    public String formulaireAjoutCommande(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        List<ClientDTO> clients = venteService.getClients(apiKey, apiSecret);
        List<ItemDTO> articles = venteService.getItems(apiKey, apiSecret);
        
        model.addAttribute("clients", clients);
        model.addAttribute("articles", articles);
        
        return "ventes/ajouter-commande";
    }
    
    /**
     * Traite l'ajout d'une nouvelle commande.
     */
    @PostMapping("/commandes/ajouter")
    public String ajouterCommande(
            @RequestParam Map<String, String> formData,
            HttpSession session,
            Model model) {
        
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        try {
            venteService.ajouterCommande(formData, apiKey, apiSecret);
            return "redirect:/ventes/commandes";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'ajout de la commande : " + e.getMessage());
            
            // Récupérer à nouveau les données pour le formulaire
            List<ClientDTO> clients = venteService.getClients(apiKey, apiSecret);
            List<ItemDTO> articles = venteService.getItems(apiKey, apiSecret);
            
            model.addAttribute("clients", clients);
            model.addAttribute("articles", articles);
            
            return "ventes/ajouter-commande";
        }
    }
    
    /**
     * Affiche les détails d'une commande.
     */
    @GetMapping("/commandes/{id}")
    public String detailsCommande(@PathVariable String id, HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        
        Map<String, Object> commande = venteService.getCommandeById(id, apiKey, apiSecret);
        model.addAttribute("commande", commande);
        
        return "ventes/details-commande";
    }
}