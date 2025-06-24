package com.example.erpnextintegration.service.imports;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;

@Service
public class ResetService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.url}")
    private String erpnextApiUrl;

    @Value("${erpnext.api.key}")
    private String erpnextApiKey;

    @Value("${erpnext.api.secret}")
    private String erpnextApiSecret;

    @SuppressWarnings({ "rawtypes", "null" })
    public Map<String, Object> resetData(HttpSession session) throws Exception {
        String sid = (String) session.getAttribute("sid");
        if (sid == null || sid.isEmpty()) {
            throw new RuntimeException("Session non authentifiée");
        }

        String url = erpnextApiUrl + "/api/method/hrms.evalhr.reset.reset";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Cookie", "sid=" + sid);
        // headers.set("Authorization", "token " + apiKey + ":" + apiSecret); // si utilisé

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            @SuppressWarnings("unused")
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );
            return null;
            // if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            //     Map<String, Object> body = response.getBody();

            //     // if ("success".equals(body.get("status"))) {
            //     //     return body;
            //     // } else {
            //     //     throw new Exception("Échec de la réinitialisation : " + body.get("message"));
            //     // }
            // } else {
            //     throw new Exception("Erreur HTTP " + response.getStatusCode() + " lors de la réinitialisation.");
            // }
        } catch (Exception e) {
            throw new Exception("Erreur pendant la réinitialisation des données de paie : " + e.getMessage(), e);
        }
    }

}
