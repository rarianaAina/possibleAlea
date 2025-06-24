package com.example.erpnextintegration.service;


import com.example.erpnextintegration.dto.payroll.ComposanteSalarialeDTO;
import com.example.erpnextintegration.dto.payroll.SalaireDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SalaireService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public SalaireService(
            RestTemplate restTemplate,
            @Value("${erpnext.api.url}") String apiUrl,
            @Value("${erpnext.api.api-key}") String apiKey,
            @Value("${erpnext.api.api-secret}") String apiSecret) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

/*
    public List<SalaireDTO> obtenirTousSalaires(Integer mois, Integer annee) {
        HttpHeaders headers = creerEntetes();

        // Créer la condition de filtrage pour le mois et l'année
        String dateDebut = String.format("%04d-%02d-01", annee, mois);
        String dateFin = String.format("%04d-%02d-%02d", annee, mois,
                LocalDate.of(annee, mois, 1).lengthOfMonth());

        String filters = "[[\"Salary Slip\", \"posting_date\", \"between\", [\"" +
                dateDebut + "\", \"" + dateFin + "\"]]]";

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Salary Slip?fields=[\"*\"]&filters=" + filters,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .map(this::mapSalaireDTO)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
*/

/*    public List<SalaireDTO> obtenirTousSalaires(Integer mois, Integer annee) {
        HttpHeaders headers = creerEntetes();

        try {
            // Création des dates de début et fin
            String dateDebut = String.format("%04d-%02d-01", annee, mois);
            String dateFin = String.format("%04d-%02d-%02d", annee, mois,
                    LocalDate.of(annee, mois, 1).lengthOfMonth());

            // Construction de l'URL avec UriComponentsBuilder
*//*
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl + "/api/resource/Salary Slip")
                    .queryParam("fields", "[\"*\"]")
                    .queryParam("filters", String.format("[[\"Salary Slip\", \"start_date\", \"between\", [\"%s\", \"%s\"]]]",
                            dateDebut, dateFin));
*//*

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl + "/api/resource/Salary Slip")
                    .queryParam("fields", "[\"*\"]")
                    .queryParam("filters", String.format("[[\"Salary Slip\", \"start_date\", \"between\", [\"%s\", \"%s\"]]]", dateDebut, dateFin))
                    .queryParam("include_child_docs", "true"); // <- clé ici

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                return data.stream()
                        .map(this::mapSalaireDTO)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // Gestion des erreurs (à adapter selon vos besoins)
            e.printStackTrace();
        }

        return Collections.emptyList();
    }*/

    public List<SalaireDTO> obtenirTousSalaires(Integer mois, Integer annee) {
        HttpHeaders headers = creerEntetes();

        try {
            String dateDebut = String.format("%04d-%02d-01", annee, mois);
            String dateFin = String.format("%04d-%02d-%02d", annee, mois,
                    LocalDate.of(annee, mois, 1).lengthOfMonth());

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl + "/api/resource/Salary Slip")
                    .queryParam("fields", "[\"name\"]") // juste récupérer les IDs
                    .queryParam("filters", String.format("[[\"Salary Slip\", \"start_date\", \"between\", [\"%s\", \"%s\"]]]", dateDebut, dateFin));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

                // Pour chaque ID, on fait un appel complet
                return data.stream()
                        .map(d -> (String) d.get("name"))
                        .map(this::obtenirSalaireParId) // appel qui renvoie toutes les données avec composantes
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

/*
    public List<SalaireDTO> obtenirSalairesParEmploye(String employeId, Integer mois, Integer annee) {
        HttpHeaders headers = creerEntetes();

        String filters;

        if (mois != null && annee != null) {
            // Filtrer par employé, mois et année
            String dateDebut = String.format("%04d-%02d-01", annee, mois);
            String dateFin = String.format("%04d-%02d-%02d", annee, mois,
                    LocalDate.of(annee, mois, 1).lengthOfMonth());

            filters = "[[\"Salary Slip\", \"employee\", \"=\", \"" + employeId + "\"], " +
                    "[\"Salary Slip\", \"posting_date\", \"between\", [\"" +
                    dateDebut + "\", \"" + dateFin + "\"]]]";
        } else {
            // Filtrer uniquement par employé
            filters = "[[\"Salary Slip\", \"employee\", \"=\", \"" + employeId + "\"]]";
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Salary Slip?fields=[\"*\"]&filters=" + filters,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .map(this::mapSalaireDTO)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
*/

    public List<SalaireDTO> obtenirSalairesParEmploye(String employeId, Integer mois, Integer annee) {
        HttpHeaders headers = creerEntetes();

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl + "/api/resource/Salary Slip")
                    .queryParam("fields", "[\"*\"]");

            if (mois != null && annee != null) {
                String dateDebut = String.format("%04d-%02d-01", annee, mois);
                String dateFin = String.format("%04d-%02d-%02d", annee, mois,
                        LocalDate.of(annee, mois, 1).lengthOfMonth());

                String filters = String.format(
                        "[[\"Salary Slip\", \"employee\", \"=\", \"%s\"], [\"Salary Slip\", \"start_date\", \"between\", [\"%s\", \"%s\"]]]",
                        employeId, dateDebut, dateFin);

                builder.queryParam("filters", filters);
            } else {
                String filters = String.format("[[\"Salary Slip\", \"employee\", \"=\", \"%s\"]]", employeId);
                builder.queryParam("filters", filters);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                return data.stream()
                        .map(this::mapSalaireDTO)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // Gérer l'exception selon vos besoins
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
    public SalaireDTO obtenirSalaireParId(String id) {
        HttpHeaders headers = creerEntetes();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Salary Slip/" + id,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return mapSalaireDTO(data);
        }

        return null;
    }

    public List<Integer> obtenirAnneesDisponibles() {
        // Retourne les 5 dernières années
        int anneeCourante = LocalDate.now().getYear();
        return IntStream.rangeClosed(anneeCourante - 4, anneeCourante)
                .boxed()
                .collect(Collectors.toList());
    }

    private SalaireDTO mapSalaireDTO(Map<String, Object> data) {
        SalaireDTO salaire = new SalaireDTO();

        salaire.setId((String) data.get("name"));
        salaire.setEmployeId((String) data.get("employee"));
        salaire.setNomEmploye((String) data.get("employee_name"));

        // Date de paiement
        if (data.get("posting_date") != null) {
            LocalDate datePaiement = LocalDate.parse(
                    (String) data.get("posting_date"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            );
            salaire.setDatePaiement(datePaiement);
            salaire.setMois(datePaiement.getMonthValue());
            salaire.setAnnee(datePaiement.getYear());
        }

        // Montants
        if (data.get("gross_pay") != null) {
            salaire.setSalaireBrut(new BigDecimal(data.get("gross_pay").toString()));
        }

        if (data.get("net_pay") != null) {
            salaire.setSalaireNet(new BigDecimal(data.get("net_pay").toString()));
        }

        // Composantes salariales
        List<ComposanteSalarialeDTO> composantes = new ArrayList<>();

        // Gains
        if (data.get("earnings") instanceof List) {
            List<Map<String, Object>> earnings = (List<Map<String, Object>>) data.get("earnings");
            for (Map<String, Object> earning : earnings) {
                ComposanteSalarialeDTO composante = new ComposanteSalarialeDTO();
                composante.setId((String) earning.get("name"));
                composante.setNom((String) earning.get("salary_component"));
                composante.setType("GAIN");
                composante.setMontant(new BigDecimal(earning.get("amount").toString()));
                composante.setDescription((String) earning.get("abbr"));
                composantes.add(composante);
            }
        }

        // Déductions
        if (data.get("deductions") instanceof List) {
            List<Map<String, Object>> deductions = (List<Map<String, Object>>) data.get("deductions");
            for (Map<String, Object> deduction : deductions) {
                ComposanteSalarialeDTO composante = new ComposanteSalarialeDTO();
                composante.setId((String) deduction.get("name"));
                composante.setNom((String) deduction.get("salary_component"));
                composante.setType("DEDUCTION");
                composante.setMontant(new BigDecimal(deduction.get("amount").toString()));
                composante.setDescription((String) deduction.get("abbr"));
                composantes.add(composante);
            }
        }

        salaire.setComposantes(composantes);

        return salaire;
    }

    private HttpHeaders creerEntetes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        return headers;
    }
}