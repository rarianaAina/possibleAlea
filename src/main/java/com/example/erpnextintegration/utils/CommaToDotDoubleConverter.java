package com.example.erpnextintegration.utils;

import com.opencsv.bean.AbstractBeanField;

public class CommaToDotDoubleConverter extends AbstractBeanField<Double, String> {
    @Override
    protected Double convert(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // Remplacer la virgule par un point et retirer les guillemets
            String cleanedValue = value.replace(",", ".").replace("\"", "").trim();
            return Double.parseDouble(cleanedValue);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Valeur invalide pour salaire_base : " + value);
        }
    }
}