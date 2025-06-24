package com.example.erpnextintegration.dto.auth;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserSessionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String fullName;
    private String apiKey;
    private String apiSecret;
    private boolean authenticated = false;

    // Méthodes pour la gestion de la session
    public void clearSession() {
        this.username = null;
        this.fullName = null;
        this.apiKey = null;
        this.apiSecret = null;
        this.authenticated = false;
    }

    public void setSessionData(String username, String fullName, String apiKey, String apiSecret) {
        this.username = username;
        this.fullName = fullName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.authenticated = true;
    }
}