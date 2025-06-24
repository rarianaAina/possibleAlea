package com.example.erpnextintegration.service;

import com.example.erpnextintegration.dto.ClientDTO;
import com.example.erpnextintegration.dto.CommandeDTO;
import com.example.erpnextintegration.dto.ItemDTO;
import com.example.erpnextintegration.dto.ProjectDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour les fonctionnalités du module Vente.
 */
@Service
public class VenteService {

    @Autowired
    private ErpnextApiService erpnextApiService;
    
    /**
     * Récupère la liste des clients.
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
     * Récupère un client par son ID.
     */
    public Map<String, Object> getClientById(String id, String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Customer/" + id,
                apiKey,
                apiSecret);
        
        return response.getBody();
    }
    
    /**
     * Ajoute un nouveau client.
     */
    public void ajouterClient(Map<String, String> formData, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Customer");
        requestBody.put("customer_name", formData.get("customerName"));
        requestBody.put("customer_type", formData.get("customerType"));
        requestBody.put("customer_group", formData.get("customerGroup"));
        requestBody.put("territory", formData.get("territory"));
        
        if (formData.containsKey("email") && !formData.get("email").isEmpty()) {
            requestBody.put("email_id", formData.get("email"));
        }
        
        if (formData.containsKey("phone") && !formData.get("phone").isEmpty()) {
            requestBody.put("mobile_no", formData.get("phone"));
        }
        
        erpnextApiService.post(
                "/api/resource/Customer",
                requestBody,
                apiKey,
                apiSecret);
    }
    
    /**
     * Récupère la liste des commandes.
     */
    public List<CommandeDTO> getCommandes(String apiKey, String apiSecret) {


        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Sales Order?fields=[\"name\",\"owner\",\"creation\",\"modified\",\"modified_by\",\"customer\",\"order_type\",\"transaction_date\",\"total_qty\",\"base_total\",\"total\",\"status\",\"delivery_date\",\"customer_name\",\"currency\"]",
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
        List<CommandeDTO> commandes = data.stream()
                .map(item -> mapper.convertValue(item, CommandeDTO.class))
                .collect(Collectors.toList());

        return commandes;

    }
    
    /**
     * Récupère une commande par son ID.
     */
    public Map<String, Object> getCommandeById(String id, String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Sales Order/" + id,
                apiKey,
                apiSecret);
        
        return response.getBody();
    }
    
    /**
     * Récupère la liste des articles.
     */
    public List<ItemDTO> getItems(String apiKey, String apiSecret) {

        String fields = "[\"item_code\"]";

        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Item?fields=" + fields,
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
        List<ItemDTO> items = data.stream()
                .map(item -> mapper.convertValue(item, ItemDTO.class))
                .collect(Collectors.toList());

        return items;
    }


    /**
     * Ajoute une nouvelle commande.
     */
    public void ajouterCommande(Map<String, String> formData, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Sales Order");
        requestBody.put("customer", formData.get("customer"));
        requestBody.put("transaction_date", formData.get("transactionDate"));
        requestBody.put("delivery_date", formData.get("deliveryDate"));
        
        // Traitement des articles de la commande
        // Cette partie est simplifiée et devrait être adaptée selon le format réel des données du formulaire
        
        erpnextApiService.post(
                "/api/resource/Sales Order",
                requestBody,
                apiKey,
                apiSecret);
    }
}