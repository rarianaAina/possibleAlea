package com.example.erpnextintegration.dto;

import lombok.Data;

/**
 * DTO pour la réponse de connexion.
 */
@Data
public class LoginResponse {
    private String message;
    private String apiKey;
    private String apiSecret;
    private String userId;
    private String userName;
    private String fullName;
    private String sid;
}