/*
package com.example.erpnextintegration.service.alea;


import com.example.erpnextintegration.service.ErpnextApiService;
import com.example.erpnextintegration.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.erpnextintegration.dto.payroll.SalaryAdjustmentDTO;
import com.example.erpnextintegration.dto.payroll.SalaryAdjustmentResultDTO;
import com.example.erpnextintegration.dto.payroll.SalarySlipDTO;
import com.example.erpnextintegration.dto.payroll.SalaryDetailDTO;
import com.example.erpnextintegration.dto.employee.EmployeeDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryAdjustementService {

    @Autowired
    private ErpnextApiService erpnextApiService;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private HRService hrService;

*/
/*    public SalaryAdjustmentResultDTO traiterAjustementSalarial(SalaryAdjustmentDTO ajustement,
                                                               String apiKey, String apiSecret) {
        SalaryAdjustmentResultDTO resultat = new SalaryAdjustmentResultDTO();
        List<String> employesTraites = new ArrayList<>();

        try {
            // 1. Récupérer tous les bulletins de paie dans la période
            List<SalarySlipDTO> bulletins = getBulletinsPeriode(ajustement.getStartDate(),
                    ajustement.getEndDate(), apiKey, apiSecret);

            // 2. Filtrer les bulletins selon les critères
            List<SalarySlipDTO> bulletinsFiltres = filtrerBulletins(bulletins, ajustement);

            int bulletinsAnnules = 0;
            int nouveauxBulletins = 0;

            // 3. Traiter chaque bulletin filtré
            for (SalarySlipDTO bulletin : bulletinsFiltres) {
                try {
                    // Annuler le bulletin existant
                    annulerBulletin(bulletin.getName(), apiKey, apiSecret);
                    bulletinsAnnules++;

                    // Récupérer l'employé
                    EmployeeDTO employe = hrService.getEmployeById(bulletin.getEmployee(), apiKey, apiSecret);

                    // Calculer le nouveau salaire de base
                    Double nouveauSalaireBase = calculerNouveauSalaire(employe.getBasicSalary(),
                            ajustement.getAdjustmentType(),
                            ajustement.getAdjustmentPercentage());

                    // Mettre à jour le salaire de base de l'employé
                    mettreAJourSalaireEmploye(employe, nouveauSalaireBase, apiKey, apiSecret);

                    // Créer un nouveau bulletin avec le nouveau salaire
                    SalarySlipDTO nouveauBulletin = creerNouveauBulletin(bulletin, nouveauSalaireBase, apiKey, apiSecret);
                    nouveauxBulletins++;

                    employesTraites.add(employe.getName());

                } catch (Exception e) {
                    System.err.println("Erreur lors du traitement du bulletin " + bulletin.getName() + ": " + e.getMessage());
                }
            }

            resultat.setAffectedEmployees(employesTraites.size());
            resultat.setCancelledSlips(bulletinsAnnules);
            resultat.setNewSlipsCreated(nouveauxBulletins);
            resultat.setProcessedEmployees(employesTraites);
            resultat.setMessage("Ajustement salarial traité avec succès");

            // Enregistrer l'historique de l'ajustement
            enregistrerHistoriqueAjustement(ajustement, resultat, apiKey, apiSecret);

        } catch (Exception e) {
            resultat.setMessage("Erreur lors du traitement : " + e.getMessage());
            throw new RuntimeException("Erreur lors du traitement de l'ajustement salarial", e);
        }

        return resultat;
    }*//*


    public SalaryAdjustmentResultDTO traiterAjustementSalarial(SalaryAdjustmentDTO ajustement,
                                                               String apiKey, String apiSecret) {
        SalaryAdjustmentResultDTO resultat = new SalaryAdjustmentResultDTO();
        List<String> employesTraites = new ArrayList<>();

        try {
            // 1. Récupérer tous les bulletins de paie dans la période
            List<SalarySlipDTO> bulletins = getBulletinsPeriode(ajustement.getStartDate(),
                    ajustement.getEndDate(), apiKey, apiSecret);

            // 2. Filtrer les bulletins selon les critères
            List<SalarySlipDTO> bulletinsFiltres = filtrerBulletins(bulletins, ajustement);

            int bulletinsAnnules = 0;
            int nouveauxBulletins = 0;

            // 3. Traiter chaque bulletin filtré
            for (SalarySlipDTO bulletin : bulletinsFiltres) {
                try {
                    // Annuler le bulletin existant
                    annulerBulletin(bulletin.getName(), apiKey, apiSecret);
                    bulletinsAnnules++;

                    // Récupérer l'employé
                    EmployeeDTO employe = hrService.getEmployeById(bulletin.getEmployee(), apiKey, apiSecret);

                    // Récupérer la structure salariale de l'employé pour obtenir le salaire de base
                    Map<String, Object> salaryStructure = getSalaryStructure(employe.getSalaryStructure(), apiKey, apiSecret);
                    Double salaireBaseActuel = getBasicSalaryFromStructure(salaryStructure);

                    // Calculer le nouveau salaire de base
                    Double nouveauSalaireBase = calculerNouveauSalaire(salaireBaseActuel,
                            ajustement.getAdjustmentType(),
                            ajustement.getAdjustmentPercentage());

                    // Mettre à jour le salaire de base dans la structure salariale
                    updateBasicSalaryInStructure(employe.getSalaryStructure(), nouveauSalaireBase, apiKey, apiSecret);

                    // Créer un nouveau bulletin avec le nouveau salaire
                    SalarySlipDTO nouveauBulletin = creerNouveauBulletin(bulletin, nouveauSalaireBase, apiKey, apiSecret);
                    nouveauxBulletins++;

                    employesTraites.add(employe.getName());

                } catch (Exception e) {
                    System.err.println("Erreur lors du traitement du bulletin " + bulletin.getName() + ": " + e.getMessage());
                }
            }

            resultat.setAffectedEmployees(employesTraites.size());
            resultat.setCancelledSlips(bulletinsAnnules);
            resultat.setNewSlipsCreated(nouveauxBulletins);
            resultat.setProcessedEmployees(employesTraites);
            resultat.setMessage("Ajustement salarial traité avec succès");

            // Enregistrer l'historique de l'ajustement
            enregistrerHistoriqueAjustement(ajustement, resultat, apiKey, apiSecret);

        } catch (Exception e) {
            resultat.setMessage("Erreur lors du traitement : " + e.getMessage());
            throw new RuntimeException("Erreur lors du traitement de l'ajustement salarial", e);
        }

        return resultat;
    }

    private Map<String, Object> getSalaryStructure(String structureName, String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Salary Structure/" + structureName,
                apiKey, apiSecret
        );
        return response.getBody();
    }

    // Nouvelle méthode pour extraire le salaire de base de la structure
    private Double getBasicSalaryFromStructure(Map<String, Object> salaryStructure) {
        if (salaryStructure == null) return 0.0;

        List<Map<String, Object>> earnings = (List<Map<String, Object>>) salaryStructure.get("earnings");
        if (earnings != null) {
            for (Map<String, Object> earning : earnings) {
                if ("Basic Salary".equals(earning.get("salary_component")) ||
                        "Salaire de Base".equals(earning.get("salary_component"))) {
                    return Double.parseDouble(earning.get("amount").toString());
                }
            }
        }
        return 0.0;
    }

    private void updateBasicSalaryInStructure(String structureName, Double newBasicSalary,
                                              String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> structure = getSalaryStructure(structureName, apiKey, apiSecret);

        List<Map<String, Object>> earnings = (List<Map<String, Object>>) structure.get("earnings");
        if (earnings != null) {
            for (Map<String, Object> earning : earnings) {
                if ("Basic Salary".equals(earning.get("salary_component")) ||
                        "Salaire de Base".equals(earning.get("salary_component"))) {
                    earning.put("amount", newBasicSalary);
                    break;
                }
            }
        }

        requestBody.put("earnings", earnings);
        erpnextApiService.put(
                "/api/resource/Salary Structure/" + structureName,
                requestBody, apiKey, apiSecret
        );
    }

    private List<SalarySlipDTO> getBulletinsPeriode(String startDate, String endDate,
                                                    String apiKey, String apiSecret) {
        String filters = String.format("[[\"start_date\",\">=\",\"%s\"],[\"end_date\",\"<=\",\"%s\"]]",
                startDate, endDate);

        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Salary Slip?fields=[\"*\"]&filters=" + filters,
                apiKey, apiSecret
        );

        return mapSalarySlipsResponse(response.getBody());
    }

    private List<SalarySlipDTO> filtrerBulletins(List<SalarySlipDTO> bulletins, SalaryAdjustmentDTO ajustement) {
        return bulletins.stream()
                .filter(bulletin -> {
                    // Vérifier si le bulletin contient le composant spécifié
                    return bulletin.getEarnings().stream()
                            .anyMatch(earning -> {
                                if (!earning.getSalaryComponent().equals(ajustement.getSalaryComponent())) {
                                    return false;
                                }

                                Double montant = earning.getAmount();
                                Double montantComparaison = ajustement.getComparisonAmount();

                                switch (ajustement.getComparisonOperator()) {
                                    case "lt":
                                        return montant < montantComparaison;
                                    case "gt":
                                        return montant > montantComparaison;
                                    case "eq":
                                        return montant.equals(montantComparaison);
                                    case "lte":
                                        return montant <= montantComparaison;
                                    case "gte":
                                        return montant >= montantComparaison;
                                    default:
                                        return false;
                                }
                            });
                })
                .collect(Collectors.toList());
    }

    private void annulerBulletin(String bulletinId, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("docstatus", 2); // 2 = Cancelled

        erpnextApiService.put(
                "/api/resource/Salary Slip/" + bulletinId,
                requestBody, apiKey, apiSecret
        );
    }

    private Double calculerNouveauSalaire(Double salaireActuel, String typeAjustement, Double pourcentage) {
        if (salaireActuel == null) {
            return 0.0;
        }

        Double facteur = pourcentage / 100.0;

        if ("plus".equals(typeAjustement)) {
            return salaireActuel * (1 + facteur);
        } else {
            return salaireActuel * (1 - facteur);
        }
    }

    private void mettreAJourSalaireEmploye(EmployeeDTO employe, Double nouveauSalaire,
                                           String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("basic_salary", nouveauSalaire);

        erpnextApiService.put(
                "/api/resource/Employee/" + employe.getName(),
                requestBody, apiKey, apiSecret
        );
    }

    private SalarySlipDTO creerNouveauBulletin(SalarySlipDTO ancienBulletin, Double nouveauSalaireBase,
                                               String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Salary Slip");
        requestBody.put("employee", ancienBulletin.getEmployee());
        requestBody.put("start_date", ancienBulletin.getStartDate());
        requestBody.put("end_date", ancienBulletin.getEndDate());
        requestBody.put("salary_structure", ancienBulletin.getSalaryStructure());

        // Recalculer les gains avec le nouveau salaire de base
        List<Map<String, Object>> earnings = new ArrayList<>();
        for (SalaryDetailDTO earning : ancienBulletin.getEarnings()) {
            Map<String, Object> earningMap = new HashMap<>();
            earningMap.put("salary_component", earning.getSalaryComponent());

            // Si c'est le salaire de base, utiliser le nouveau montant
            if ("Basic Salary".equals(earning.getSalaryComponent()) ||
                    "Salaire de Base".equals(earning.getSalaryComponent())) {
                earningMap.put("amount", nouveauSalaireBase);
            } else {
                earningMap.put("amount", earning.getAmount());
            }

            earnings.add(earningMap);
        }
        requestBody.put("earnings", earnings);

        // Copier les déductions
        List<Map<String, Object>> deductions = new ArrayList<>();
        for (SalaryDetailDTO deduction : ancienBulletin.getDeductions()) {
            Map<String, Object> deductionMap = new HashMap<>();
            deductionMap.put("salary_component", deduction.getSalaryComponent());
            deductionMap.put("amount", deduction.getAmount());
            deductions.add(deductionMap);
        }
        requestBody.put("deductions", deductions);

        ResponseEntity<Map> response = erpnextApiService.post(
                "/api/resource/Salary Slip",
                requestBody, apiKey, apiSecret
        );

        return mapSalarySlipResponse(response.getBody());
    }

    private void enregistrerHistoriqueAjustement(SalaryAdjustmentDTO ajustement,
                                                 SalaryAdjustmentResultDTO resultat,
                                                 String apiKey, String apiSecret) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Salary Adjustment Log");
        requestBody.put("adjustment_date", new Date());
        requestBody.put("salary_component", ajustement.getSalaryComponent());
        requestBody.put("comparison_operator", ajustement.getComparisonOperator());
        requestBody.put("comparison_amount", ajustement.getComparisonAmount());
        requestBody.put("adjustment_type", ajustement.getAdjustmentType());
        requestBody.put("adjustment_percentage", ajustement.getAdjustmentPercentage());
        requestBody.put("affected_employees", resultat.getAffectedEmployees());
        requestBody.put("cancelled_slips", resultat.getCancelledSlips());
        requestBody.put("new_slips_created", resultat.getNewSlipsCreated());

        try {
            erpnextApiService.post(
                    "/api/resource/Salary Adjustment Log",
                    requestBody, apiKey, apiSecret
            );
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer le processus principal
            System.err.println("Erreur lors de l'enregistrement de l'historique : " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getHistoriqueAjustements(String apiKey, String apiSecret) {
        try {
            ResponseEntity<Map> response = erpnextApiService.get(
                    "/api/resource/Salary Adjustment Log?fields=[\"*\"]&order_by=creation desc",
                    apiKey, apiSecret
            );

            Map<String, Object> responseBody = response.getBody();
            return (List<Map<String, Object>>) responseBody.get("data");
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Méthodes de mapping
    private List<SalarySlipDTO> mapSalarySlipsResponse(Map<String, Object> response) {
        List<SalarySlipDTO> bulletins = new ArrayList<>();
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

        if (data != null) {
            for (Map<String, Object> item : data) {
                bulletins.add(mapSalarySlipResponse(item));
            }
        }

        return bulletins;
    }

    private SalarySlipDTO mapSalarySlipResponse(Map<String, Object> data) {
        SalarySlipDTO dto = new SalarySlipDTO();
        dto.setName((String) data.get("name"));
        dto.setEmployee((String) data.get("employee"));
        dto.setEmployeeName((String) data.get("employee_name"));
        dto.setStartDate(LocalDate.parse((String) data.get("start_date")));
        dto.setEndDate(LocalDate.parse((String) data.get("end_date")));
        dto.setSalaryStructure((String) data.get("salary_structure"));
        dto.setStatus((String) data.get("status"));

        if (data.get("gross_pay") != null) {
            dto.setGrossPay(Double.parseDouble(data.get("gross_pay").toString()));
        }
        if (data.get("total_deduction") != null) {
            dto.setTotalDeduction(Double.parseDouble(data.get("total_deduction").toString()));
        }
        if (data.get("net_pay") != null) {
            dto.setNetPay(Double.parseDouble(data.get("net_pay").toString()));
        }

        // Mapper les gains et déductions
        List<SalaryDetailDTO> earnings = new ArrayList<>();
        List<Map<String, Object>> earningsData = (List<Map<String, Object>>) data.get("earnings");
        if (earningsData != null) {
            for (Map<String, Object> earning : earningsData) {
                SalaryDetailDTO earningDTO = new SalaryDetailDTO();
                earningDTO.setSalaryComponent((String) earning.get("salary_component"));
                if (earning.get("amount") != null) {
                    earningDTO.setAmount(Double.parseDouble(earning.get("amount").toString()));
                }
                earnings.add(earningDTO);
            }
        }
        dto.setEarnings(earnings);

        List<SalaryDetailDTO> deductions = new ArrayList<>();
        List<Map<String, Object>> deductionsData = (List<Map<String, Object>>) data.get("deductions");
        if (deductionsData != null) {
            for (Map<String, Object> deduction : deductionsData) {
                SalaryDetailDTO deductionDTO = new SalaryDetailDTO();
                deductionDTO.setSalaryComponent((String) deduction.get("salary_component"));
                if (deduction.get("amount") != null) {
                    deductionDTO.setAmount(Double.parseDouble(deduction.get("amount").toString()));
                }
                deductions.add(deductionDTO);
            }
        }
        dto.setDeductions(deductions);

        return dto;
    }
}*/
