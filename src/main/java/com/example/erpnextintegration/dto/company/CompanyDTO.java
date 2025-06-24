package com.example.erpnextintegration.dto.company;

import lombok.Data;

@Data
public class CompanyDTO {
    private String name;
    private String companyName;
    private String abbr;
    private String country;
    private String defaultCurrency;
    private String domain;
    private boolean isActive;
}