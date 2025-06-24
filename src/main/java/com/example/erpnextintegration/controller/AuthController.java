package com.example.erpnextintegration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erpnextintegration.dto.LoginResponse;
import com.example.erpnextintegration.service.AuthService;

import jakarta.servlet.http.HttpSession;

/**
 * Contrôleur pour gérer l'authentification.
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;
    
    /**
     * Affiche la page de connexion.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    /**
     * Traite la demande de connexion.
     */
    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username, 
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {
        
        try {
            LoginResponse response = authService.login(username, password);
            
            // Stocker les informations de connexion dans la session
            session.setAttribute("apiKey", response.getApiKey());
            session.setAttribute("apiSecret", response.getApiSecret());
            session.setAttribute("sid", response.getSid());
            session.setAttribute("username", username);
            
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Échec de connexion : " + e.getMessage());
            return "login";
        }
    }
    
    /**
     * Déconnexion de l'utilisateur.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}