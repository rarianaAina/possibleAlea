package com.example.erpnextintegration.controller;

import com.example.erpnextintegration.dto.payroll.FiltreSalaireDTO;
import com.example.erpnextintegration.dto.payroll.SalaireDTO;
import com.example.erpnextintegration.dto.statistiques.StatistiquesMensuellesDTO;
import com.example.erpnextintegration.service.SalaireService;
import com.example.erpnextintegration.service.StatistiquesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/statistiques")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    @Autowired
    private SalaireService salaireService;

    @Autowired
    public StatistiquesController(StatistiquesService statistiquesService) {
        this.statistiquesService = statistiquesService;
    }

/*    @GetMapping
    public String afficherStatistiques(Model model, @RequestParam(required = false) Integer annee) {
        if (annee == null) {
            annee = java.time.Year.now().getValue();
        }

        List<StatistiquesMensuellesDTO> statistiques = statistiquesService.obtenirStatistiquesParAnnee(annee);
        List<Integer> annees = statistiquesService.obtenirAnneesDisponibles();

        model.addAttribute("statistiques", statistiques);
        model.addAttribute("annees", annees);
        model.addAttribute("anneeSelectionnee", annee);

        return "statistiques/index";
    }*/

    @GetMapping
    public String afficherStatistiques(Model model, @RequestParam(required = false) Integer annee) {
        if (annee == null) {
            annee = java.time.Year.now().getValue();
        }

        // Obtenir les statistiques existantes
        List<StatistiquesMensuellesDTO> statistiquesExistantes = statistiquesService.obtenirStatistiquesParAnnee(annee);
        List<Integer> annees = statistiquesService.obtenirAnneesDisponibles();

        // Créer une liste complète pour tous les mois
        List<StatistiquesMensuellesDTO> statistiquesCompletes = new ArrayList<>();

        // Récupérer les clés des composantes (si des données existent)
        Set<String> composantes = statistiquesExistantes.isEmpty() ?
                Collections.emptySet() :
                statistiquesExistantes.get(0).getTotalParComposante().keySet();

        for (int mois = 1; mois <= 12; mois++) {
            final int currentMois = mois;
            Optional<StatistiquesMensuellesDTO> statExistante = statistiquesExistantes.stream()
                    .filter(s -> s.getMois() == currentMois)
                    .findFirst();

            if (statExistante.isPresent()) {
                statistiquesCompletes.add(statExistante.get());
            } else {
                // Créer une statistique vide pour ce mois
                StatistiquesMensuellesDTO statVide = new StatistiquesMensuellesDTO();
                statVide.setAnnee(annee);
                statVide.setMois(currentMois);
                statVide.setNombreEmployes(0);
                statVide.setTotalSalaires(BigDecimal.ZERO);

                // Initialiser les composantes à 0
                Map<String, BigDecimal> composantesVides = new LinkedHashMap<>();
                composantes.forEach(c -> composantesVides.put(c, BigDecimal.ZERO));
                statVide.setTotalParComposante(composantesVides);

                statistiquesCompletes.add(statVide);
            }
        }

        model.addAttribute("statistiques", statistiquesCompletes);
        model.addAttribute("annees", annees);
        model.addAttribute("anneeSelectionnee", annee);

        return "statistiques/index";
    }

    @GetMapping("/{annee}/{mois}")
    public String afficherDetailMois(@PathVariable Integer annee,
                                     @PathVariable Integer mois,
                                     Model model,
                                     @ModelAttribute FiltreSalaireDTO filtre) {

        List<SalaireDTO> salaires = Optional.ofNullable(
                salaireService.obtenirTousSalaires(filtre.getMois(), filtre.getAnnee())
        ).orElse(Collections.emptyList());

        // Calcul des totaux globaux
        BigDecimal totalBrut = salaires.stream()
                .map(SalaireDTO::getSalaireBrut)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = salaires.stream()
                .map(SalaireDTO::getSalaireNet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcul des totaux par composante
        Map<String, BigDecimal> totauxComposantes = new LinkedHashMap<>();
        if (!salaires.isEmpty()) {
            // Initialiser avec toutes les composantes du premier salaire (pour garder l'ordre)
            salaires.get(0).getComposantes().forEach(c ->
                    totauxComposantes.put(c.getNom(), BigDecimal.ZERO));

            // Calculer les totaux
            salaires.forEach(salaire -> {
                salaire.getComposantes().forEach(composante -> {
                    totauxComposantes.merge(
                            composante.getNom(),
                            composante.getMontant(),
                            BigDecimal::add
                    );
                });
            });
        }

        model.addAttribute("totalBrut", totalBrut);
        model.addAttribute("totalNet", totalNet);
        model.addAttribute("totauxComposantes", totauxComposantes);
        model.addAttribute("detailsSalaires", salaires);
        model.addAttribute("annee", annee);
        model.addAttribute("mois", mois);

        return "statistiques/detail-mois";
    }
}