package com.example.erpnextintegration.service;

import com.example.erpnextintegration.dto.importation.ImportEmployeeDTO;
import com.example.erpnextintegration.dto.importation.ImportStructureSalarialeDTO;
import com.example.erpnextintegration.dto.importation.ImportSalaireDTO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ImportationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.url}")
    private String apiUrl;

    @Value("${erpnext.api.api-key}")
    private String apiKey;

    @Value("${erpnext.api.api-secret}")
    private String apiSecret;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MOIS_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

    public void importerDonnees(
            MultipartFile employesFile,
            MultipartFile structuresFile,
            MultipartFile salairesFile) throws IOException {

        List<ImportEmployeeDTO> employes = importerEmployes(employesFile);
        List<ImportStructureSalarialeDTO> structures = importerStructuresSalariales(structuresFile);
        List<ImportSalaireDTO> salaires = importerSalaires(salairesFile);

        traiterDonnees(employes, structures, salaires);
    }

    private List<ImportEmployeeDTO> importerEmployes(MultipartFile file) throws IOException {
        List<ImportEmployeeDTO> employes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withDelimiter(',')
                    .withTrim()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                ImportEmployeeDTO employe = new ImportEmployeeDTO();
                employe.setReference(record.get("Ref").trim());
                employe.setNom(record.get("Nom").trim());
                employe.setPrenom(record.get("Prenom").trim());
                employe.setGenre(record.get("genre").trim());

                try {
                    employe.setDateEmbauche(LocalDate.parse(record.get("Date embauche").trim(), DATE_FORMATTER));
                    employe.setDateNaissance(LocalDate.parse(record.get("date naissance").trim(), DATE_FORMATTER));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Format de date invalide pour l'employé " + employe.getNom() +
                            ". Utilisez le format dd/MM/yyyy");
                }

                employe.setEntreprise(record.get("company").trim());
                employes.add(employe);
            }
        }

        return employes;
    }

    private List<ImportStructureSalarialeDTO> importerStructuresSalariales(MultipartFile file) throws IOException {
        List<ImportStructureSalarialeDTO> structures = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withDelimiter(',')
                    .withTrim()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                ImportStructureSalarialeDTO structure = new ImportStructureSalarialeDTO();
                structure.setStructureSalariale(record.get("salary structure").trim());
                structure.setNom(record.get("name").trim());
                structure.setAbreviation(record.get("Abbr").trim());
                structure.setType(record.get("type").trim());
                structure.setValeur(record.get("valeur").trim());
                structures.add(structure);
            }
        }

        return structures;
    }

    private List<ImportSalaireDTO> importerSalaires(MultipartFile file) throws IOException {
        List<ImportSalaireDTO> salaires = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withDelimiter(',')
                    .withTrim()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                ImportSalaireDTO salaire = new ImportSalaireDTO();

                try {
                    String dateStr = record.get("Mois").trim();
                    LocalDate date = LocalDate.parse(dateStr, inputFormatter);
                    salaire.setMois(String.format("%02d/%04d", date.getMonthValue(), date.getYear()));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Format de date invalide pour le champ 'Mois'. Attendu : dd/MM/yyyy");
                }

                salaire.setReferenceEmploye(record.get("Ref Employe").trim());

                try {
                    salaire.setSalaireBase(new BigDecimal(record.get("Salaire Base").trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Montant de salaire base invalide pour l'employé " +
                            salaire.getReferenceEmploye());
                }

                salaire.setStructureSalariale(record.get("Salaire").trim());
                salaires.add(salaire);
            }
        }

        return salaires;
    }

    private void traiterDonnees(
            List<ImportEmployeeDTO> employes,
            List<ImportStructureSalarialeDTO> structures,
            List<ImportSalaireDTO> salaires) {

        String company = employes.isEmpty() ? "My Company" : employes.get(0).getEntreprise();

        // Créer l'entreprise
        creerEntrepriseSiInexistante(company);

        // Créer/Mettre à jour les employés
        Map<String, String> refToEmployeId = new HashMap<>();
        for (ImportEmployeeDTO employe : employes) {
            String employeId = verifierOuCreerEmploye(employe);
            if (employeId != null) {
                refToEmployeId.put(employe.getReference(), employeId);
            }
        }

        // Créer les composantes salariales
        for (ImportStructureSalarialeDTO structure : structures) {
            if (!verifierComposanteExisteERPNext(structure.getNom())) {
                creerComposanteERPNext(
                        structure.getNom(),
                        structure.getStructureSalariale(),
                        structure.getAbreviation(),
                        structure.getType()
                );
            }
        }

        // Regrouper les structures par nom
        Map<String, List<ImportStructureSalarialeDTO>> structuresParNom = new HashMap<>();
        structures.forEach(structure -> {
            structuresParNom.computeIfAbsent(structure.getStructureSalariale(), k -> new ArrayList<>())
                    .add(structure);
        });

        // Créer les structures salariales
        structuresParNom.forEach((nom, comps) -> {
            creerStructureSalariale(nom, comps, company);
            // Attendre que la structure soit créée
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Grouper les salaires par employé et par mois
        Map<String, Map<String, ImportSalaireDTO>> salairesParEmployeEtMois = new HashMap<>();
        for (ImportSalaireDTO salaire : salaires) {
            salairesParEmployeEtMois
                    .computeIfAbsent(salaire.getReferenceEmploye(), k -> new HashMap<>())
                    .put(salaire.getMois(), salaire);
        }

        // Attribuer les structures aux employés avec les bons salaires de base par période
        for (Map.Entry<String, String> entry : refToEmployeId.entrySet()) {
            String employeId = entry.getValue();
            String refEmploye = entry.getKey();

            if (salairesParEmployeEtMois.containsKey(refEmploye)) {
                Map<String, ImportSalaireDTO> salairesParMois = salairesParEmployeEtMois.get(refEmploye);

                // Trier les mois par ordre chronologique
                List<String> moisTries = new ArrayList<>(salairesParMois.keySet());
                moisTries.sort(Comparator.naturalOrder());

                // Attribuer les structures pour chaque période
                for (int i = 0; i < moisTries.size(); i++) {
                    String mois = moisTries.get(i);
                    ImportSalaireDTO salaire = salairesParMois.get(mois);

                    String fromDate = getFirstDayOfMonth(mois);
                    String toDate = (i < moisTries.size() - 1)
                            ? getLastDayOfPreviousMonth(moisTries.get(i + 1))
                            : null;

                    attribuerStructureSalariale(
                            salaire.getStructureSalariale(),
                            employeId,
                            company,
                            salaire.getSalaireBase(),
                            fromDate,
                            toDate
                    );
                }
            }
        }

        // Attendre que les attributions soient terminées
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Créer les fiches de paie
        for (ImportSalaireDTO salaire : salaires) {
            String employeId = refToEmployeId.get(salaire.getReferenceEmploye());
            if (employeId != null) {
                creerFichePaie(salaire, employeId, structures);
            }
        }
    }

    private String getFirstDayOfMonth(String moisFormate) {
        String[] parts = moisFormate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        return LocalDate.of(year, month, 1).toString();
    }

    private String getLastDayOfPreviousMonth(String moisFormate) {
        String[] parts = moisFormate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        return LocalDate.of(year, month, 1).minusDays(1).toString();
    }

    private void creerEntrepriseSiInexistante(String nomEntreprise) {
        HttpHeaders headers = creerEntetes();

        try {
            String filters = String.format("[[\"company_name\",\"=\",\"%s\"]]", nomEntreprise);
            String url = String.format("%s/api/resource/Company?filters=%s",
                    apiUrl, URLEncoder.encode(filters, StandardCharsets.UTF_8));

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getBody() != null && ((List) response.getBody().get("data")).isEmpty()) {
                Map<String, Object> company = new HashMap<>();
                company.put("doctype", "Company");
                company.put("company_name", nomEntreprise);
                company.put("default_currency", "EUR");
                company.put("country", "France");

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(company, headers);
                restTemplate.exchange(
                        apiUrl + "/api/resource/Company",
                        HttpMethod.POST,
                        requestEntity,
                        Map.class
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'entreprise: " + e.getMessage());
        }
    }

    private String verifierOuCreerEmploye(ImportEmployeeDTO employe) {
        HttpHeaders headers = creerEntetes();

        try {
            String filters = String.format("[[\"employee_name\",\"=\",\"%s %s\"],[\"company\",\"=\",\"%s\"]]",
                    employe.getPrenom(), employe.getNom(), employe.getEntreprise());
            String url = String.format("%s/api/resource/Employee?filters=%s",
                    apiUrl, URLEncoder.encode(filters, StandardCharsets.UTF_8));

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getBody() != null && !((List)response.getBody().get("data")).isEmpty()) {
                Map<String, Object> employeeData = (Map<String, Object>)((List)response.getBody().get("data")).get(0);
                return (String)employeeData.get("name");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'employé: " + e.getMessage());
        }

        return creerNouvelEmploye(employe, headers);
    }

    private String creerNouvelEmploye(ImportEmployeeDTO employe, HttpHeaders headers) {
        Map<String, Object> employeData = new HashMap<>();
        employeData.put("doctype", "Employee");
        employeData.put("first_name", employe.getPrenom());
        employeData.put("last_name", employe.getNom());
        employeData.put("gender", normaliserGenre(employe.getGenre()));
        employeData.put("date_of_birth", employe.getDateNaissance().toString());
        employeData.put("date_of_joining", employe.getDateEmbauche().toString());
        employeData.put("company", employe.getEntreprise());
        employeData.put("status", "Active");

        try {
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(employeData, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/api/resource/Employee",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                String employeeId = (String) data.get("name");
                System.out.println("Nouvel employé créé: " + employeeId);
                return employeeId;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'employé: " + e.getMessage());
        }
        return null;
    }

    private boolean verifierComposanteExisteERPNext(String nomComposante) {
        HttpHeaders headers = creerEntetes();
        String url = apiUrl + "/api/resource/Salary Component/" + URLEncoder.encode(nomComposante, StandardCharsets.UTF_8);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private void creerComposanteERPNext(String nom, String structure, String abreviation, String typeCSV) {
        HttpHeaders headers = creerEntetes();
        String typeERPNext = "earning".equalsIgnoreCase(typeCSV) ? "Earning" : "Deduction";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Salary Component");
        requestBody.put("salary_component", nom);
        requestBody.put("salary_component_abbr", abreviation);
        requestBody.put("type", typeERPNext);
        requestBody.put("description", structure);
        requestBody.put("depends_on_payment_days", 0);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Component",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
        } catch (Exception e) {
            System.err.println("Erreur création composante '" + nom + "': " + e.getMessage());
        }
    }

    private void creerStructureSalariale(String nom, List<ImportStructureSalarialeDTO> composantes, String company) {
        HttpHeaders headers = creerEntetes();

        try {
            String checkUrl = apiUrl + "/api/resource/Salary Structure/" + URLEncoder.encode(nom, StandardCharsets.UTF_8);
            try {
                ResponseEntity<Map> checkResponse = restTemplate.exchange(
                        checkUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );
                if (checkResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("La structure salariale " + nom + " existe déjà");
                    return;
                }
            } catch (HttpClientErrorException.NotFound e) {
                System.out.println("Création de la structure salariale " + nom);
            }

            Map<String, Object> structureData = new HashMap<>();
            structureData.put("doctype", "Salary Structure");
            structureData.put("name", nom);
            structureData.put("company", company);
            structureData.put("is_active", "Yes");
            structureData.put("payroll_frequency", "Monthly");
            structureData.put("docstatus", 1);

            List<Map<String, Object>> earnings = new ArrayList<>();
            List<Map<String, Object>> deductions = new ArrayList<>();

            for (ImportStructureSalarialeDTO composante : composantes) {
                Map<String, Object> comp = new HashMap<>();
                comp.put("salary_component", composante.getNom());
                comp.put("abbr", composante.getAbreviation());
                comp.put("amount_based_on_formula", 1);
                comp.put("formula", composante.getValeur());
                comp.put("depends_on_payment_days", 0);

                if ("Earning".equalsIgnoreCase(composante.getType())) {
                    earnings.add(comp);
                } else {
                    deductions.add(comp);
                }
            }

            structureData.put("earnings", earnings);
            structureData.put("deductions", deductions);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(structureData, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Structure",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Structure " + nom + " créée avec succès");
            }
        } catch (Exception e) {
            System.err.println("Erreur création structure '" + nom + "': " + e.getMessage());
        }
    }

    private void attribuerStructureSalariale(String structureName, String employeeId, String company,
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

    private void creerFichePaie(ImportSalaireDTO salaire, String employeId, List<ImportStructureSalarialeDTO> structures) {
        HttpHeaders headers = creerEntetes();

        try {
            String[] moisAnnee = salaire.getMois().split("/");
            LocalDate dateDebut = LocalDate.of(
                    Integer.parseInt(moisAnnee[1]),
                    Integer.parseInt(moisAnnee[0]),
                    1
            );
            LocalDate dateFin = dateDebut.plusMonths(1).minusDays(1);

            Map<String, Object> salaireData = new HashMap<>();
            salaireData.put("doctype", "Salary Slip");
            salaireData.put("employee", employeId);
            salaireData.put("salary_structure", salaire.getStructureSalariale());
            salaireData.put("start_date", dateDebut.toString());
            salaireData.put("end_date", dateFin.toString());
            salaireData.put("docstatus", 1);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(salaireData, headers);
            restTemplate.exchange(
                    apiUrl + "/api/resource/Salary Slip",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            System.out.println("Fiche de paie créée pour l'employé " + employeId + " (" + salaire.getMois() + ")");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la fiche de paie: " + e.getMessage());
        }
    }

    private String normaliserGenre(String genre) {
        if (genre == null) return "Other";
        return switch (genre.toLowerCase()) {
            case "m", "male", "homme", "h", "masculin" -> "Male";
            case "f", "female", "femme", "fem", "feminin" -> "Female";
            default -> "Other";
        };
    }

    private HttpHeaders creerEntetes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        return headers;
    }
}