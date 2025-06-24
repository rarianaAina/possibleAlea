package com.example.erpnextintegration.service.imports;

import com.example.erpnextintegration.dto.imports.EmployeData;
import com.example.erpnextintegration.dto.imports.RapportErreur;
import com.example.erpnextintegration.dto.imports.ResultatImport;
import com.example.erpnextintegration.utils.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeImportService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.url}")
    private String erpnextApiUrl;

    @Value("${erpnext.api.key}")
    private String erpnextApiKey;

    @Value("${erpnext.api.secret}")
    private String erpnextApiSecret;

    @Autowired
    private RapportErreurService rapportErreurService;

    public ResultatImport   importEmployesFromCSV(ResultatImport resultatImport, MultipartFile file) throws IOException {
        if (!file.getContentType().equals("text/csv")) {
            throw new IllegalArgumentException("Seuls les fichiers CSV sont acceptés");
        }

        HeaderColumnNameMappingStrategy<EmployeData> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(EmployeData.class);

        List<EmployeData> validEmployes = new ArrayList<>();
        List<RapportErreur> erreurs = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<EmployeData> csvToBean = new CsvToBeanBuilder<EmployeData>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<EmployeData> employes = csvToBean.parse();

            int ligne = 1; // ligne 1 = en-tête
            for (EmployeData employe : employes) {
                List<RapportErreur> raison = validateEmploye(employe, ligne);
                if (raison.isEmpty()) {
                    validEmployes.add(employe);
                    employe.setDateEmbauche(DateUtils.normalizeToStandardFormat(employe.getDateEmbauche()).toString());
                    employe.setDateNaissance(DateUtils.normalizeToStandardFormat(employe.getDateNaissance()).toString());
                } else {
                    erreurs.addAll(raison);
                }
                ligne++;
            }
        }

        resultatImport.setEmployesValides(validEmployes);
        resultatImport.setErreursEmploye(erreurs);
        return resultatImport;
    }

    private List<RapportErreur> validateEmploye(EmployeData employe, int ligne) {
        List<RapportErreur> rapportErreurs = new ArrayList<>();
        
        if (employe.getRef() == null || employe.getRef().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Ref manquant", employe.getRef()));
        }
        if (employe.getNom() == null || employe.getNom().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Nom manquant", employe.getNom()));
        }
        if (employe.getPrenom() == null || employe.getPrenom().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Prenom manquant", employe.getPrenom()));
        }
        if (employe.getDateNaissance() == null || !DateUtils.isValidDate(employe.getDateNaissance())) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Date d'anniversaire invalide", employe.getDateNaissance()));
        }
        if (employe.getDateEmbauche() == null || !DateUtils.isValidDate(employe.getDateEmbauche())) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Date d'embauche invalide", employe.getDateEmbauche()));
        }
        
        return rapportErreurs; 
    }



    @SuppressWarnings({ "rawtypes", "unchecked", "null" })
    public Map<String, String> createEmployees(HttpSession session, List<EmployeData> employeesData) throws Exception {
        String sid = (String) session.getAttribute("sid");
        if (sid == null || sid.isEmpty()) {
            throw new RuntimeException("Session not authenticated");
        }

        String url = erpnextApiUrl + "/api/method/hrms.evalhr.employee.import_employe";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Cookie", "sid=" + sid);

        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("employees", employeesData);

        String jsonPayload;
        try {
            jsonPayload = new ObjectMapper().writeValueAsString(jsonBody);
        } catch (JsonProcessingException e) {
            throw new Exception("Error converting employees data to JSON", e);
        }

        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> rawBody = response.getBody();
                Map<String, Object> responseBody = (Map<String, Object>) rawBody.get("message");

                if (responseBody != null && "success".equals(responseBody.get("status"))) {
                    return (Map<String, String>) responseBody.get("ref_mapping");
                } else {
                    throw new Exception("Failed to create employees: " + responseBody.get("message"));
                }
            } else {
                throw new Exception("Failed to create employees: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new Exception("Error while creating employees: " + e.getMessage(), e);
        }
    }


}