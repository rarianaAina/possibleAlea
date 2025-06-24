package com.example.erpnextintegration.service;

import com.example.erpnextintegration.dto.payroll.ComposanteSalarialeDTO;
import com.example.erpnextintegration.dto.payroll.ModifierComposanteDTO;
import com.example.erpnextintegration.dto.StructureSalarialeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StructureSalarialeService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public StructureSalarialeService(
            RestTemplate restTemplate,
            @Value("${erpnext.api.url}") String apiUrl,
            @Value("${erpnext.api.api-key}") String apiKey,
            @Value("${erpnext.api.api-secret}") String apiSecret) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public List<StructureSalarialeDTO> obtenirToutesStructures() {
        HttpHeaders headers = creerEntetes();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Salary Structure?fields=[\"*\"]",
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .map(this::mapStructureSalarialeDTO)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public StructureSalarialeDTO obtenirStructureParId(String id) {
        HttpHeaders headers = creerEntetes();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/api/resource/Salary Structure/" + id,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return mapStructureSalarialeDTO(data);
        }

        return null;
    }

    public void modifierComposante(String structureId, ModifierComposanteDTO composante) {
        // Récupérer la structure actuelle
        StructureSalarialeDTO structure = obtenirStructureParId(structureId);

        if (structure != null) {
            HttpHeaders headers = creerEntetes();

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("earnings", structure.getComposantes().stream()
                    .filter(c -> "GAIN".equals(c.getType()))
                    .map(c -> {
                        Map<String, Object> comp = new HashMap<>();
                        if (c.getId().equals(composante.getId())) {
                            comp.put("salary_component", composante.getNom());
                            comp.put("abbr", composante.getAbreviation());
                            comp.put("formula", composante.getFormule());
                            comp.put("amount_based_on_formula", composante.getFormule() != null ? 1 : 0);
                            comp.put("amount", composante.getMontantFixe());
                        } else {
                            comp.put("salary_component", c.getNom());
                            comp.put("abbr", c.getDescription());
                            comp.put("amount", c.getMontant());
                        }
                        return comp;
                    })
                    .collect(Collectors.toList()));

            requestMap.put("deductions", structure.getComposantes().stream()
                    .filter(c -> "DEDUCTION".equals(c.getType()))
                    .map(c -> {
                        Map<String, Object> comp = new HashMap<>();
                        if (c.getId().equals(composante.getId())) {
                            comp.put("salary_component", composante.getNom());
                            comp.put("abbr", composante.getAbreviation());
                            comp.put("formula", composante.getFormule());
                            comp.put("amount_based_on_formula", composante.getFormule() != null ? 1 : 0);
                            comp.put("amount", composante.getMontantFixe());
                        } else {
                            comp.put("salary_component", c.getNom());
                            comp.put("abbr", c.getDescription());
                            comp.put("amount", c.getMontant());
                        }
                        return comp;
                    })
                    .collect(Collectors.toList()));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

            restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Structure/" + structureId,
                    HttpMethod.PUT,
                    requestEntity,
                    Map.class
            );
        }
    }

    public void ajouterComposante(String structureId, ComposanteSalarialeDTO nouvelleComposante) {
        // D'abord créer la composante salariale si elle n'existe pas
        creerComposanteSalariale(nouvelleComposante);

        // Ensuite l'ajouter à la structure
        StructureSalarialeDTO structure = obtenirStructureParId(structureId);
        System.out.println(structureId);
        System.out.println(structure);

        if (structure != null) {
            HttpHeaders headers = creerEntetes();

            Map<String, Object> requestMap = new HashMap<>();

            List<Map<String, Object>> earnings = structure.getComposantes().stream()
                    .filter(c -> "GAIN".equals(c.getType()))
                    .map(this::mapComposanteToMap)
                    .collect(Collectors.toList());

            List<Map<String, Object>> deductions = structure.getComposantes().stream()
                    .filter(c -> "DEDUCTION".equals(c.getType()))
                    .map(this::mapComposanteToMap)
                    .collect(Collectors.toList());

            // Ajouter la nouvelle composante
            if ("GAIN".equals(nouvelleComposante.getType())) {
                earnings.add(mapComposanteToMap(nouvelleComposante));
            } else {
                deductions.add(mapComposanteToMap(nouvelleComposante));
            }

            requestMap.put("earnings", earnings);
            requestMap.put("deductions", deductions);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

            restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Structure/" + structureId,
                    HttpMethod.PUT,
                    requestEntity,
                    Map.class
            );
        }
    }

/*
    private void creerComposanteSalariale(ComposanteSalarialeDTO composante) {
        System.out.println("Tonga ato ");
        HttpHeaders headers = creerEntetes();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("name", composante.getName());
        requestMap.put("salary_component_name", composante.getName());
        requestMap.put("component_name", composante.getName());
        System.out.println(composante.getName());
        requestMap.put("salary_component_abbr", composante.getDescription());
        requestMap.put("type", composante.getType().equals("GAIN") ? "Earning" : "Deduction");
        requestMap.put("is_tax_applicable", 0);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

        try {
            restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Component",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            System.out.println("OK");
        } catch (Exception e) {
            // La composante existe peut-être déjà
            System.err.println("Erreur lors de la création de la composante: " + e.getMessage());
        }
    }
*/

/*
    private void creerComposanteSalariale(ComposanteSalarialeDTO composante) {
        HttpHeaders headers = creerEntetes();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("doctype", "Salary Component"); // obligatoire
        requestMap.put("component_name", composante.getName()); // champ reconnu
        requestMap.put("abbr", composante.getDescription()); // doit être court (ex: "PT")
        requestMap.put("type", composante.getType().equals("GAIN") ? "Earning" : "Deduction");
        requestMap.put("company", "Ma Société SARL"); // remplace par le vrai nom dans ton ERP
        requestMap.put("is_tax_applicable", 0);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

        try {
            restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Component",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            System.out.println("Composante créée !");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la composante: " + e.getMessage());
        }
    }
*/

    private void creerComposanteSalariale(ComposanteSalarialeDTO composante) {
        HttpHeaders headers = creerEntetes();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("doctype", "Salary Component");
        requestMap.put("name", composante.getName()); // Champ obligatoire
        requestMap.put("salary_component", composante.getName()); // Autre champ important
        requestMap.put("component_name", composante.getName());
        requestMap.put("abbr", composante.getDescription());
        requestMap.put("type", composante.getType().equals("GAIN") ? "Earning" : "Deduction");
        requestMap.put("company", "My Company"); // À remplacer par le nom réel de votre société dans ERPNext
        requestMap.put("is_tax_applicable", 0);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Component",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Composante créée avec succès !");
            } else {
                System.err.println("Erreur lors de la création de la composante: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la composante: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Map<String, Object> mapComposanteToMap(ComposanteSalarialeDTO composante) {
        Map<String, Object> comp = new HashMap<>();
        comp.put("salary_component", composante.getName());
        comp.put("abbr", composante.getDescription());
        comp.put("amount", composante.getMontant());
        return comp;
    }

    private StructureSalarialeDTO mapStructureSalarialeDTO(Map<String, Object> data) {
        StructureSalarialeDTO structure = new StructureSalarialeDTO();

        structure.setName((String) data.get("name"));
        structure.setNom((String) data.get("salary_structure_name"));
        structure.setCompany((String) data.get("company"));
        structure.setCurrency((String) data.get("currency"));
        structure.setPayroll_frequency((String) data.get("mode_of_payment"));
        structure.setStatut((String) data.get("is_active"));

        List<ComposanteSalarialeDTO> composantes = new ArrayList<>();

        // Gains
        if (data.get("earnings") instanceof List) {
            List<Map<String, Object>> earnings = (List<Map<String, Object>>) data.get("earnings");
            for (Map<String, Object> earning : earnings) {
                ComposanteSalarialeDTO composante = new ComposanteSalarialeDTO();
                composante.setId((String) earning.get("name"));
                composante.setNom((String) earning.get("salary_component"));
                composante.setType("GAIN");
                composante.setMontant(new BigDecimal(earning.get("amount").toString()));
                composante.setDescription((String) earning.get("abbr"));
                composantes.add(composante);
            }
        }

        // Déductions
        if (data.get("deductions") instanceof List) {
            List<Map<String, Object>> deductions = (List<Map<String, Object>>) data.get("deductions");
            for (Map<String, Object> deduction : deductions) {
                ComposanteSalarialeDTO composante = new ComposanteSalarialeDTO();
                composante.setId((String) deduction.get("name"));
                composante.setNom((String) deduction.get("salary_component"));
                composante.setType("DEDUCTION");
                composante.setMontant(new BigDecimal(deduction.get("amount").toString()));
                composante.setDescription((String) deduction.get("abbr"));
                composantes.add(composante);
            }
        }

        structure.setComposantes(composantes);

        return structure;
    }

    private HttpHeaders creerEntetes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        return headers;
    }
}