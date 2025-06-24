package com.example.erpnextintegration.controller;

import com.example.erpnextintegration.dto.payroll.FiltreSalaireDTO;
import com.example.erpnextintegration.dto.payroll.SalaireDTO;
import com.example.erpnextintegration.dto.payroll.ComposanteSalarialeDTO;
import com.example.erpnextintegration.service.SalaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/salaires")
public class SalaireController {

    private final SalaireService salaireService;

    public SalaireController(SalaireService salaireService) {
        this.salaireService = salaireService;
    }

    @GetMapping
    public String listerSalaires(Model model, @ModelAttribute FiltreSalaireDTO filtre) {
        appliquerValeursParDefaut(filtre);

        List<SalaireDTO> salaires = Optional.ofNullable(
                salaireService.obtenirTousSalaires(filtre.getMois(), filtre.getAnnee())
        ).orElse(Collections.emptyList());

        System.out.println(salaires);
        // Calcul des totaux globaux
        BigDecimal totalBrut = salaires.stream()
                .map(SalaireDTO::getSalaireBrut)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = salaires.stream()
                .map(SalaireDTO::getSalaireNet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SalaireDTO salaireAvecComposantes = salaires.stream()
                .filter(s -> s.getComposantes() != null && !s.getComposantes().isEmpty())
                .findFirst()
                .orElse(null);

        // Calcul des totaux par composante
        Map<String, BigDecimal> totauxComposantes = calculerTotauxComposantes(salaires);


        model.addAttribute("totalBrut", totalBrut);
        model.addAttribute("totalNet", totalNet);
        model.addAttribute("totauxComposantes", totauxComposantes);
        model.addAttribute("salaires", salaires);
        model.addAttribute("filtre", filtre);
        model.addAttribute("mois", Month.values());


        List<Integer> annees = salaireService.obtenirAnneesDisponibles();
        Collections.sort(annees, Collections.reverseOrder());
        model.addAttribute("annees", annees);

        return "salaires/liste";
    }

    private Map<String, BigDecimal> calculerTotauxComposantes(List<SalaireDTO> salaires) {
        return salaires.stream()
                .flatMap(s -> s.getComposantes().stream())
                .collect(Collectors.groupingBy(
                        ComposanteSalarialeDTO::getNom,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                ComposanteSalarialeDTO::getMontant,
                                BigDecimal::add
                        )
                ));
    }

    private void appliquerValeursParDefaut(FiltreSalaireDTO filtre) {
        if (filtre.getMois() == null) {
            filtre.setMois(LocalDate.now().getMonthValue());
        }
        if (filtre.getAnnee() == null) {
            filtre.setAnnee(LocalDate.now().getYear());
        }
    }
}