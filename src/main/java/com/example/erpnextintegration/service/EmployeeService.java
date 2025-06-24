package com.example.erpnextintegration.service;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.erpnextintegration.dto.ProjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.erpnextintegration.dto.employee.DepartmentDTO;
import com.example.erpnextintegration.dto.employee.DesignationDTO;
import com.example.erpnextintegration.dto.employee.EmployeeDTO;
import com.example.erpnextintegration.dto.employee.EmployeeListDTO;
import com.example.erpnextintegration.dto.hr.LeaveRequestDTO;
import com.example.erpnextintegration.exception.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private ErpnextApiService erpnextApiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<EmployeeDTO> getAllEmployees(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Employee?fields=[\"name\",\"first_name\",\"gender\"]",
                apiKey,
                apiSecret);

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("data")) {
            return List.of(); // liste vide si pas de données
        }

        ObjectMapper mapper = new ObjectMapper();

        // Récupération de la liste brute (liste de Map)
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

        // Conversion de chaque Map en ProjectDTO
        List<EmployeeDTO> projets = data.stream()
                .map(item -> mapper.convertValue(item, EmployeeDTO.class))
                .collect(Collectors.toList());

        return projets;
    }

/*
    public List<EmployeeDTO> getAllEmployees() throws ApiException {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("fields", "[\"*\"]");

            JsonNode response = apiService.get("/resource/Employee", JsonNode.class, params);

            if (response.has("data")) {
                return objectMapper.convertValue(
                        response.get("data"),
                        new TypeReference<List<EmployeeDTO>>() {}
                );
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des employés: ", e);
            throw new ApiException("Erreur lors de la récupération des employés: " + e.getMessage());
        }
    }
*/

    public EmployeeDTO getEmployeeById(String id, String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Employee/" + id,
                apiKey,
                apiSecret);

        Map<String, Object> body = response.getBody();

        if (body == null || !body.containsKey("data")) {
            return null; // ou tu peux lancer une exception personnalisée
        }

        ObjectMapper mapper = new ObjectMapper();

        // Récupération de l'objet "data" contenant le projet
        Map<String, Object> data = (Map<String, Object>) body.get("data");

        // Conversion en ProjectDTO
        return mapper.convertValue(data, EmployeeDTO.class);
    }

/*
    public EmployeeDTO getEmployeeById(String id) throws ApiException {
        try {
            JsonNode response = apiService.get("/resource/Employee/" + id, JsonNode.class);

            if (response.has("data")) {
                return objectMapper.convertValue(response.get("data"), EmployeeDTO.class);
            }

            throw new ApiException("Employé non trouvé");
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'employé: ", e);
            throw new ApiException("Erreur lors de la récupération de l'employé: " + e.getMessage());
        }
    }
*/

    public void ajouterEmployee(Map<String, String> formData, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Employee");
        requestBody.put("employee_name", formData.get("employeeName"));  // Nom de l'employé

        if (formData.containsKey("email") && !formData.get("email").isEmpty()) {
            requestBody.put("email_id", formData.get("email"));
        }

        if (formData.containsKey("employee_number") && !formData.get("employee_number").isEmpty()) {
            requestBody.put("employee_number", formData.get("employee_number"));
        }

        if (formData.containsKey("department") && !formData.get("department").isEmpty()) {
            requestBody.put("department", formData.get("department"));
        }

        if (formData.containsKey("designation") && !formData.get("designation").isEmpty()) {
            requestBody.put("designation", formData.get("designation"));
        }

        if (formData.containsKey("date_of_joining") && !formData.get("date_of_joining").isEmpty()) {
            requestBody.put("date_of_joining", formData.get("date_of_joining"));
        }

        erpnextApiService.post(
                "/api/resource/Employee",
                requestBody,
                apiKey,
                apiSecret);
    }

    public void updateEmployee(String employeeName, Map<String, String> formData, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();

        // Champs à mettre à jour, uniquement si présents et non vides
        if (formData.containsKey("employee_name") && !formData.get("employee_name").isEmpty()) {
            requestBody.put("employee_name", formData.get("employee_name"));
        }

        if (formData.containsKey("email") && !formData.get("email").isEmpty()) {
            requestBody.put("email_id", formData.get("email"));
        }

        if (formData.containsKey("employee_number") && !formData.get("employee_number").isEmpty()) {
            requestBody.put("employee_number", formData.get("employee_number"));
        }

        if (formData.containsKey("department") && !formData.get("department").isEmpty()) {
            requestBody.put("department", formData.get("department"));
        }

        if (formData.containsKey("designation") && !formData.get("designation").isEmpty()) {
            requestBody.put("designation", formData.get("designation"));
        }

        if (formData.containsKey("date_of_joining") && !formData.get("date_of_joining").isEmpty()) {
            requestBody.put("date_of_joining", formData.get("date_of_joining"));
        }

        // Appel à l'API via PUT
        ResponseEntity<Map> response = erpnextApiService.put(
                "/api/resource/Employee/" + employeeName,
                requestBody,
                apiKey,
                apiSecret);

        // Tu peux éventuellement gérer la réponse ici, par exemple :
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Employé mis à jour avec succès.");
        } else {
            System.err.println("Erreur lors de la mise à jour de l'employé : " + response.getStatusCode());
        }
    }

/*
    public void deleteEmployee(String id) throws ApiException {
        try {
            apiService.delete("/resource/Employee/" + id, JsonNode.class);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'employé: ", e);
            throw new ApiException("Erreur lors de la suppression de l'employé: " + e.getMessage());
        }
    }
*/

/*
    public List<DepartmentDTO> getAllDepartments() throws ApiException {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("fields", "[\"*\"]");

            JsonNode response = apiService.get("/resource/Department", JsonNode.class, params);

            if (response.has("data")) {
                return objectMapper.convertValue(
                        response.get("data"),
                        new TypeReference<List<DepartmentDTO>>() {}
                );
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des départements: ", e);
            throw new ApiException("Erreur lors de la récupération des départements: " + e.getMessage());
        }
    }

    public List<DesignationDTO> getAllDesignations() throws ApiException {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("fields", "[\"*\"]");

            JsonNode response = apiService.get("/resource/Designation", JsonNode.class, params);

            if (response.has("data")) {
                return objectMapper.convertValue(
                        response.get("data"),
                        new TypeReference<List<DesignationDTO>>() {}
                );
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des désignations: ", e);
            throw new ApiException("Erreur lors de la récupération des désignations: " + e.getMessage());
        }
    }
*/

/*
    public List<LeaveRequestDTO> getPendingLeaveRequests() throws ApiException {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("fields", "[\"*\"]");
            params.put("filters", "[[\"status\",\"=\",\"Open\"]]");

            JsonNode response = apiService.get("/resource/Leave Application", JsonNode.class, params);

            if (response.has("data")) {
                return objectMapper.convertValue(
                        response.get("data"),
                        new TypeReference<List<LeaveRequestDTO>>() {}
                );
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des demandes de congé: ", e);
            throw new ApiException("Erreur lors de la récupération des demandes de congé: " + e.getMessage());
        }
    }
*/

    public List<LeaveRequestDTO> getAllLeaveRequests(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Leave Application?fields=[\"*\"]",
                apiKey,
                apiSecret);

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("data")) {
            return List.of(); // liste vide si pas de données
        }

        ObjectMapper mapper = new ObjectMapper();

        // Récupération de la liste brute (liste de Map)
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

        // Conversion de chaque Map en LeaveRequestDTO
        List<LeaveRequestDTO> leaveRequests = data.stream()
                .map(item -> mapper.convertValue(item, LeaveRequestDTO.class))
                .collect(Collectors.toList());

        return leaveRequests;
    }

/*
    public int getEmployeeCount() throws ApiException {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("fields", "[\"count(*) as total\"]");

            JsonNode response = apiService.get("/resource/Employee", JsonNode.class, params);

            if (response.has("data") && response.get("data").isArray() && response.get("data").size() > 0) {
                return response.get("data").get(0).path("total").asInt();
            }

            return 0;
        } catch (Exception e) {
            log.error("Erreur lors du comptage des employés: ", e);
            throw new ApiException("Erreur lors du comptage des employés: " + e.getMessage());
        }
    }
*/

/*    public int getPendingLeaveRequestsCount() throws ApiException {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("fields", "[\"count(*) as total\"]");
            params.put("filters", "[[\"status\",\"=\",\"Open\"]]");

            JsonNode response = apiService.get("/resource/Leave Application", JsonNode.class, params);

            if (response.has("data") && response.get("data").isArray() && response.get("data").size() > 0) {
                return response.get("data").get(0).path("total").asInt();
            }

            return 0;
        } catch (Exception e) {
            log.error("Erreur lors du comptage des demandes de congé: ", e);
            throw new ApiException("Erreur lors du comptage des demandes de congé: " + e.getMessage());
        }
    }*/
}