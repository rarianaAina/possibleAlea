package com.example.erpnextintegration.controller.alea;


import com.example.erpnextintegration.dto.employee.EmployeeDTO;
import com.example.erpnextintegration.dto.payroll.SalaireDTO;
import com.example.erpnextintegration.service.EmployeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.erpnextintegration.service.PayrollService;
import com.example.erpnextintegration.service.alea.HRService;
import com.example.erpnextintegration.dto.alea.SalarySlipFilterDTO;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/paie/bulletins")
public class SalarySlipFilterController {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private HRService hrService;

    @Autowired
    private EmployeService employeService;

    @GetMapping("/filtrer")
    public String pageFiltreBulletins(HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");
        List<EmployeeDTO> employes = employeService.rechercherEmployes(null);
        System.out.println(employes);

        model.addAttribute("composants", payrollService.getComposantsSalariaux(apiKey, apiSecret));
        model.addAttribute("employes", employes);

        model.addAttribute("departements", hrService.getDepartements(apiKey, apiSecret));
        return "paie/bulletins/filtrer";
    }

    @PostMapping("/filtrer")
    public String filtrerBulletins(@ModelAttribute SalarySlipFilterDTO filter,
                                   HttpSession session, Model model) {
        String apiKey = (String) session.getAttribute("apiKey");
        String apiSecret = (String) session.getAttribute("apiSecret");

        try {
            model.addAttribute("bulletins", payrollService.getBulletinsFiltres(filter, apiKey, apiSecret));
            model.addAttribute("filter", filter);
            model.addAttribute("composants", payrollService.getComposantsSalariaux(apiKey, apiSecret));
            model.addAttribute("employes", hrService.getEmployes(apiKey, apiSecret));
            model.addAttribute("departements", hrService.getDepartements(apiKey, apiSecret));
            return "paie/bulletins/resultats-filtre";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("composants", payrollService.getComposantsSalariaux(apiKey, apiSecret));
            model.addAttribute("employes", hrService.getEmployes(apiKey, apiSecret));
            model.addAttribute("departements", hrService.getDepartements(apiKey, apiSecret));
            return "paie/bulletins/filtrer";
        }
    }
}