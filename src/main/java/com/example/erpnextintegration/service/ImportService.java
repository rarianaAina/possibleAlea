package com.example.erpnextintegration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour les fonctionnalités d'importation.
 */
@Service
public class ImportService {

    @Autowired
    private ErpnextApiService erpnextApiService;
    
    /**
     * Importe les données à partir des fichiers Excel.
     */
    public void importerDonnees(
            MultipartFile materialRequestFile,
            MultipartFile suppliersFile,
            MultipartFile quotationsFile,
            String apiKey,
            String apiSecret) throws IOException {

        // Traiter le fichier des fournisseurs
        List<Map<String, String>> suppliers = traiterFichierFournisseurs(suppliersFile);

        // Créer les fournisseurs dans ERPNext
        Map<String, String> supplierIds = creerFournisseurs(suppliers, apiKey, apiSecret);

        // Traiter le fichier des demandes de matériel
        List<Map<String, String>> materialRequests = traiterFichierMaterialRequest(materialRequestFile);

        // Créer les demandes de matériel dans ERPNext
        Map<String, String> materialRequestIds = creerMaterialRequests(materialRequests, apiKey, apiSecret);

        // Traiter le fichier des demandes de devis
        List<Map<String, String>> quotations = traiterFichierQuotations(quotationsFile);

        // Créer les demandes de devis dans ERPNext
        creerRequestForQuotations(quotations, materialRequestIds, supplierIds, apiKey, apiSecret);
    }

    /**
     * Traite le fichier Excel des fournisseurs.
     */
    private List<Map<String, String>> traiterFichierFournisseurs(MultipartFile file) throws IOException {
        List<Map<String, String>> suppliers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            // Ignorer l'en-tête
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Map<String, String> supplier = new HashMap<>();

                if (row.length >= 1) {
                    supplier.put("supplier_name", row[0]);

                    if (row.length >= 2) {
                        supplier.put("country", row[1]);
                    }

                    if (row.length >= 3) {
                        supplier.put("supplier_type", row[2]);
                    }

                    suppliers.add(supplier);
                }
            }
        } catch (CsvException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV des fournisseurs", e);
        }

        return suppliers;
    }

    /**
     * Crée les fournisseurs dans ERPNext.
     */
    private Map<String, String> creerFournisseurs(List<Map<String, String>> suppliers, String apiKey, String apiSecret) {
        Map<String, String> supplierIds = new HashMap<>();
        
        for (Map<String, String> supplier : suppliers) {
            String supplierName = supplier.get("supplier_name");
            
            // Vérifier si le fournisseur existe déjà
            try {
                ResponseEntity<Map> response = erpnextApiService.get(
                        "/api/resource/Supplier?filters=[[\"supplier_name\",\"=\",\"" + supplierName + "\"]]",
                        apiKey,
                        apiSecret);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                
                if (data != null && !data.isEmpty()) {
                    // Le fournisseur existe déjà
                    String supplierId = (String) data.get(0).get("name");
                    supplierIds.put(supplierName, supplierId);
                } else {
                    // Créer un nouveau fournisseur
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("doctype", "Supplier");
                    requestBody.put("supplier_name", supplierName);
                    
                    if (supplier.containsKey("country")) {
                        requestBody.put("country", supplier.get("country"));
                    }
                    
                    if (supplier.containsKey("supplier_type")) {
                        requestBody.put("supplier_type", supplier.get("supplier_type"));
                    }
                    
                    ResponseEntity<Map> createResponse = erpnextApiService.post(
                            "/api/resource/Supplier",
                            requestBody,
                            apiKey,
                            apiSecret);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> createResponseBody = createResponse.getBody();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> createdSupplier = (Map<String, Object>) createResponseBody.get("data");
                    
                    String supplierId = (String) createdSupplier.get("name");
                    supplierIds.put(supplierName, supplierId);
                }
            } catch (Exception e) {
                // Gérer l'erreur
                throw new RuntimeException("Erreur lors de la création du fournisseur " + supplierName + ": " + e.getMessage());
            }
        }
        
        return supplierIds;
    }
    
    /**
     * Traite le fichier Excel des demandes de matériel.
     */
/*
    private List<Map<String, String>> traiterFichierMaterialRequest(MultipartFile file) throws IOException {
        List<Map<String, String>> materialRequests = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            // Ignorer l'en-tête
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Map<String, String> materialRequest = new HashMap<>();

                if (row.length >= 1) {
                    materialRequest.put("date", row[0]);

                    if (row.length >= 2) {
                        materialRequest.put("item_name", row[1]);
                    }

                    if (row.length >= 3) {
                        materialRequest.put("item_group", row[2]);
                    }

                    if (row.length >= 4) {
                        materialRequest.put("required_by", row[3]);
                    }

                    if (row.length >= 5) {
                        materialRequest.put("quantity", row[4]);
                    }

                    if (row.length >= 6) {
                        materialRequest.put("purpose", row[5]);
                    }

                    if (row.length >= 7) {
                        materialRequest.put("warehouse", row[6]);
                    }

                    if (row.length >= 8) {
                        materialRequest.put("ref", row[7]);
                    }

                    materialRequests.add(materialRequest);
                }
            }
        } catch (CsvException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV des demandes de matériel", e);
        }

        return materialRequests;
    }
*/

    private List<Map<String, String>> traiterFichierMaterialRequest(MultipartFile file) throws IOException {
        List<Map<String, String>> materialRequests = new ArrayList<>();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            // Ignorer l'en-tête
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Map<String, String> materialRequest = new HashMap<>();

                if (row.length >= 1) {
                    // Convertir la date si elle existe
                    if (row[0] != null && !row[0].isEmpty()) {
                        try {
                            LocalDate date = LocalDate.parse(row[0], inputFormatter);
                            materialRequest.put("date", date.format(outputFormatter));
                        } catch (Exception e) {
                            // Si le parsing échoue, garder la valeur originale
                            materialRequest.put("date", row[0]);
                        }
                    } else {
                        materialRequest.put("date", row[0]);
                    }

                    if (row.length >= 2) {
                        materialRequest.put("item_name", row[1]);
                    }

                    if (row.length >= 3) {
                        materialRequest.put("item_group", row[2]);
                    }

                    if (row.length >= 4) {
                        // Convertir la date required_by si elle existe
                        if (row[3] != null && !row[3].isEmpty()) {
                            try {
                                LocalDate requiredByDate = LocalDate.parse(row[3], inputFormatter);
                                materialRequest.put("required_by", requiredByDate.format(outputFormatter));
                            } catch (Exception e) {
                                // Si le parsing échoue, garder la valeur originale
                                materialRequest.put("required_by", row[3]);
                            }
                        } else {
                            materialRequest.put("required_by", row[3]);
                        }
                    }

                    if (row.length >= 5) {
                        materialRequest.put("quantity", row[4]);
                    }

                    if (row.length >= 6) {
                        materialRequest.put("purpose", row[5]);
                    }

                    if (row.length >= 7) {
                        materialRequest.put("warehouse", row[6]);
                    }

                    if (row.length >= 8) {
                        materialRequest.put("ref", row[7]);
                    }

                    materialRequests.add(materialRequest);
                }
            }
        } catch (CsvException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV des demandes de matériel", e);
        }

        return materialRequests;
    }
    /**
     * Crée les demandes de matériel dans ERPNext.
     */
/*    private Map<String, String> creerMaterialRequests(List<Map<String, String>> materialRequests, String apiKey, String apiSecret) {
        Map<String, String> materialRequestIds = new HashMap<>();

        for (Map<String, String> materialRequest : materialRequests) {
            try {
                // Vérifier si l'article existe
                String itemName = materialRequest.get("item_name");
                String itemId = verifierOuCreerItem(itemName, materialRequest.get("item_group"), apiKey, apiSecret);

                // Créer la demande de matériel
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("doctype", "Material Request");
                requestBody.put("material_request_type", "Purchase");
                requestBody.put("transaction_date", materialRequest.get("date"));

                if (materialRequest.containsKey("required_by")) {
                    requestBody.put("schedule_date", materialRequest.get("required_by"));
                }

                // Ajouter les articles
                List<Map<String, Object>> items = new ArrayList<>();
                Map<String, Object> item = new HashMap<>();
                item.put("item_code", itemId);
                item.put("qty", Double.parseDouble(materialRequest.get("quantity")));

                if (materialRequest.containsKey("warehouse")) {
                    item.put("warehouse", materialRequest.get("warehouse"));
                }

                items.add(item);
                requestBody.put("items", items);

                // Créer la demande de matériel
                ResponseEntity<Map> response = erpnextApiService.post(
                        "/api/resource/Material Request",
                        requestBody,
                        apiKey,
                        apiSecret);

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                String materialRequestId = (String) data.get("name");

                // Soumettre la demande de matériel
                Map<String, Object> submitBody = new HashMap<>();
                submitBody.put("doctype", "Material Request");
                submitBody.put("name", materialRequestId);
                submitBody.put("action", "Submit");

                erpnextApiService.post(
                        "/api/method/frappe.client.submit",
                        submitBody,
                        apiKey,
                        apiSecret);

                // Stocker l'ID de la demande de matériel
                String ref = materialRequest.get("ref");
                if (ref != null && !ref.isEmpty()) {
                    materialRequestIds.put(ref, materialRequestId);
                }
            } catch (Exception e) {
                // Gérer l'erreur
                throw new RuntimeException("Erreur lors de la création de la demande de matériel : " + e.getMessage());
            }
        }

        return materialRequestIds;
    }
    */

    private Map<String, String> creerMaterialRequests(List<Map<String, String>> materialRequests, String apiKey, String apiSecret) {
        Map<String, String> materialRequestIds = new HashMap<>();

        for (Map<String, String> materialRequest : materialRequests) {
            try {
                // Vérifier si l'article existe
                String itemName = materialRequest.get("item_name");
                String itemId = verifierOuCreerItem(itemName, materialRequest.get("item_group"), apiKey, apiSecret);

                // Vérifier si l'entrepôt existe, sinon le créer
                String warehouse = materialRequest.get("warehouse");
                String warehouseId = null;
                if (warehouse != null && !warehouse.isEmpty()) {
                    warehouseId = verifierOuCreerEntrepot(warehouse, apiKey, apiSecret);
                }

                // Créer la demande de matériel
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("doctype", "Material Request");
                requestBody.put("material_request_type", "Purchase");
                requestBody.put("transaction_date", materialRequest.get("date"));

                if (materialRequest.containsKey("required_by")) {
                    requestBody.put("schedule_date", materialRequest.get("required_by"));
                }

                // Ajouter les articles
                List<Map<String, Object>> items = new ArrayList<>();
                Map<String, Object> item = new HashMap<>();
                item.put("item_code", itemId);
                item.put("qty", Double.parseDouble(materialRequest.get("quantity")));

                if (warehouseId != null) {
                    item.put("warehouse", warehouseId);
                }

                items.add(item);
                requestBody.put("items", items);

                // Créer la demande de matériel
                ResponseEntity<Map> response = erpnextApiService.post(
                        "/api/resource/Material Request",
                        requestBody,
                        apiKey,
                        apiSecret);

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                String materialRequestId = (String) data.get("name");

                // Soumettre la demande de matériel
                Map<String, Object> submitBody = new HashMap<>();
                submitBody.put("doc", data);
                submitBody.put("doctype", "Material Request");
                submitBody.put("name", materialRequestId);
                submitBody.put("action", "Submit");

                erpnextApiService.post(
                        "/api/method/frappe.client.submit",
                        submitBody,
                        apiKey,
                        apiSecret);

                // Stocker l'ID de la demande de matériel
                String ref = materialRequest.get("ref");
                if (ref != null && !ref.isEmpty()) {
                    materialRequestIds.put(ref, materialRequestId);
                }
            } catch (Exception e) {
                // Gérer l'erreur
                throw new RuntimeException("Erreur lors de la création de la demande de matériel : " + e.getMessage());
            }
        }

        return materialRequestIds;
    }

    /**
     * Vérifie si un entrepôt existe, sinon le crée.
     */
    private String verifierOuCreerEntrepot(String warehouseName, String apiKey, String apiSecret) {
        try {
            // Vérifier si l'entrepôt existe
            ResponseEntity<Map> response = erpnextApiService.get(
                    "/api/resource/Warehouse?filters=[[\"warehouse_name\",\"=\",\"" + warehouseName + "\"]]",
                    apiKey,
                    apiSecret);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");

            if (data != null && !data.isEmpty()) {
                // L'entrepôt existe déjà
                return (String) data.get(0).get("name");
            } else {
                // Créer un nouvel entrepôt
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("doctype", "Warehouse");
                requestBody.put("warehouse_name", warehouseName);
                requestBody.put("parent_warehouse", "All Warehouses - RD"); // Ou un autre entrepôt parent par défaut

                ResponseEntity<Map> createResponse = erpnextApiService.post(
                        "/api/resource/Warehouse",
                        requestBody,
                        apiKey,
                        apiSecret);

                @SuppressWarnings("unchecked")
                Map<String, Object> createResponseBody = createResponse.getBody();

                @SuppressWarnings("unchecked")
                Map<String, Object> createdWarehouse = (Map<String, Object>) createResponseBody.get("data");

                return (String) createdWarehouse.get("name");
            }
        } catch (Exception e) {
            // Gérer l'erreur
            throw new RuntimeException("Erreur lors de la vérification ou création de l'entrepôt " + warehouseName + ": " + e.getMessage());
        }
    }
    /**
     * Vérifie si un article existe, sinon le crée.
     */
    private String verifierOuCreerItem(String itemName, String itemGroup, String apiKey, String apiSecret) {
        try {
            // Vérifier si l'article existe
            ResponseEntity<Map> response = erpnextApiService.get(
                    "/api/resource/Item?filters=[[\"item_name\",\"=\",\"" + itemName + "\"]]",
                    apiKey,
                    apiSecret);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            
            if (data != null && !data.isEmpty()) {
                // L'article existe déjà
                return (String) data.get(0).get("name");
            } else {
                // Vérifier si le groupe d'articles existe
                String itemGroupId = verifierOuCreerItemGroup(itemGroup, apiKey, apiSecret);
                
                // Créer un nouvel article
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("doctype", "Item");
                requestBody.put("item_name", itemName);
                requestBody.put("item_code", generateItemCode(itemName));
                requestBody.put("item_group", itemGroupId);
                requestBody.put("is_stock_item", 1);
                requestBody.put("stock_uom", "Nos");  // Unité de mesure par défaut
                requestBody.put("standard_rate", 100); // Prix standard fictif
                
                ResponseEntity<Map> createResponse = erpnextApiService.post(
                        "/api/resource/Item",
                        requestBody,
                        apiKey,
                        apiSecret);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> createResponseBody = createResponse.getBody();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> createdItem = (Map<String, Object>) createResponseBody.get("data");
                
                return (String) createdItem.get("name");
            }
        } catch (Exception e) {
            // Gérer l'erreur
            throw new RuntimeException("Erreur lors de la vérification ou création de l'article " + itemName + ": " + e.getMessage());
        }
    }
    
    /**
     * Vérifie si un groupe d'articles existe, sinon le crée.
     */
    private String verifierOuCreerItemGroup(String itemGroup, String apiKey, String apiSecret) {
        if (itemGroup == null || itemGroup.isEmpty()) {
            // Utiliser un groupe d'articles par défaut
            return "All Item Groups";
        }
        
        try {
            // Vérifier si le groupe d'articles existe
            ResponseEntity<Map> response = erpnextApiService.get(
                    "/api/resource/Item Group?filters=[[\"name\",\"=\",\"" + itemGroup + "\"]]",
                    apiKey,
                    apiSecret);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            
            if (data != null && !data.isEmpty()) {
                // Le groupe d'articles existe déjà
                return (String) data.get(0).get("name");
            } else {
                // Créer un nouveau groupe d'articles
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("doctype", "Item Group");
                requestBody.put("item_group_name", itemGroup);
                requestBody.put("parent_item_group", "All Item Groups");
                
                ResponseEntity<Map> createResponse = erpnextApiService.post(
                        "/api/resource/Item Group",
                        requestBody,
                        apiKey,
                        apiSecret);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> createResponseBody = createResponse.getBody();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> createdItemGroup = (Map<String, Object>) createResponseBody.get("data");
                
                return (String) createdItemGroup.get("name");
            }
        } catch (Exception e) {
            // Gérer l'erreur
            throw new RuntimeException("Erreur lors de la vérification ou création du groupe d'articles " + itemGroup + ": " + e.getMessage());
        }
    }
    
    /**
     * Génère un code d'article unique.
     */
    private String generateItemCode(String itemName) {
        // Générer un code basé sur le nom de l'article et la date actuelle
        String prefix = itemName.substring(0, Math.min(3, itemName.length())).toUpperCase();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        return prefix + "-" + date + "-" + System.currentTimeMillis() % 1000;
    }
    
    /**
     * Traite le fichier Excel des demandes de devis.
     */
    private List<Map<String, String>> traiterFichierQuotations(MultipartFile file) throws IOException {
        List<Map<String, String>> quotations = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            // Ignorer l'en-tête
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Map<String, String> quotation = new HashMap<>();

                if (row.length >= 2) {
                    quotation.put("ref_request_quotation", row[0]);
                    quotation.put("supplier", row[1]);
                    quotations.add(quotation);
                }
            }
        } catch (CsvException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV des demandes de devis", e);
        }

        return quotations;
    }


    /**
     * Crée les demandes de devis dans ERPNext.
     */
    private void creerRequestForQuotations(
            List<Map<String, String>> quotations,
            Map<String, String> materialRequestIds,
            Map<String, String> supplierIds,
            String apiKey,
            String apiSecret) {
        
        for (Map<String, String> quotation : quotations) {
            try {
                String materialRequestRef = quotation.get("ref_request_quotation");
                String supplierName = quotation.get("supplier");
                
                String materialRequestId = materialRequestIds.get(materialRequestRef);
                String supplierId = supplierIds.get(supplierName);
                
                if (materialRequestId == null || supplierId == null) {
                    // Référence invalide, passer à la suivante
                    continue;
                }
                
                // Récupérer les détails de la demande de matériel
                ResponseEntity<Map> mrResponse = erpnextApiService.get(
                        "/api/resource/Material Request/" + materialRequestId,
                        apiKey,
                        apiSecret);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> mrResponseBody = mrResponse.getBody();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> mrData = (Map<String, Object>) mrResponseBody.get("data");
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> mrItems = (List<Map<String, Object>>) mrData.get("items");
                
                // Créer la demande de devis
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("doctype", "Request for Quotation");
                requestBody.put("transaction_date", LocalDate.now().toString());
                
                // Ajouter le fournisseur
                List<Map<String, Object>> suppliers = new ArrayList<>();
                Map<String, Object> supplierEntry = new HashMap<>();
                supplierEntry.put("supplier", supplierId);
                suppliers.add(supplierEntry);
                requestBody.put("suppliers", suppliers);
                
                // Ajouter les articles
                List<Map<String, Object>> items = new ArrayList<>();
                for (Map<String, Object> mrItem : mrItems) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("item_code", mrItem.get("item_code"));
                    item.put("qty", mrItem.get("qty"));
                    item.put("material_request", materialRequestId);
                    item.put("material_request_item", mrItem.get("name"));
                    
                    items.add(item);
                }
                requestBody.put("items", items);
                
                // Créer la demande de devis
                ResponseEntity<Map> rfqResponse = erpnextApiService.post(
                        "/api/resource/Request for Quotation",
                        requestBody,
                        apiKey,
                        apiSecret);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> rfqResponseBody = rfqResponse.getBody();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> rfqData = (Map<String, Object>) rfqResponseBody.get("data");
                
                String rfqId = (String) rfqData.get("name");
                
                // Soumettre la demande de devis
                Map<String, Object> submitBody = new HashMap<>();
                submitBody.put("doctype", "Request for Quotation");
                submitBody.put("name", rfqId);
                submitBody.put("action", "Submit");
                
                erpnextApiService.post(
                        "/api/method/frappe.client.submit",
                        submitBody,
                        apiKey,
                        apiSecret);
                
                // Créer le devis fournisseur
                creerSupplierQuotation(rfqId, supplierId, items, apiKey, apiSecret);
            } catch (Exception e) {
                // Gérer l'erreur
                throw new RuntimeException("Erreur lors de la création de la demande de devis : " + e.getMessage());
            }
        }
    }
    
    /**
     * Crée un devis fournisseur à partir d'une demande de devis.
     */
    private void creerSupplierQuotation(
            String rfqId,
            String supplierId,
            List<Map<String, Object>> items,
            String apiKey,
            String apiSecret) {
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("doctype", "Supplier Quotation");
            requestBody.put("supplier", supplierId);
            requestBody.put("transaction_date", LocalDate.now().toString());
            
            // Ajouter les articles
            List<Map<String, Object>> sqItems = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Map<String, Object> sqItem = new HashMap<>();
                sqItem.put("item_code", item.get("item_code"));
                sqItem.put("qty", item.get("qty"));
                
                // Ajouter un prix fictif
                sqItem.put("rate", 100);
                
                sqItems.add(sqItem);
            }
            requestBody.put("items", sqItems);
            
            // Créer le devis fournisseur
            erpnextApiService.post(
                    "/api/resource/Supplier Quotation",
                    requestBody,
                    apiKey,
                    apiSecret);
            
            // Le devis fournisseur reste en état brouillon (draft)
        } catch (Exception e) {
            // Gérer l'erreur
            throw new RuntimeException("Erreur lors de la création du devis fournisseur : " + e.getMessage());
        }
    }
    
    /**
     * Récupère la valeur d'une cellule sous forme de chaîne.
     */

}