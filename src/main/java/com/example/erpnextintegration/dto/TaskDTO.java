package com.example.erpnextintegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TaskDTO {
    private String name;
    private String owner;
    private String creation;

    private String modified;

    @JsonProperty("modified_by")
    private String modified_by;

    private int docstatus;
    private int idx;
    private String subject;
    private String project;

    @JsonProperty("is_group")
    private int isGroup;

    @JsonProperty("is_template")
    private int isTemplate;

    private String status;
    private String priority;

    @JsonProperty("task_weight")
    private double taskWeight;

    @JsonProperty("exp_start_date")
    private String exp_start_date;

    @JsonProperty("expected_time")
    private double expected_time;

    private int start;

    @JsonProperty("exp_end_date")
    private String exp_end_date;

    private double progress;
    private int duration;

    @JsonProperty("is_milestone")
    private int isMilestone;

    private String description;

    @JsonProperty("depends_on_tasks")
    private String dependsOnTasks;

    @JsonProperty("actual_time")
    private double actualTime;

    @JsonProperty("total_costing_amount")
    private double totalCostingAmount;

    @JsonProperty("total_billing_amount")
    private double totalBillingAmount;

    private String company;
    private int lft;
    private int rgt;

    @JsonProperty("old_parent")
    private String oldParent;

    private String doctype;
    private String issue;
    private String type;
    // À ajuster si on a le format des objets
}
