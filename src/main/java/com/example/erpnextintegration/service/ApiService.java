/*
Corrige ce code pour que :
        - Lors de la création de la structure salariale, aucun montant ne sera ajouté pour les composantes salariales mais lors de la création d'une Salary Slip, c'est la que le montant de chaque composante salariale sera ajouté.
        Et toujours concernant le montant des composantes salariales : si dans la colonne valeur c'est 30% et dans la colonne remarque c'est salaire base + indemnité donc on prend le salaire de base pour le mois concerné et l'indemnité
*/

/*

Ref,Nom,Prenom,genre,Date embauche,date naissance,company
        1,Rakoto,Alain,Masculin,03/04/2024,01/01/1980,My Company
        2,Rasoa,Jeanne,Feminin,08/06/2024,01/01/1990,My Company

        salary structure,name,Abbr,type,valeur,company
        gasy1,Salaire Base,SB,earning,base,My Company
        gasy1,IndemnitÃ©,IND,earning,SB * 0.3,My Company
        gasy1,Taxe sociale,TS,deduction,(SB + IND) * 0.2,My Company

        Mois,Ref Employe,Salaire Base,Salaire
        01/04/2025,1,1500000,gasy1
        01/04/2025,2,900000,gasy1
        01/03/2025,1,1600000,gasy1
        01/03/2025,2,900000,gasy1

package com.example.erpnextintegration.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.erpnextintegration.config.ErpnextApiConfig;
import com.example.erpnextintegration.dto.auth.UserSessionDto;
import com.example.erpnextintegration.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
public class ApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserSessionDto userSessionDto;

    @Autowired
    private ErpnextApiConfig erpnextApiConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T get(String endpoint, Class<T> responseType) throws ApiException {
        return exchange(endpoint, HttpMethod.GET, null, responseType);
    }

    public <T> T get(String endpoint, Class<T> responseType, Map<String, String> queryParams) throws ApiException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(erpnextApiConfig.getBaseApiUrl() + endpoint);

        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }

        return exchange(builder.toUriString(), HttpMethod.GET, null, responseType);
    }

    public <T, R> T post(String endpoint, R body, Class<T> responseType) throws ApiException {
        return exchange(endpoint, HttpMethod.POST, body, responseType);
    }

    public <T, R> T put(String endpoint, R body, Class<T> responseType) throws ApiException {
        return exchange(endpoint, HttpMethod.PUT, body, responseType);
    }

    public <T> T delete(String endpoint, Class<T> responseType) throws ApiException {
        return exchange(endpoint, HttpMethod.DELETE, null, responseType);
    }

    private <T, R> T exchange(String endpoint, HttpMethod method, R body, Class<T> responseType) throws ApiException {
        try {
            HttpHeaders headers = createHeaders();

            HttpEntity<R> requestEntity = new HttpEntity<>(body, headers);

            String url = endpoint.startsWith("http") ? endpoint : erpnextApiConfig.getBaseApiUrl() + endpoint;

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                // Vérifier si ERPNext a renvoyé une erreur dans la réponse
                if (rootNode.has("exception") || rootNode.has("error")) {
                    String errorMessage = rootNode.has("exception")
                            ? rootNode.path("exception").asText()
                            : rootNode.path("error").asText();
                    throw new ApiException("Erreur API ERPNext: " + errorMessage);
                }

                // Convertir la réponse dans le type demandé
                return objectMapper.convertValue(rootNode, responseType);
            } else {
                throw new ApiException("Erreur API HTTP: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'appel API: ", e);
            throw new ApiException("Erreur lors de l'appel API: " + e.getMessage());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Ajouter les en-têtes d'authentification si l'utilisateur est connecté
        if (userSessionDto.isAuthenticated()) {
            headers.set("Authorization", "token " + userSessionDto.getApiKey() + ":" + userSessionDto.getApiSecret());
        }

        return headers;
    }
}*/
