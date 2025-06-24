package com.example.erpnextintegration.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class ErpnextApiConfig {

    @Value("${erpnext.api.url}")
    private String apiUrl;

    @Value("${erpnext.api.version}")
    private String apiVersion;

    public String getBaseApiUrl() {
        return apiUrl + "/api/" + apiVersion;
    }

    public String getLoginUrl() {
        return apiUrl + "/api/method/login";
    }

    public String getLogoutUrl() {
        return apiUrl + "/api/method/logout";
    }
}