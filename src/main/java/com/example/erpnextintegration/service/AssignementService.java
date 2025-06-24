package com.example.erpnextintegration.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssignementService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.url}")
    private String apiUrl;

    @Value("${erpnext.api.api-key}")
    private String apiKey;

    @Value("${erpnext.api.api-secret}")
    private String apiSecret;

    public void attribuerStructureSalariale(String structureName, String employeeId, String company,
                                             BigDecimal baseSalary, String fromDate, String toDate) {
        HttpHeaders headers = creerEntetes();

        try {
            Map<String, Object> assignmentData = new HashMap<>();
            assignmentData.put("doctype", "Salary Structure Assignment");
            assignmentData.put("employee", employeeId);
            assignmentData.put("salary_structure", structureName);
            assignmentData.put("company", company);
            assignmentData.put("from_date", fromDate);
            if (toDate != null) {
                assignmentData.put("to_date", toDate);
            }
            assignmentData.put("base", baseSalary);
            assignmentData.put("docstatus", 1);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(assignmentData, headers);

            restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Structure Assignment",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            System.out.println("Structure " + structureName + " attribuée à l'employé " + employeeId +
                    " du " + fromDate + " au " + (toDate != null ? toDate : "indéfini"));
        } catch (Exception e) {
            System.err.println("Erreur lors de l'attribution de la structure: " + e.getMessage());
        }
    }

    public Map<String, String> assignStructure(HttpSession session,
                                               List<String> employeeIds,
                                               LocalDate fromDate,
                                               Double baseSalary) throws Exception {

        String structureName = "g1";
        String company = "Orinasa SA";

        String sid = (String) session.getAttribute("sid");
        if (sid == null || sid.isEmpty()) {
            throw new RuntimeException("Session non authentifiée");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Cookie", "sid=" + sid);

        Map<String, String> results = new HashMap<>();

        for (String employeeId : employeeIds) {
            String url = apiUrl + "/api/method/hrms.evalhr.structure_assignment.assign_structure_to_employee";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("employee", employeeId);
            requestBody.put("salary_structure", structureName);
            requestBody.put("start_date", fromDate.toString());
            requestBody.put("company", company);
            requestBody.put("salaire_base", baseSalary);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    results.put(employeeId, "Assignation réussie");
                } else {
                    results.put(employeeId, "Échec - Code HTTP: " + response.getStatusCode());
                }
            } catch (Exception e) {
                results.put(employeeId, "Erreur: " + e.getMessage());
            }
        }

        return results;
    }
    private HttpHeaders creerEntetes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        return headers;
    }
}
