package com.example.erpnextintegration.dto;

import lombok.Data;

@Data
public class CommandeDTO {
    private String name;
    private String owner;
    private String creation;
    private String modified;
    private String modified_by;
    private String delivery_date;
    private String customer_name;
    private String customer;
    private String order_type;
    private String transaction_date;
    private double total_qty;
    private String base_total;
    private double total;
    private String status;
    private String currency;
}
