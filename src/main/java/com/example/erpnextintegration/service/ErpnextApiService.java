package com.example.erpnextintegration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * Service pour interagir avec l'API ERPNext.
 */
@Service
public class ErpnextApiService {

    @Value("${erpnext.api.url}")
    private String erpnextApiUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Effectue une requête GET vers l'API ERPNext.
     * 
     * @param endpoint Point de terminaison de l'API
     * @param apiKey Clé API
     * @param apiSecret Secret API
     * @return Réponse de l'API
     */
    public ResponseEntity<Map> get(String endpoint, String apiKey, String apiSecret) {
        HttpHeaders headers = createHeaders(apiKey, apiSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(
                erpnextApiUrl + endpoint,
                HttpMethod.GET,
                entity,
                Map.class);
    }
    
    /**
     * Effectue une requête POST vers l'API ERPNext.
     * 
     * @param endpoint Point de terminaison de l'API
     * @param requestBody Corps de la requête
     * @param apiKey Clé API
     * @param apiSecret Secret API
     * @return Réponse de l'API
     */
    public ResponseEntity<Map> post(String endpoint, Object requestBody, String apiKey, String apiSecret) {
        HttpHeaders headers = createHeaders(apiKey, apiSecret);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        
        return restTemplate.exchange(
                erpnextApiUrl + endpoint,
                HttpMethod.POST,
                entity,
                Map.class);
    }
    
    /**
     * Effectue une requête PUT vers l'API ERPNext.
     * 
     * @param endpoint Point de terminaison de l'API
     * @param requestBody Corps de la requête
     * @param apiKey Clé API
     * @param apiSecret Secret API
     * @return Réponse de l'API
     */
    public ResponseEntity<Map> put(String endpoint, Object requestBody, String apiKey, String apiSecret) {
        HttpHeaders headers = createHeaders(apiKey, apiSecret);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        
        return restTemplate.exchange(
                erpnextApiUrl + endpoint,
                HttpMethod.PUT,
                entity,
                Map.class);
    }
    
    /**
     * Crée les en-têtes HTTP avec authentification pour l'API ERPNext.
     * 
     * @param apiKey Clé API
     * @param apiSecret Secret API
     * @return En-têtes HTTP
     */
    private HttpHeaders createHeaders(String apiKey, String apiSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String auth = apiKey + ":" + apiSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.add("Authorization", "Basic " + encodedAuth);
        
        return headers;
    }
}