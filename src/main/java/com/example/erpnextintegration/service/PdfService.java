package com.example.erpnextintegration.service;


import com.example.erpnextintegration.dto.employee.EmployeeDTO;
import com.example.erpnextintegration.dto.payroll.SalaireDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    @Autowired
    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void genererFichePaiePdf(EmployeeDTO employe, SalaireDTO salaire, HttpServletResponse response) throws Exception {
        Context context = new Context(Locale.FRANCE);
        context.setVariable("employe", employe);
        context.setVariable("salaire", salaire);

        // Formatage des dates et montants
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        context.setVariable("dateFormatter", dateFormatter);

        // Nom du mois en français
        String nomMois = salaire.getDatePaiement().getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        context.setVariable("nomMois", nomMois);

        // Processus de création du PDF
        String processedHtml = templateEngine.process("employes/fiche-paie-pdf", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(processedHtml);
        renderer.layout();
        renderer.createPDF(outputStream);

        // Configuration de la réponse HTTP
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=fiche-paie-" +
                employe.getName() + "-" + nomMois + "-" + salaire.getAnnee() + ".pdf");

        // Écriture du PDF dans la réponse
        response.getOutputStream().write(outputStream.toByteArray());
    }
}