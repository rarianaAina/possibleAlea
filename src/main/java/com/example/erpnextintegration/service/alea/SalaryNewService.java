package com.example.erpnextintegration.service.alea;

import com.example.erpnextintegration.dto.alea.SalarySlipResponse;
import com.example.erpnextintegration.dto.payroll.ComposanteSalarialeDTO;
import com.example.erpnextintegration.dto.payroll.SalaireDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class SalaryNewService {
    private static final Logger logger = LoggerFactory.getLogger(SalaryNewService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.url}")
    private String apiUrl;

    @Value("${erpnext.api.api-key}")
    private String apiKey;

    @Value("${erpnext.api.api-secret}")
    private String apiSecret;

    public List<SalaireDTO> getSalarySlipsWithComponent(String componentName, BigDecimal amount, String condition, Double pourcentage, String plusOuMoins) {
        HttpHeaders headers = createHeaders();
        String listUrl = apiUrl + "/api/resource/Salary Slip?filters=[[\"docstatus\",\"=\",\"1\"]]&limit_page_length=1000";
        ResponseEntity<Map> listResponse = restTemplate.exchange(
                listUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        List<Map<String, Object>> slips = (List<Map<String, Object>>) listResponse.getBody().get("data");
        List<SalaireDTO> result = new ArrayList<>();
        List<String> slipsToCancel = new ArrayList<>();
        List<Map<String, Object>> slipsToCreate = new ArrayList<>();

        // Première passe: collecter les bulletins à traiter
        for (Map<String, Object> slipEntry : slips) {
            String name = (String) slipEntry.get("name");
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .pathSegment("api", "resource", "Salary Slip", name)
                    .encode()
                    .toUriString();

            try {
                ResponseEntity<Map> detailResponse = restTemplate.exchange(
                        URI.create(url),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

                Map<String, Object> slip = (Map<String, Object>) detailResponse.getBody().get("data");
                List<ComposanteSalarialeDTO> composants = new ArrayList<>();

                extractComponents((List<Map<String, Object>>) slip.get("earnings"), "GAIN", componentName, amount, condition, composants);
                extractComponents((List<Map<String, Object>>) slip.get("deductions"), "DEDUCTION", componentName, amount, condition, composants);

                if (!composants.isEmpty()) {
                    SalaireDTO dto = new SalaireDTO();
                    dto.setId((String) slip.get("name"));
                    dto.setEmployeId((String) slip.get("employee"));
                    dto.setNomEmploye((String) slip.get("employee_name"));
                    dto.setMois(LocalDate.parse((String) slip.get("start_date")).getMonthValue());
                    dto.setDatePaiement(LocalDate.parse((String) slip.get("posting_date")));
                    dto.setSalaireBrut(toBigDecimal(slip.get("gross_pay")));
                    dto.setSalaireNet(toBigDecimal(slip.get("net_pay")));
                    dto.setComposantes(composants);

                    result.add(dto);
                    slipsToCancel.add(name);
                    slipsToCreate.add(slip);
                }

            } catch (Exception ex) {
                logger.error("Erreur lors de la récupération du slip '{}' : {}", name, ex.getMessage());
            }
        }

        // Deuxième passe: annuler les anciens bulletins
        if (!slipsToCancel.isEmpty()) {
            boolean cancellationSuccess = cancelMultipleSalarySlips(slipsToCancel);

            if (cancellationSuccess) {
                try {
                    // Petite pause pour s'assurer que l'annulation est bien prise en compte
                    Thread.sleep(2000);
                    deleteMultipleSalarySlips(slipsToCancel);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Troisième passe: créer les nouveaux bulletins
                for (Map<String, Object> slip : slipsToCreate) {
                    try {
                        createNewSalarySlipWithIncrease(slip, pourcentage, plusOuMoins);
                    } catch (Exception e) {
                        logger.error("Erreur lors de la création du nouveau bulletin pour l'employé {}",
                                slip.get("employee"), e);
                    }
                }
            } else {
                logger.error("Certains bulletins n'ont pas pu être annulés, création des nouveaux bulletins annulée");
            }
        }

        return result;
    }

    private void extractComponents(List<Map<String, Object>> list, String type, String target, BigDecimal amount,
                                   String condition, List<ComposanteSalarialeDTO> composants) {
        if (list == null) return;

        for (Map<String, Object> c : list) {
            String componentName = (String) c.get("salary_component");
            BigDecimal componentAmount = toBigDecimal(c.get("amount"));

            boolean nameMatches = target.equalsIgnoreCase(componentName);
            boolean amountMatches = false;

            // Appliquer la condition appropriée
            if (amount != null) {
                switch (condition.toLowerCase()) {
                    case "inferieur":
                        amountMatches = componentAmount.compareTo(amount) < 0;
                        break;
                    case "superieur":
                        amountMatches = componentAmount.compareTo(amount) > 0;
                        break;
                    case "egal":
                        amountMatches = componentAmount.compareTo(amount) == 0;
                        break;
                    default:
                        logger.warn("Condition non reconnue: {}", condition);
                }
            } else {
                amountMatches = true; // Si aucun montant n'est spécifié, on prend tous les composants
            }

            if (nameMatches && amountMatches) {
                composants.add(new ComposanteSalarialeDTO(
                        null,
                        componentName,
                        type,
                        componentAmount,
                        (String) c.get("description"),
                        (String) c.get("name")
                ));
            }
        }
    }

/*
    private void createNewSalarySlipWithIncrease(Map<String, Object> originalSlip, Double pourcentage, String plusOuMoins) {
        try {
            HttpHeaders headers = createHeaders();

            Map<String, Object> requestData = new HashMap<>();
            Map<String, Object> salaryData = new HashMap<>();

            String employeeId = (String) originalSlip.get("employee");
            String salaryStructure = "g1";
            String startDateStr = (String) originalSlip.get("start_date");
            LocalDate startDate = LocalDate.parse(startDateStr);
            Integer mois = startDate.getMonthValue();

            if (mois == null) {
                throw new IllegalArgumentException("Le mois du bulletin original est obligatoire");
            }


            BigDecimal baseSalary = findBaseSalary((List<Map<String, Object>>) originalSlip.get("earnings"));

            BigDecimal userPercentage = BigDecimal.valueOf(pourcentage);
            BigDecimal multiplier = BigDecimal.valueOf(100).add(userPercentage).divide(BigDecimal.valueOf(100));
            BigDecimal newBaseSalary = baseSalary.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);


            salaryData.put("ref_employe", employeeId);
            salaryData.put("salaire_base", newBaseSalary);
            salaryData.put("salary_structure", salaryStructure);
            salaryData.put("mois", startDate.toString());

            requestData.put("salaries", Collections.singletonList(salaryData));

            String url = apiUrl + "/api/method/hrms.evalhr.salary_slip_alea.import_salary_data_alea";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestData, headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                logger.info("Nouveau Salary Slip créé avec succès pour l'employé {}: {}", employeeId, responseBody);
            } else {
                logger.error("Erreur lors de la création pour l'employé {}. Statut: {}", employeeId, response.getStatusCode());
                throw new RuntimeException("Échec de la création du nouveau bulletin");
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la création du nouveau bulletin", e);
            throw new RuntimeException("Échec de la création du nouveau bulletin", e);
        }
    }
*/

    private void createNewSalarySlipWithIncrease(Map<String, Object> originalSlip, Double pourcentage, String plusOuMoins) {
        try {
            HttpHeaders headers = createHeaders();

            Map<String, Object> requestData = new HashMap<>();
            Map<String, Object> salaryData = new HashMap<>();

            String employeeId = (String) originalSlip.get("employee");
            String salaryStructure = "g1";
            String startDateStr = (String) originalSlip.get("start_date");
            LocalDate startDate = LocalDate.parse(startDateStr);
            Integer mois = startDate.getMonthValue();

            if (mois == null) {
                throw new IllegalArgumentException("Le mois du bulletin original est obligatoire");
            }

            BigDecimal baseSalary = findBaseSalary((List<Map<String, Object>>) originalSlip.get("earnings"));

            BigDecimal userPercentage = BigDecimal.valueOf(pourcentage);
            BigDecimal multiplier;

            if ("plus".equalsIgnoreCase(plusOuMoins)) {
                multiplier = BigDecimal.valueOf(100).add(userPercentage).divide(BigDecimal.valueOf(100));
            } else if ("moins".equalsIgnoreCase(plusOuMoins)) {
                multiplier = BigDecimal.valueOf(100).subtract(userPercentage).divide(BigDecimal.valueOf(100));
            } else {
                throw new IllegalArgumentException("La valeur de 'plusOuMoins' doit être 'plus' ou 'moins'");
            }

            BigDecimal newBaseSalary = baseSalary.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

            salaryData.put("ref_employe", employeeId);
            salaryData.put("salaire_base", newBaseSalary);
            salaryData.put("salary_structure", salaryStructure);
            salaryData.put("mois", startDate.toString());

            requestData.put("salaries", Collections.singletonList(salaryData));

            String url = apiUrl + "/api/method/hrms.evalhr.salary_slip_alea.import_salary_data_alea";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestData, headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                logger.info("Nouveau Salary Slip créé avec succès pour l'employé {}: {}", employeeId, responseBody);
            } else {
                logger.error("Erreur lors de la création pour l'employé {}. Statut: {}", employeeId, response.getStatusCode());
                throw new RuntimeException("Échec de la création du nouveau bulletin");
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la création du nouveau bulletin", e);
            throw new RuntimeException("Échec de la création du nouveau bulletin", e);
        }
    }

    public void cancelSalarySlip(String slipName) {
        HttpHeaders headers = createHeaders();
        Map<String, Object> body = new HashMap<>();
        body.put("docstatus", 2);

        String url = apiUrl + "/api/resource/Salary Slip/" + slipName;

        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                String.class);
    }


    public boolean cancelMultipleSalarySlips(List<String> slipNames) {
        HttpHeaders headers = createHeaders();
        Map<String, Object> body = new HashMap<>();
        body.put("docstatus", 2);
        boolean allCancelled = true;

        for (String slipName : slipNames) {
            try {
                String url = apiUrl + "/api/resource/Salary Slip/" + slipName;
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.PUT,
                        new HttpEntity<>(body, headers),
                        String.class);

                if (response.getStatusCode() == HttpStatus.OK) {

                    logger.info("Bulletin annulé avec succès: {}", slipName);
                } else {
                    logger.error("Échec de l'annulation du bulletin {}: {}", slipName, response.getStatusCode());
                    allCancelled = false;
                }
            } catch (Exception e) {
                logger.error("Erreur lors de l'annulation du bulletin {}: {}", slipName, e.getMessage());
                allCancelled = false;
            }
        }

        return allCancelled;
    }


    public boolean deleteMultipleSalarySlips(List<String> slipNames) {
        HttpHeaders headers = createHeaders(); // Authentification API
        boolean allDeleted = true;

        for (String slipName : slipNames) {
            try {
                String url = apiUrl + "/api/resource/Salary Slip/" + slipName;
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        String.class);

                if (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getStatusCode() == HttpStatus.OK) {
                    logger.info("Bulletin supprimé avec succès: {}", slipName);
                } else {
                    logger.error("Échec de la suppression du bulletin {}: {}", slipName, response.getStatusCode());
                    allDeleted = false;
                }
            } catch (Exception e) {
                logger.error("Erreur lors de la suppression du bulletin {}: {}", slipName, e.getMessage());
                allDeleted = false;
            }
        }

        return allDeleted;
    }

    private BigDecimal findBaseSalary(List<Map<String, Object>> earnings) {
        for (Map<String, Object> earning : earnings) {
            if ("Salaire Base".equalsIgnoreCase((String) earning.get("salary_component"))) {
                return toBigDecimal(earning.get("amount"));
            }
        }
        throw new RuntimeException("Composant 'Salaire de base' introuvable dans les gains");
    }


    private BigDecimal toBigDecimal(Object o) {
        return o != null ? new BigDecimal(o.toString()) : BigDecimal.ZERO;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        return headers;
    }

}