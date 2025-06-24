package com.example.erpnextintegration.service.imports;

import com.example.erpnextintegration.dto.imports.GrilleSalaireData;
import com.example.erpnextintegration.dto.imports.RapportErreur;
import com.example.erpnextintegration.dto.imports.ResultatImport;
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
public class GrilleImportService {

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

    public ResultatImport importGrilleSalaireFromCSV(ResultatImport resultatImport, MultipartFile file) throws IOException {
        if (!file.getContentType().equals("text/csv")) {
            throw new IllegalArgumentException("Seuls les fichiers CSV sont acceptés");
        }

        HeaderColumnNameMappingStrategy<GrilleSalaireData> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(GrilleSalaireData.class);

        List<GrilleSalaireData> validGrilles = new ArrayList<>();
        List<RapportErreur> erreurs = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<GrilleSalaireData> csvToBean = new CsvToBeanBuilder<GrilleSalaireData>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<GrilleSalaireData> grilles = csvToBean.parse();

            int ligne = 1; // ligne 1 = en-tête
            for (GrilleSalaireData grille : grilles) {
                List<RapportErreur> raison = validateGrilleSalaire(grille, ligne);
                if (raison.isEmpty()) {
                    validGrilles.add(grille);
                } else {
                    erreurs.addAll(raison);
                }
                ligne++;
            }
        }

        resultatImport.setGrilleSalaireDatas(validGrilles);
        resultatImport.setErreursGrille(erreurs);
        return resultatImport;
    }

    private List<RapportErreur> validateGrilleSalaire(GrilleSalaireData grille, int ligne) {
        List<RapportErreur> rapportErreurs = new ArrayList<>();
        
        if (grille.getSalaryStructure() == null || grille.getSalaryStructure().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Salary structure manquant", grille.getSalaryStructure()));
        }
        
        if (grille.getName() == null || grille.getName().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Name manquant", grille.getName()));
        }
        
        if (grille.getAbbr() == null || grille.getAbbr().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Abbr manquant", grille.getAbbr()));
        }
        
        if (grille.getType() == null || grille.getType().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Type manquant", grille.getType()));
        }
        
        if (grille.getValeur() == null || grille.getValeur().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Valeur manquante", grille.getValeur()));
        }
        
        if (grille.getCompany() == null || grille.getCompany().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Company manquante", grille.getCompany()));
        }
        
        return rapportErreurs;
    }
    
    

    @SuppressWarnings({ "rawtypes", "unchecked", "null" })
    public Map<String, String> importGrilleSalaire(HttpSession session, List<GrilleSalaireData> grillesData) throws Exception {
        String sid = (String) session.getAttribute("sid");
        if (sid == null || sid.isEmpty()) {
            throw new RuntimeException("Session not authenticated");
        }

        String url = erpnextApiUrl + "/api/method/hrms.evalhr.structure_salariale.import_grille_salaire";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Cookie", "sid=" + sid);

        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("grilles", grillesData);

        String jsonPayload;
        try {
            jsonPayload = new ObjectMapper().writeValueAsString(jsonBody);
        } catch (JsonProcessingException e) {
            throw new Exception("Error converting grilles data to JSON", e);
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
                    
                    return Collections.singletonMap("message", "Import réussi");
                } else {
                    throw new Exception("Failed to import grille salaire: " + responseBody.get("message"));
                }
            } else {
                throw new Exception("Failed to import grille salaire: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new Exception("Error while importing grille salaire: " + e.getMessage(), e);
        }
    }

}