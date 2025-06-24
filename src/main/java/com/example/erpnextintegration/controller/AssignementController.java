package com.example.erpnextintegration.controller;


import com.example.erpnextintegration.service.EmployeService;
import com.example.erpnextintegration.service.AssignementService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/assignement")
public class AssignementController {

    @Autowired
    private EmployeService employeService;

    @Autowired
    private AssignementService assignementService;

    @GetMapping
    public String afficherFormulaire(Model model) {
        model.addAttribute("employes", employeService.rechercherEmployes(null));
        return "assign/index";
    }

    @PostMapping("/nouveau")
    public String creerDemandeConge(@RequestParam String employeId,
                                    @RequestParam BigDecimal baseSalary,@RequestParam String fromDate,@RequestParam String toDate, RedirectAttributes redirectAttributes) {
        String structureName = "g1";
        String company = "Orinasa SA";
        System.out.println("Nantsoina");
        try {
            assignementService.attribuerStructureSalariale(structureName, employeId, company, baseSalary, fromDate, toDate);
            redirectAttributes.addFlashAttribute("success",
                    "La demande est OK.");
            return "redirect:/employees";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création : " + e.getMessage());
            return "redirect:/assignement";
        }
    }

/*    @PostMapping("/assign")
    public Map<String, Object> assignStructure(
            HttpSession session,
            @RequestParam List<String> employeeIds,
            @RequestParam String fromDate,
            @RequestParam Double baseSalary) {

        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate startDate = LocalDate.parse(fromDate);
            Map<String, String> assignmentResults = assignementService.assignStructure(
                    session, employeeIds, startDate, baseSalary
            );

            response.put("status", "success");
            response.put("results", assignmentResults);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }*/

/*    @PostMapping("/assign")
    public Map<String, Object> assignStructure(
            HttpSession session,
            @RequestBody Map<String, Object> requestBody) {

        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<String> employeeIds = (List<String>) requestBody.get("employeeIds");
            String fromDate = (String) requestBody.get("fromDate");
            Double baseSalary = Double.parseDouble(requestBody.get("baseSalary").toString());

            LocalDate startDate = LocalDate.parse(fromDate);
            Map<String, String> assignmentResults = assignementService.assignStructure(
                    session, employeeIds, startDate, baseSalary
            );

            response.put("status", "success");
            response.put("results", assignmentResults);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }*/

    @PostMapping("/assign")
    public String assignStructure(
            HttpSession session,
            @RequestBody Map<String, Object> requestBody) {

        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<String> employeeIds = (List<String>) requestBody.get("employeeIds");
            String fromDate = (String) requestBody.get("fromDate");


            Double baseSalary = null;
            if (requestBody.containsKey("baseSalary") && requestBody.get("baseSalary") != null && !requestBody.get("baseSalary").toString().isBlank()) {
                baseSalary = Double.parseDouble(requestBody.get("baseSalary").toString());
            }

            LocalDate startDate = LocalDate.parse(fromDate);
            Map<String, String> assignmentResults = assignementService.assignStructure(
                    session, employeeIds, startDate, baseSalary
            );
/*
            response.put("status", "success");
            response.put("results", assignmentResults);*/
            return "redirect:/assignement";
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response.toString();
    }

}
