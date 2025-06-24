package com.example.erpnextintegration.service;


import com.example.erpnextintegration.dto.alea.SalarySlipFilterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.erpnextintegration.dto.payroll.*;
import com.example.erpnextintegration.dto.employee.EmployeeDTO;
import com.example.erpnextintegration.dto.company.CompanyDTO;

@Service
public class PayrollService {

    @Autowired
    private ErpnextApiService erpnextApiService;

    public List<SalaryStructureDTO> getStructuresSalariales(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Salary Structure?fields=[\"*\"]",
                apiKey,
                apiSecret
        );

        return mapSalaryStructuresResponse(response.getBody());
    }

    public void ajouterStructureSalariale(SalaryStructureDTO structure, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = mapSalaryStructureToRequest(structure);

        erpnextApiService.post(
                "/api/resource/Salary Structure",
                requestBody,
                apiKey,
                apiSecret
        );
    }


    public List<SalarySlipDTO> getBulletinsPaie(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Salary Slip?fields=[\"*\"]",
                apiKey,
                apiSecret
        );

        return mapSalarySlipsResponse(response.getBody());
    }

    public SalarySlipDTO getBulletinPaieById(String id, String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Salary Slip/" + id,
                apiKey,
                apiSecret
        );

        return mapSalarySlipResponse(response.getBody());
    }

    public void genererBulletinPaie(SalarySlipDTO bulletin, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = mapSalarySlipToRequest(bulletin);

        erpnextApiService.post(
                "/api/resource/Salary Slip",
                requestBody,
                apiKey,
                apiSecret
        );
    }

    public void soumettreBulletinPaie(String id, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = Map.of("docstatus", 1);

        erpnextApiService.put(
                "/api/resource/Salary Slip/" + id,
                requestBody,
                apiKey,
                apiSecret
        );
    }

    public List<EmployeeDTO> getEmployesActifs(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Employee?fields=[\"*\"]&filters=[[\"status\",\"=\",\"Active\"]]",
                apiKey,
                apiSecret
        );

        return mapEmployeesResponse(response.getBody());
    }

    public List<SalaryComponentDTO> getComposantsSalariaux(String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Salary Component?fields=[\"*\"]",
                apiKey,
                apiSecret
        );

        return mapSalaryComponentsResponse(response.getBody());
    }

    public SalaryComponentDTO getComposantSalarialById(String id, String apiKey, String apiSecret) {
        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Salary Component/" + id,
                apiKey,
                apiSecret
        );

        return mapSalaryComponentResponse(response.getBody());
    }

    public void modifierComposantSalarial(String id, SalaryComponentDTO composant, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = mapSalaryComponentToRequest(composant);

        erpnextApiService.put(
                "/api/resource/Salary Component/" + id,
                requestBody,
                apiKey,
                apiSecret
        );
    }

    public void ajouterComposantSalarial(SalaryComponentDTO composant, String apiKey, String apiSecret) {
        Map<String, Object> requestBody = mapSalaryComponentToRequest(composant);

        erpnextApiService.post(
                "/api/resource/Salary Component",
                requestBody,
                apiKey,
                apiSecret
        );
    }


/*
    private Map<String, Object> mapSalaryStructureToRequest(SalaryStructureDTO structure) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Salary Structure");
        requestBody.put("name", structure.getName());
        requestBody.put("payroll_frequency", structure.getPayrollFrequency());
        requestBody.put("currency", structure.getCurrency());
        requestBody.put("from_date", structure.getFromDate());
        requestBody.put("company", structure.getCompany());
        requestBody.put("is_active", structure.isActive());

        // Mapper les gains
        List<Map<String, Object>> earnings = new ArrayList<>();
        if (structure.getEarnings() != null) {
            for (SalaryComponentDTO earning : structure.getEarnings()) {
                Map<String, Object> earningMap = new HashMap<>();
                earningMap.put("salary_component", earning.getSalaryComponent());
                earningMap.put("amount", earning.getAmount());
                earningMap.put("formula", earning.getFormula());
                earningMap.put("condition", earning.getCondition());
                earnings.add(earningMap);
            }
        }
        requestBody.put("earnings", earnings);

        // Mapper les déductions
        List<Map<String, Object>> deductions = new ArrayList<>();
        if (structure.getDeductions() != null) {
            for (SalaryComponentDTO deduction : structure.getDeductions()) {
                Map<String, Object> deductionMap = new HashMap<>();
                deductionMap.put("salary_component", deduction.getSalaryComponent());
                deductionMap.put("amount", deduction.getAmount());
                deductionMap.put("formula", deduction.getFormula());
                deductionMap.put("condition", deduction.getCondition());
                deductions.add(deductionMap);
            }
        }
        requestBody.put("deductions", deductions);

        return requestBody;
    }
*/


    private SalaryComponentDTO mapSalaryComponentResponse(Map<String, Object> data) {
        SalaryComponentDTO dto = new SalaryComponentDTO();
        dto.setName((String) data.get("name"));
        dto.setSalaryComponent((String) data.get("salary_component"));
        dto.setType((String) data.get("type"));
        dto.setDescription((String) data.get("description"));
        dto.setCondition((String) data.get("condition"));
        dto.setFormula((String) data.get("formula"));

        if (data.get("amount") != null) {
            dto.setAmount(Double.parseDouble(data.get("amount").toString()));
        }

        if (data.get("is_active") != null) {
            dto.setActive((Boolean) data.get("is_active"));
        }

        return dto;
    }

    private Map<String, Object> mapSalaryComponentToRequest(SalaryComponentDTO component) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Salary Component");
        requestBody.put("salary_component", component.getSalaryComponent());
        requestBody.put("type", component.getType());
        requestBody.put("description", component.getDescription());
        requestBody.put("condition", component.getCondition());
        requestBody.put("formula", component.getFormula());
        requestBody.put("amount", component.getAmount());
        requestBody.put("is_active", component.isActive());

        return requestBody;
    }

    private List<CompanyDTO> mapCompaniesResponse(Map<String, Object> response) {
        List<CompanyDTO> companies = new ArrayList<>();
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

        if (data != null) {
            for (Map<String, Object> item : data) {
                CompanyDTO dto = new CompanyDTO();
                dto.setName((String) item.get("name"));
                dto.setCompanyName((String) item.get("company_name"));
                dto.setAbbr((String) item.get("abbr"));
                dto.setCountry((String) item.get("country"));
                dto.setDefaultCurrency((String) item.get("default_currency"));
                companies.add(dto);
            }
        }

        return companies;
    }

/*
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
*/

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

    private Map<String, Object> mapSalarySlipToRequest(SalarySlipDTO bulletin) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Salary Slip");
        requestBody.put("employee", bulletin.getEmployee());
        requestBody.put("start_date", bulletin.getStartDate());
        requestBody.put("end_date", bulletin.getEndDate());
        requestBody.put("salary_structure", bulletin.getSalaryStructure());

        return requestBody;
    }

    private List<EmployeeDTO> mapEmployeesResponse(Map<String, Object> response) {
        List<EmployeeDTO> employees = new ArrayList<>();
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

        if (data != null) {
            for (Map<String, Object> item : data) {
                EmployeeDTO dto = new EmployeeDTO();
                dto.setName((String) item.get("name"));
                dto.setEmployeeName((String) item.get("employee_name"));
                dto.setEmployeeNumber((String) item.get("employee_number"));
                dto.setDepartment((String) item.get("department"));
                dto.setDesignation((String) item.get("designation"));
                dto.setStatus((String) item.get("status"));
                employees.add(dto);
            }
        }

        return employees;
    }

    private List<SalaryComponentDTO> mapSalaryComponentsResponse(Map<String, Object> response) {
        List<SalaryComponentDTO> components = new ArrayList<>();
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

        if (data != null) {
            for (Map<String, Object> item : data) {
                components.add(mapSalaryComponentResponse(item));
            }
        }

        return components;
    }

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

    private SalaryStructureDTO mapSalaryStructureResponse(Map<String, Object> data) {
        SalaryStructureDTO dto = new SalaryStructureDTO();
        dto.setName((String) data.get("name"));
        dto.setPayrollFrequency((String) data.get("payroll_frequency"));
        dto.setCurrency((String) data.get("currency"));
        dto.setFromDate((String) data.get("from_date"));
        dto.setCompany((String) data.get("company"));

        if (data.get("is_active") != null) {
            dto.setActive((Boolean) data.get("is_active"));
        }

        // Mapper les gains et déductions
        List<SalaryComponentDTO> earnings = new ArrayList<>();
        List<Map<String, Object>> earningsData = (List<Map<String, Object>>) data.get("earnings");
        if (earningsData != null) {
            for (Map<String, Object> earning : earningsData) {
                SalaryComponentDTO componentDTO = new SalaryComponentDTO();
                componentDTO.setSalaryComponent((String) earning.get("salary_component"));
                componentDTO.setType("Earning");
                if (earning.get("amount") != null) {
                    componentDTO.setAmount(Double.parseDouble(earning.get("amount").toString()));
                }
                componentDTO.setFormula((String) earning.get("formula"));
                componentDTO.setCondition((String) earning.get("condition"));
                earnings.add(componentDTO);
            }
        }
        dto.setEarnings(earnings);

        List<SalaryComponentDTO> deductions = new ArrayList<>();
        List<Map<String, Object>> deductionsData = (List<Map<String, Object>>) data.get("deductions");
        if (deductionsData != null) {
            for (Map<String, Object> deduction : deductionsData) {
                SalaryComponentDTO componentDTO = new SalaryComponentDTO();
                componentDTO.setSalaryComponent((String) deduction.get("salary_component"));
                componentDTO.setType("Deduction");
                if (deduction.get("amount") != null) {
                    componentDTO.setAmount(Double.parseDouble(deduction.get("amount").toString()));
                }
                componentDTO.setFormula((String) deduction.get("formula"));
                componentDTO.setCondition((String) deduction.get("condition"));
                deductions.add(componentDTO);
            }
        }
        dto.setDeductions(deductions);

        return dto;
    }

    private List<SalaryStructureDTO> mapSalaryStructuresResponse(Map<String, Object> response) {
        List<SalaryStructureDTO> structures = new ArrayList<>();
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

        if (data != null) {
            for (Map<String, Object> item : data) {
                structures.add(mapSalaryStructureResponse(item));
            }
        }

        return structures;
    }

    private Map<String, Object> mapSalaryStructureToRequest(SalaryStructureDTO structure) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("doctype", "Salary Structure");
        requestBody.put("name", structure.getName());
        requestBody.put("payroll_frequency", structure.getPayrollFrequency());
        requestBody.put("currency", structure.getCurrency());
        requestBody.put("from_date", structure.getFromDate());
        requestBody.put("company", structure.getCompany());
        requestBody.put("is_active", structure.isActive());

        // Mapper les gains
        List<Map<String, Object>> earnings = new ArrayList<>();
        if (structure.getEarnings() != null) {
            for (SalaryComponentDTO earning : structure.getEarnings()) {
                Map<String, Object> earningMap = new HashMap<>();
                earningMap.put("salary_component", earning.getSalaryComponent());
                earningMap.put("amount", earning.getAmount());
                earningMap.put("formula", earning.getFormula());
                earningMap.put("condition", earning.getCondition());
                earnings.add(earningMap);
            }
        }
        requestBody.put("earnings", earnings);

        // Mapper les déductions
        List<Map<String, Object>> deductions = new ArrayList<>();
        if (structure.getDeductions() != null) {
            for (SalaryComponentDTO deduction : structure.getDeductions()) {
                Map<String, Object> deductionMap = new HashMap<>();
                deductionMap.put("salary_component", deduction.getSalaryComponent());
                deductionMap.put("amount", deduction.getAmount());
                deductionMap.put("formula", deduction.getFormula());
                deductionMap.put("condition", deduction.getCondition());
                deductions.add(deductionMap);
            }
        }
        requestBody.put("deductions", deductions);

        return requestBody;
    }

    public List<SalarySlipDTO> getBulletinsFiltres(SalarySlipFilterDTO filter, String apiKey, String apiSecret) {
        StringBuilder filtersBuilder = new StringBuilder();
        List<String> conditions = new ArrayList<>();

        if (filter.getStartDate() != null && !filter.getStartDate().isEmpty()) {
            conditions.add("[\"start_date\",\">=\",\"" + filter.getStartDate() + "\"]");
        }

        if (filter.getEndDate() != null && !filter.getEndDate().isEmpty()) {
            conditions.add("[\"end_date\",\"<=\",\"" + filter.getEndDate() + "\"]");
        }

        if (filter.getEmployee() != null && !filter.getEmployee().isEmpty()) {
            conditions.add("[\"employee\",\"=\",\"" + filter.getEmployee() + "\"]");
        }

        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            conditions.add("[\"status\",\"=\",\"" + filter.getStatus() + "\"]");
        }

        String filtersParam = "";
        if (!conditions.isEmpty()) {
            filtersParam = "&filters=[" + String.join(",", conditions) + "]";
        }

        ResponseEntity<Map> response = erpnextApiService.get(
                "/api/resource/Salary Slip?fields=[\"*\"]" + filtersParam,
                apiKey,
                apiSecret
        );

        List<SalarySlipDTO> bulletins = mapSalarySlipsResponse(response.getBody());

        // Filtrer par composant salarial si spécifié
        if (filter.getSalaryComponent() != null && !filter.getSalaryComponent().isEmpty()) {
            return bulletins.stream()
                    .filter(bulletin -> bulletin.getEarnings().stream()
                            .anyMatch(earning -> earning.getSalaryComponent().equals(filter.getSalaryComponent())))
                    .collect(java.util.stream.Collectors.toList());
        }

        return bulletins;
    }

    // Méthodes privées de mapping...

}