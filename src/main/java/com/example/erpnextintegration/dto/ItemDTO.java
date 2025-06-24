package com.example.erpnextintegration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemDTO {

    @JsonProperty("item_code")
    private String itemCode;

    private String name;
    private double qty;
    private double rate;
    private double amount;
    private String uom;
    private String warehouse;

    @JsonProperty("schedule_date")

    private String scheduleDate;

    private String owner;

    private String creation;

    private String modified;

    @JsonProperty("modified_by")
    private String modifiedBy;

    @Override
    public String toString() {
        return name; // pour que Thymeleaf puisse l'utiliser comme valeur dans les listes
    }
}

