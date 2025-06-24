package com.example.erpnextintegration.dto.employee;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private String name; // ID unique dans ERPNext
    private String employeeNumber;
    private String first_name;
    private String lastName;
    private String email;
    private String phone;
    private String mobilePhone;
    private String gender;
    private LocalDate date_of_birth;
    private LocalDate date_of_joining;
    private String department;
    private String designation; // Poste ou fonction
    private String employeeStatus; // Actif, En période d'essai, etc.
    private String employmentType; // CDI, CDD, etc.
    private String company;
    private String branchOffice;
    private LocalDate contractEndDate; // Pour les CDD
    private String bankName;
    private String bankAccountNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private String nationalId; // Numéro de sécurité sociale
    private String taxId; // Numéro fiscal
    private LocalDate probationEndDate;
    private List<String> skills;
    private String imageUrl;
    private String notes;
    private String employeeName;
    private String status;
}