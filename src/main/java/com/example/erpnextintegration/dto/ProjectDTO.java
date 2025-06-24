package com.example.erpnextintegration.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectDTO {
    private String name;
    private String owner;
    private String creation;
    private String modified;
    private String modified_by;
    private String doctype;
    private int docstatus;
    private int idx;
    private String naming_series;
    private String project_name;
    private String status;
    private String project_type;
    private String is_active;
    private String percent_complete_method;
    private double percent_complete;
    private String project_template;
    private String expected_start_date;
    private String expected_end_date;
    private String priority;
    private String department;
    private String customer;
    private String sales_order;
    private String copied_from;
    private String notes;
    private String actual_start_date;
    private double actual_time;
    private String actual_end_date;
    private double estimated_costing;
    private double total_costing_amount;
    private double total_purchase_cost;
    private String company;
    private double total_sales_amount;
    private double total_billable_amount;
    private double total_billed_amount;
    private double total_consumed_material_cost;
    private String cost_center;
    private double gross_margin;
    private double per_gross_margin;
    private int collect_progress;
    private String holiday_list;
    private String frequency;
    private String from_time;
    private String to_time;
    private String first_email;
    private String second_email;
    private String daily_time_to_send;
    private String day_to_send;
    private String weekly_time_to_send;
    private String message;
    private List<String> users;
}
