package com.example.erpnextintegration.service.imports;

import org.springframework.stereotype.Service;

import com.example.erpnextintegration.dto.imports.RapportErreur;

@Service
public class RapportErreurService {
    public RapportErreur createError(int ligne, String raison, String valeur) {
        RapportErreur rapportErreur = new RapportErreur();
        rapportErreur.setFichier("file 2");
        rapportErreur.setLigne(ligne + "");
        rapportErreur.setRaison(raison);
        rapportErreur.setValeur(valeur);
        return rapportErreur;
    }
}
