package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DiagnosticEauSynthese : Génère un tableau synthétique des consommations d'eau.
 * Destiné au maître de stage pour traiter la demande de répartition par site (Europe, etc.).
 */
public class DiagnosticEauSynthese {

    private static final String FICHIER = "Donnees/Autres/CALC DEP(4).xlsx";

    public static void main(String[] args) {
        System.out.println("====================================================================================================");
        System.out.println("   RAPPORT SYNTHÉTIQUE : CONSOMMATIONS D'EAU PAR SITE (BP 2025)                                     ");
        System.out.println("====================================================================================================\n");

        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Conso eau");
            if (s == null) {
                System.err.println("ERREUR : L'onglet 'Conso eau' est introuvable.");
                return;
            }

            // En-tête du tableau synthétique
            System.out.printf("%-45s | %-12s | %-12s | %-15s | %-12s%n", 
                              "SITE / AFFECTATION", "PERIODE", "CONSO (m3)", "MONTANT TTC", "N° FACTURE");
            System.out.println("----------------------------------------------------------------------------------------------------");

            // Lecture à partir de la ligne 6 (index 5)
            for (int i = 5; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String site = getStr(row.getCell(2)); // Affectation
                if (site.isEmpty() || site.equalsIgnoreCase("null")) continue;

                // --- 1ER SEMESTRE ---
                double m3_1  = getVal(row.getCell(7));
                double eur_1 = getVal(row.getCell(8));
                
                // --- 2ND SEMESTRE ---
                double m3_2  = getVal(row.getCell(17));
                double eur_2 = getVal(row.getCell(18));
                String date_2 = getStr(row.getCell(20)); // Estimation Date
                String bill_2 = getStr(row.getCell(19)); // Estimation N° Facture

                // Affichage S1 s'il y a du montant
                if (eur_1 > 0 || m3_1 > 0) {
                    System.out.printf("%-45s | %-12s | %10.1f | %10.2f € | %-15s%n", 
                                      tronquer(site, 45), "S1 2025", m3_1, eur_1, "-");
                }

                // Affichage S2 s'il y a du montant
                if (eur_2 > 0 || m3_2 > 0) {
                    System.out.printf("%-45s | %-12s | %10.1f | %10.2f € | %-15s%n", 
                                      tronquer(site, 45), "S2 2025", m3_2, eur_2, bill_2);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur technique : " + e.getMessage());
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
