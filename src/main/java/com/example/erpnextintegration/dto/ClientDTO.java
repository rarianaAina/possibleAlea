package com.example.erpnextintegration.dto;

import lombok.Data;

@Data
public class ClientDTO {
    private String name;
    private String owner;
    private String creation;
    private String modified;
    private String modified_by;
    private int docstatus;
    private int idx;
    private String naming_series;
    private String salutation;
    private String customer_name;
    private String customer_type;
    private String customer_group;
    private String territory;
    private String gender;
    private String lead_name;
    private String opportunity_name;
    private String prospect_name;
    private String account_manager;
    private String image;
    private String default_currency;
    private String default_bank_account;
    private String default_price_list;
    private int is_internal_customer;
    private String represents_company;
    private String market_segment;
    private String industry;
    private String customer_pos_id;
    private String website;
    private String language;
    private String customer_details;
    private String customer_primary_address;
    private String primary_address;
    private String customer_primary_contact;
    private String mobile_no;
    private String email_id;
    private String first_name;
    private String last_name;
    private String tax_id;
    private String tax_category;
    private String tax_withholding_category;
    private String payment_terms;
    private String loyalty_program;
    private String loyalty_program_tier;
    private String default_sales_partner;
    private double default_commission_rate;
    private int so_required;
    private int dn_required;
    private int is_frozen;
    private int disabled;
}
