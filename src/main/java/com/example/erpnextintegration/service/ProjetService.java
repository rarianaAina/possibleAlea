package com.example.erpnextintegration.service;

import com.example.erpnextintegration.dto.ClientDTO;
import com.example.erpnextintegration.dto.ProjectDTO;
import com.example.erpnextintegration.dto.TaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour les fonctionnalités du module Projet.
 */
@Service
public class ProjetService {

    @Autowired
    private ErpnextApiService erpnextApiService;
    
    /**
     * Récupère la liste des projets.
     */

    public List<ProjectDTO> getProjets(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Project?fields=[\"*\"]",
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
        List<ProjectDTO> projets = data.stream()
                .map(item -> mapper.convertValue(item, ProjectDTO.class))
                .collect(Collectors.toList());

        return projets;
    }

    /**
     * Récupère un projet par son ID.
     */
    public ProjectDTO getProjetById(String id, String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Project/" + id,
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
        return mapper.convertValue(data, ProjectDTO.class);
    }

    /**
     * Récupère la liste des clients.
     */
/*    public Map<String, Object> getClients(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Customer?fields=[\"*\"]",
                apiKey,
                apiSecret);
        
        return response.getBody();
    }
    */

    public List<ClientDTO> getClients(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Customer?fields=[\"*\"]",
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
        List<ClientDTO> clients = data.stream()
                .map(item -> mapper.convertValue(item, ClientDTO.class))
                .collect(Collectors.toList());

        return clients;
    }

    /**
     * Ajoute un nouveau projet.
     */
    public void ajouterProjet(Map<String, String> formData, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Project");
        requestBody.put("project_name", formData.get("projectName"));
        requestBody.put("status", "Open");
        
        if (formData.containsKey("customer") && !formData.get("customer").isEmpty()) {
            requestBody.put("customer", formData.get("customer"));
        }
        
        if (formData.containsKey("expectedStartDate") && !formData.get("expectedStartDate").isEmpty()) {
            requestBody.put("expected_start_date", formData.get("expectedStartDate"));
        }
        
        if (formData.containsKey("expectedEndDate") && !formData.get("expectedEndDate").isEmpty()) {
            requestBody.put("expected_end_date", formData.get("expectedEndDate"));
        }
        
        erpnextApiService.post(
                "/api/resource/Project",
                requestBody,
                apiKey,
                apiSecret);
    }
    
    /**
     * Récupère les tâches d'un projet.
     */
    public List<TaskDTO> getTachesByProjet(String projetId, String apiKey, String apiSecret) {
        // Liste des champs importants à récupérer dans la requête API
        String fields = "[\"name\",\"subject\",\"project\",\"status\",\"priority\",\"exp_start_date\",\"exp_end_date\",\"owner\",\"description\",\"progress\"]";

        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Task?filters=[[\"project\",\"=\",\"" + projetId + "\"]]&fields=" + fields,
                apiKey,
                apiSecret);

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("data")) {
            return List.of(); // retourne une liste vide si aucune donnée
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

        return data.stream()
                .map(item -> mapper.convertValue(item, TaskDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Ajoute une nouvelle tâche à un projet.
     */
    public void ajouterTache(String projetId, Map<String, String> formData, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Task");
        requestBody.put("subject", formData.get("subject"));
        requestBody.put("project", projetId);
        requestBody.put("status", "Open");
        
        if (formData.containsKey("description") && !formData.get("description").isEmpty()) {
            requestBody.put("description", formData.get("description"));
        }
        
        if (formData.containsKey("expectedStartDate") && !formData.get("expectedStartDate").isEmpty()) {
            requestBody.put("exp_start_date", formData.get("expectedStartDate"));
        }
        
        if (formData.containsKey("expectedEndDate") && !formData.get("expectedEndDate").isEmpty()) {
            requestBody.put("exp_end_date", formData.get("expectedEndDate"));
        }
        
        if (formData.containsKey("priority") && !formData.get("priority").isEmpty()) {
            requestBody.put("priority", formData.get("priority"));
        }
        
        erpnextApiService.post(
                "/api/resource/Task",
                requestBody,
                apiKey,
                apiSecret);
    }
}