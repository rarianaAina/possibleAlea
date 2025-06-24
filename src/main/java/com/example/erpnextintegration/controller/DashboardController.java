package com.example.erpnextintegration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.erpnextintegration.service.DashboardService;
import com.example.erpnextintegration.dto.DashboardStatsDTO;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        DashboardStatsDTO stats = dashboardService.getDashboardStats(apiKey, apiSecret);
        model.addAttribute("stats", stats);
        model.addAttribute("username", session.getAttribute("username"));

        return "dashboard";
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("apiKey") != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }
}