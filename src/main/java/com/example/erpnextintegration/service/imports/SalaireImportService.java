package com.example.erpnextintegration.service.imports;

import com.example.erpnextintegration.dto.imports.SalaireData;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalaireImportService {

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

    public ResultatImport importSalairesFromCSV(ResultatImport resultatImport, MultipartFile file) throws IOException {
        if (!file.getContentType().equals("text/csv")) {
            throw new IllegalArgumentException("Seuls les fichiers CSV sont acceptés");
        }

        HeaderColumnNameMappingStrategy<SalaireData> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(SalaireData.class);

        List<SalaireData> validSalaires = new ArrayList<>();
        List<RapportErreur> erreurs = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<SalaireData> csvToBean = new CsvToBeanBuilder<SalaireData>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<SalaireData> salaires = csvToBean.parse();

            int ligne = 1; 
            for (SalaireData salaire : salaires) {
                List<RapportErreur> raison = validateSalaire(salaire, ligne);
                if (raison.isEmpty()) {
                    validSalaires.add(salaire);
                    salaire.setMois(DateUtils.normalizeToStandardFormat(salaire.getMois()).toString());
                    
                } else {
                    erreurs.addAll(raison);
                }
                ligne++;
            }
        }

        resultatImport.setSalaireDatas(validSalaires);
        resultatImport.setErreursSalaire(erreurs);
        return resultatImport;
    }

    private List<RapportErreur> validateSalaire(SalaireData salaire, int ligne) {
        List<RapportErreur> rapportErreurs = new ArrayList<>();
        
        try {
            if (salaire.getMois() == null) {
                rapportErreurs.add(rapportErreurService.createError(ligne, "Mois manquant ou invalide", null));
            }
            if (!DateUtils.isValidDate(salaire.getMois())) {
                rapportErreurs.add(rapportErreurService.createError(ligne, "Date d'anniversaire invalide", salaire.getMois()));
            }
            
        } catch (DateTimeParseException e) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Format de mois invalide", salaire.getMois() != null ? salaire.getMois().toString() : null));
        }
        
        if (salaire.getRefEmploye() == null || salaire.getRefEmploye().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Référence employé manquante", salaire.getRefEmploye()));
        }
        
        // if (salaire.getSalaireBase() == null || salaire.getSalaireBase() <= 0) {
        //     rapportErreurs.add(rapportErreurService.createError(ligne, "Salaire base invalide", 
        //         salaire.getSalaireBase() != null ? salaire.getSalaireBase().toString() : null));
        // }
         if (salaire.getSalaireBase() == null) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Salaire base invalide", 
                salaire.getSalaireBase() != null ? salaire.getSalaireBase().toString() : null));
        }
        
        if (salaire.getSalaryStructure() == null || salaire.getSalaryStructure().isEmpty()) {
            rapportErreurs.add(rapportErreurService.createError(ligne, "Structure salariale manquante", salaire.getSalaryStructure()));
        }
        
        return rapportErreurs;
    }


    public List<SalaireData> transformeEmploye(List<SalaireData> salaireDatas, Map<String, String> refEmp) {
        for (SalaireData salaireData : salaireDatas) {
            String ref = salaireData.getRefEmploye();  
            if (ref != null && refEmp.containsKey(ref)) {
                salaireData.setRefEmploye(refEmp.get(ref));  
            }
        }
        return salaireDatas;
    }


    @SuppressWarnings({ "rawtypes", "unchecked", "null" })
    public Map<String, String> importSalaireData(HttpSession session, List<SalaireData> salairesData) throws Exception {
        String sid = (String) session.getAttribute("sid");
        if (sid == null || sid.isEmpty()) {
            throw new RuntimeException("Session non authentifiée");
        }

        String url = erpnextApiUrl + "/api/method/hrms.evalhr.salary_slip.import_salary_data";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Cookie", "sid=" + sid);
        // headers.set("Authorization", "token " + erpnextApiKey + ":" + erpnextApiSecret);

        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("salaries", salairesData);  

        String jsonPayload;
        try {
            jsonPayload = new ObjectMapper().writeValueAsString(jsonBody);
        } catch (JsonProcessingException e) {
            throw new Exception("Erreur lors de la conversion JSON des salaires", e);
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
                    return Collections.singletonMap("message", "Import des salaires réussi");
                } else {
                    throw new Exception("Échec de l'import des salaires : " + responseBody.get("message"));
                }
            } else {
                throw new Exception("Échec HTTP " + response.getStatusCode() + " lors de l'import des salaires.");
            }
        } catch (Exception e) {
            throw new Exception("Erreur pendant l'import des salaires : " + e.getMessage(), e);
        }
    }


}