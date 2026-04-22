package fr.mairie.tarification_api.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;

/**
 * DiagnosticEauSynthese : Génère un fichier Excel synthétique des consommations d'eau.
 * Destiné au maître de stage pour traiter la demande de répartition par site.
 */
public class DiagnosticEauSynthese {

    private static final String FICHIER_SOURCE = new File("Donnees/Autres/CALC DEP(4).xlsx").exists() 
        ? "Donnees/Autres/CALC DEP(4).xlsx" 
        : "../Donnees/Autres/CALC DEP(4).xlsx";
        
    private static final String FICHIER_EXPORT = "Synthese_Conso_Eau_2025.xlsx";

    public static void main(String[] args) {
        System.out.println("====================================================================================================");
        System.out.println("   GÉNÉRATION DU FICHIER EXCEL : SYNTHÈSE CONSOMMATIONS D'EAU 2025                                 ");
        System.out.println("====================================================================================================\n");

        try (FileInputStream fis = new FileInputStream(FICHIER_SOURCE);
             Workbook wbSource = WorkbookFactory.create(fis);
             Workbook wbExport = new XSSFWorkbook()) {

            Sheet sSource = wbSource.getSheet("Conso eau");
            if (sSource == null) {
                System.err.println("ERREUR : L'onglet 'Conso eau' est introuvable.");
                return;
            }

            Sheet sExport = wbExport.createSheet("Synthèse Eau 2025");
            
            // Création de l'en-tête Excel
            Row headerRow = sExport.createRow(0);
            String[] headers = {"SITE / AFFECTATION", "PERIODE", "CONSO (m3)", "MONTANT TTC (€)", "N° FACTURE"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // Style gras (optionnel)
            }

            int exportRowNum = 1;

            // Lecture à partir de la ligne 6 (index 5)
            for (int i = 5; i <= sSource.getLastRowNum(); i++) {
                Row row = sSource.getRow(i);
                if (row == null) continue;

                String col1 = getStr(row.getCell(1)); 
                String col2 = getStr(row.getCell(2));
                String col3 = getStr(row.getCell(3));
                String site = (col1 + " " + col2 + " " + col3).trim();

                if (site.isEmpty() || site.contains("null")) continue;

                // --- 1ER SEMESTRE ---
                double m3_1  = getVal(row.getCell(7));
                double eur_1 = getVal(row.getCell(8));
                
                // --- 2ND SEMESTRE ---
                double m3_2  = getVal(row.getCell(17));
                double eur_2 = getVal(row.getCell(18));
                String bill_2 = getStr(row.getCell(19));

                // Ajout S1 s'il y a du montant
                if (eur_1 > 0 || m3_1 > 0) {
                    Row exportRow = sExport.createRow(exportRowNum++);
                    exportRow.createCell(0).setCellValue(site);
                    exportRow.createCell(1).setCellValue("S1 2025");
                    exportRow.createCell(2).setCellValue(m3_1);
                    exportRow.createCell(3).setCellValue(eur_1);
                    exportRow.createCell(4).setCellValue("-");
                }

                // Ajout S2 s'il y a du montant
                if (eur_2 > 0 || m3_2 > 0) {
                    Row exportRow = sExport.createRow(exportRowNum++);
                    exportRow.createCell(0).setCellValue(site);
                    exportRow.createCell(1).setCellValue("S2 2025");
                    exportRow.createCell(2).setCellValue(m3_2);
                    exportRow.createCell(3).setCellValue(eur_2);
                    exportRow.createCell(4).setCellValue(bill_2);
                }
            }

            // Sauvegarde du fichier
            try (FileOutputStream fos = new FileOutputStream(FICHIER_EXPORT)) {
                wbExport.write(fos);
            }

            System.out.println("[SUCCÈS] Le fichier '" + FICHIER_EXPORT + "' a été généré avec succès.");
            System.out.println("Nombre de lignes exportées : " + (exportRowNum - 1));

        } catch (Exception e) {
            System.err.println("Erreur technique : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String tronquer(String s, int n) {
        if (s.length() <= n) return s;
        return s.substring(0, n-3) + "...";
    }

    private static String getStr(Cell c) {
        if (c == null) return "";
        if (c.getCellType() == CellType.NUMERIC) return String.valueOf((long)c.getNumericCellValue());
        return c.toString().trim();
    }

    private static double getVal(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            if (c.getCellType() == CellType.FORMULA) return c.getNumericCellValue();
            String s = c.toString().replace(",", ".");
            return Double.parseDouble(s);
        } catch (Exception e) { return 0; }
    }
}
