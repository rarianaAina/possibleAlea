package com.example.erpnextintegration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.erpnextintegration.dto.LoginResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour gérer l'authentification avec ERPNext.
 */
@Service
public class AuthService {

    @Value("${erpnext.api.url}")
    private String erpnextApiUrl;
    
    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.api-key}")
    private String apiKeyFromProperties;

    @Value("${erpnext.api.api-secret}")
    private String apiSecretFromProperties;


    /**
     * Authentification avec ERPNext.
     * 
     * @param username Nom d'utilisateur
     * @param password Mot de passe
     * @return Réponse d'authentification contenant les clés API
     */
/*
    public LoginResponse login(String username, String password) {
        String loginUrl = erpnextApiUrl + "/api/method/login";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("usr", username);
        requestBody.put("pwd", password);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        
        // Authentification avec ERPNext
        restTemplate.postForEntity(loginUrl, request, Map.class);
        
        // Récupération des clés API
        return getApiKey(username);
    }
*/

    public LoginResponse login(String username, String password) {
        String loginUrl = erpnextApiUrl + "/api/method/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("usr", username);
        requestBody.put("pwd", password);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // Authentification avec ERPNext et capture de la réponse complète
        ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

        // Récupération du cookie de session (SID) depuis les en-têtes
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        String sid = extractSidFromCookie(setCookieHeader);

        // Récupération des clés API
        LoginResponse loginResponse = getApiKey(username);
        loginResponse.setSid(sid); // Ajout du SID à la réponse

        return loginResponse;
    }

    private String extractSidFromCookie(String setCookieHeader) {
        if (setCookieHeader != null) {
            String[] cookies = setCookieHeader.split(";");
            for (String cookie : cookies) {
                if (cookie.trim().startsWith("sid=")) {
                    return cookie.trim().substring(4);
                }
            }
        }
        return null;
    }

    private LoginResponse getApiKey(String username) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setApiKey(apiKeyFromProperties);
        loginResponse.setApiSecret(apiSecretFromProperties);
        loginResponse.setUserName(username);
        return loginResponse;
    }

}