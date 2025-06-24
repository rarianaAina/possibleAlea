package com.example.erpnextintegration.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private int totalClients;
    private int totalCommandes;
    private int totalProjets;
    private double chiffreAffaires;
    private int commandesEnCours;
    private int projetsEnCours;
    private ActivityDataDTO[] activityData;
}