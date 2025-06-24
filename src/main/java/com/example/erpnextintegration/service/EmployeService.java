package com.example.erpnextintegration.service;


import com.example.erpnextintegration.dto.employee.EmployeeDTO;
import com.example.erpnextintegration.dto.employee.RechercheFiltreDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public EmployeService(
            RestTemplate restTemplate,
            @Value("${erpnext.api.url}") String apiUrl,
            @Value("${erpnext.api.api-key}") String apiKey,
            @Value("${erpnext.api.api-secret}") String apiSecret) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public List<EmployeeDTO> rechercherEmployes(RechercheFiltreDTO filtre) {
        HttpHeaders headers = creerEntetes();

        StringBuilder filters = new StringBuilder("[");
        List<String> conditions = new ArrayList<>();

        if (filtre != null) {
            if (filtre.getNom() != null && !filtre.getNom().isEmpty()) {
                conditions.add("[\"Employee\", \"employee_name\", \"like\", \"%" + filtre.getNom() + "%\"]");
            }

            if (filtre.getDepartement() != null && !filtre.getDepartement().isEmpty()) {
                conditions.add("[\"Employee\", \"department\", \"=\", \"" + filtre.getDepartement() + "\"]");
            }

            if (filtre.getDesignation() != null && !filtre.getDesignation().isEmpty()) {
                conditions.add("[\"Employee\", \"designation\", \"=\", \"" + filtre.getDesignation() + "\"]");
            }

            if (filtre.getStatut() != null && !filtre.getStatut().isEmpty()) {
                conditions.add("[\"Employee\", \"status\", \"=\", \"" + filtre.getStatut() + "\"]");
            }
        }

        if (!conditions.isEmpty()) {
            filters.append(String.join(", ", conditions));
        }
        filters.append("]");

        String url = apiUrl + "/api/resource/Employee?fields=[\"*\"]";
        if (!conditions.isEmpty()) {
            url += "&filters=" + filters.toString();
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .map(this::mapEmployeDTO)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public EmployeeDTO obtenirEmployeParId(String id) {
        HttpHeaders headers = creerEntetes();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Employee/" + id,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return mapEmployeDTO(data);
        }

        return null;
    }

    public List<String> obtenirTousDepartements() {
        HttpHeaders headers = creerEntetes();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Department?fields=[\"name\"]",
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .map(dept -> (String) dept.get("name"))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private EmployeeDTO mapEmployeDTO(Map<String, Object> data) {
        EmployeeDTO employe = new EmployeeDTO();

        employe.setName((String) data.get("name")); // ID unique dans ERPNext
        employe.setFirst_name((String) data.get("first_name"));
        employe.setLastName((String) data.get("last_name"));
        employe.setEmail((String) data.get("company_email"));
        employe.setPhone((String) data.get("cell_number")); // Téléphone principal
        employe.setDepartment((String) data.get("department"));
        employe.setDesignation((String) data.get("designation")); // Poste
        employe.setEmployeeStatus((String) data.get("status")); // Statut de l'employé
        employe.setGender((String) data.get("gender"));

        // Adresse
        if (data.get("permanent_address") != null) {
            employe.setAddress((String) data.get("permanent_address"));
        }

        // Date d'embauche
        if (data.get("date_of_joining") != null) {
            employe.setDate_of_joining(LocalDate.parse(
                    (String) data.get("date_of_joining"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ));
        }

        return employe;
    }

    private HttpHeaders creerEntetes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        return headers;
    }
}