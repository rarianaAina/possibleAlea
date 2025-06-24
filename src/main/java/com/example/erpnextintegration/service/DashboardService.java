package com.example.erpnextintegration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.erpnextintegration.dto.DashboardStatsDTO;
import com.example.erpnextintegration.dto.ActivityDataDTO;

@Service
public class DashboardService {

    @Autowired
    private ErpnextApiService erpnextApiService;

    public DashboardStatsDTO getDashboardStats(String apiKey, String apiSecret) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Récupérer les statistiques des clients
        ResponseEntity<Map> clientsResponse = erpnextApiService.get(
                "/api/resource/Customer?fields=[\"name\"]&limit_page_length=0",
                apiKey,
                apiSecret
        );
        Map<String, Object> clientsData = clientsResponse.getBody();
        List<Map<String, Object>> clients = (List<Map<String, Object>>) clientsData.get("data");
        stats.setTotalClients(clients.size());

        // Récupérer les statistiques des commandes
        ResponseEntity<Map> commandesResponse = erpnextApiService.get(
                "/api/resource/Sales Order?fields=[\"name\",\"status\",\"grand_total\"]&limit_page_length=0",
                apiKey,
                apiSecret
        );
        Map<String, Object> commandesData = commandesResponse.getBody();
        List<Map<String, Object>> commandes = (List<Map<String, Object>>) commandesData.get("data");
        stats.setTotalCommandes(commandes.size());

        double chiffreAffaires = 0;
        int commandesEnCours = 0;
        for (Map<String, Object> commande : commandes) {
            chiffreAffaires += Double.parseDouble(commande.get("grand_total").toString());
            if (!commande.get("status").equals("Completed")) {
                commandesEnCours++;
            }
        }
        stats.setChiffreAffaires(chiffreAffaires);
        stats.setCommandesEnCours(commandesEnCours);

        // Récupérer les statistiques des projets
        ResponseEntity<Map> projetsResponse = erpnextApiService.get(
                "/api/resource/Project?fields=[\"name\",\"status\"]&limit_page_length=0",
                apiKey,
                apiSecret
        );
        Map<String, Object> projetsData = projetsResponse.getBody();
        List<Map<String, Object>> projets = (List<Map<String, Object>>) projetsData.get("data");
        stats.setTotalProjets(projets.size());

        int projetsEnCours = 0;
        for (Map<String, Object> projet : projets) {
            if (projet.get("status").equals("Open")) {
                projetsEnCours++;
            }
        }
        stats.setProjetsEnCours(projetsEnCours);

        // Récupérer les données d'activité des 7 derniers jours
        List<ActivityDataDTO> activityDataList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);

            ResponseEntity<Map> dailyCommandesResponse = erpnextApiService.get(
                    "/api/resource/Sales Order?fields=[\"name\"]&filters=[[\"transaction_date\",\"=\",\"" + dateStr + "\"]]",
                    apiKey,
                    apiSecret
            );
            Map<String, Object> dailyCommandesData = dailyCommandesResponse.getBody();
            List<Map<String, Object>> dailyCommandes = (List<Map<String, Object>>) dailyCommandesData.get("data");

            ResponseEntity<Map> dailyProjetsResponse = erpnextApiService.get(
                    "/api/resource/Project?fields=[\"name\"]&filters=[[\"expected_start_date\",\"=\",\"" + dateStr + "\"]]",
                    apiKey,
                    apiSecret
            );
            Map<String, Object> dailyProjetsData = dailyProjetsResponse.getBody();
            List<Map<String, Object>> dailyProjets = (List<Map<String, Object>>) dailyProjetsData.get("data");

            ActivityDataDTO activityData = new ActivityDataDTO();
            activityData.setDate(dateStr);
            activityData.setCommandes(dailyCommandes.size());
            activityData.setProjets(dailyProjets.size());
            activityDataList.add(activityData);
        }

        stats.setActivityData(activityDataList.toArray(new ActivityDataDTO[0]));

        return stats;
    }
}