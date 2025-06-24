package com.example.erpnextintegration.service;

import com.example.erpnextintegration.dto.payroll.SalaireDTO;
import com.example.erpnextintegration.dto.statistiques.StatistiquesMensuellesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class StatistiquesService {

    private final SalaireService salaireService;

    @Autowired
    public StatistiquesService(SalaireService salaireService) {
        this.salaireService = salaireService;
    }

    public List<StatistiquesMensuellesDTO> obtenirStatistiquesParAnnee(Integer annee) {
        List<StatistiquesMensuellesDTO> statistiques = new ArrayList<>();

        for (int mois = 1; mois <= 12; mois++) {
            List<SalaireDTO> salaires = salaireService.obtenirTousSalaires(mois, annee);

            if (!salaires.isEmpty()) {
                StatistiquesMensuellesDTO statsDto = new StatistiquesMensuellesDTO();
                statsDto.setMois(mois);
                statsDto.setAnnee(annee);
                statsDto.setNombreEmployes(salaires.size());

                // Calcul du total des salaires
                BigDecimal totalSalaires = salaires.stream()
                        .map(SalaireDTO::getSalaireNet)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                statsDto.setTotalSalaires(totalSalaires);

                // Calcul des totaux par composante
                Map<String, BigDecimal> totalParComposante = new HashMap<>();
                salaires.stream()
                        .flatMap(s -> s.getComposantes().stream())
                        .forEach(comp -> {
                            totalParComposante.merge(
                                    comp.getNom(),
                                    comp.getMontant(),
                                    BigDecimal::add
                            );
                        });
                statsDto.setTotalParComposante(totalParComposante);

                statistiques.add(statsDto);
            }
        }

        return statistiques;
    }

    public List<SalaireDTO> obtenirDetailsSalairesParMois(Integer annee, Integer mois) {
        return salaireService.obtenirTousSalaires(mois, annee);
    }

    public List<Integer> obtenirAnneesDisponibles() {
        int anneeCourante = LocalDate.now().getYear();
        return IntStream.rangeClosed(anneeCourante - 4, anneeCourante)
                .boxed()
                .collect(Collectors.toList());
    }
}