package com.example.erpnextintegration.service;


import com.example.erpnextintegration.dto.CongeDTO;
import com.example.erpnextintegration.dto.conge.DemandeCongeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CongeService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public CongeService(
            RestTemplate restTemplate,
            @Value("${erpnext.api.url}") String apiUrl,
            @Value("${erpnext.api.api-key}") String apiKey,
            @Value("${erpnext.api.api-secret}") String apiSecret) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public List<CongeDTO> obtenirCongesParEmploye(String employeId) {
        HttpHeaders headers = creerEntetes();

        String filters = "[[\"Leave Application\", \"employee\", \"=\", \"" + employeId + "\"]]";

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Leave Application?fields=[\"*\"]&filters=" + filters,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .map(this::mapCongeDTO)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public List<CongeDTO> obtenirTousConges() {
        HttpHeaders headers = creerEntetes();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Leave Application?fields=[\"*\"]",
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .map(this::mapCongeDTO)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public CongeDTO creerDemandeConge(DemandeCongeDTO demande) {
        HttpHeaders headers = creerEntetes();

        // Calculer le nombre de jours
        long nombreJours = ChronoUnit.DAYS.between(demande.getDateDebut(), demande.getDateFin()) + 1;

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("employee", demande.getEmployeId());
        requestMap.put("leave_type", demande.getTypeConge());
        requestMap.put("from_date", demande.getDateDebut().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        requestMap.put("to_date", demande.getDateFin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        requestMap.put("total_leave_days", nombreJours);
        requestMap.put("description", demande.getMotif());
        requestMap.put("status", "Open");

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Leave Application",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return mapCongeDTO(data);
        }

        throw new RuntimeException("Erreur lors de la création de la demande de congé");
    }

    public void approuverConge(String congeId) {
        modifierStatutConge(congeId, "Approved");
    }

    public void rejeterConge(String congeId) {
        modifierStatutConge(congeId, "Rejected");
    }

    private void modifierStatutConge(String congeId, String statut) {
        HttpHeaders headers = creerEntetes();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("status", statut);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

        restTemplate.exchange(
                apiUrl + "/api/resource/Leave Application/" + congeId,
                HttpMethod.PUT,
                requestEntity,
                Map.class
        );
    }

    public List<String> obtenirTypesConge() {
        HttpHeaders headers = creerEntetes();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Leave Type?fields=[\"name\"]",
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .map(type -> (String) type.get("name"))
                    .collect(Collectors.toList());
        }

        return Arrays.asList("Annual Leave", "Sick Leave", "Casual Leave");
    }

    private CongeDTO mapCongeDTO(Map<String, Object> data) {
        CongeDTO conge = new CongeDTO();

        conge.setId((String) data.get("name"));
        conge.setEmployeId((String) data.get("employee"));
        conge.setNomEmploye((String) data.get("employee_name"));
        conge.setTypeConge((String) data.get("leave_type"));
        conge.setMotif((String) data.get("description"));
        conge.setStatut((String) data.get("status"));

        if (data.get("from_date") != null) {
            conge.setDateDebut(LocalDate.parse(
                    (String) data.get("from_date"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ));
        }

        if (data.get("to_date") != null) {
            conge.setDateFin(LocalDate.parse(
                    (String) data.get("to_date"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ));
        }

        if (data.get("total_leave_days") != null) {
            conge.setNombreJours(((Number) data.get("total_leave_days")).intValue());
        }

        if (data.get("creation") != null) {
            String creationStr = (String) data.get("creation");
            conge.setDateCreation(LocalDate.parse(
                    creationStr.substring(0, 10),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ));
        }

        return conge;
    }

    private HttpHeaders creerEntetes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        return headers;
    }
}