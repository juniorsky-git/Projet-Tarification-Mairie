package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;

/**
 * Traabilit des donnes : identifie les lignes et colonnes prcises.
 */
public class VerificateurValeurs {

    private static final String FICHIER = "Donnees/Autres/CALC DEP.xlsx";

    public static void main(String[] args) {
        System.out.println("=== TRACABILITE DES DONNEES (CALC DEP.xlsx) ===\n");

        if (!new File(FICHIER).exists()) {
            System.err.println("Fichier non trouve : " + FICHIER);
            return;
        }

        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            // --- 1. DASHBOARD SCOLAIRE (Simulation) ---
            System.out.println("--- 1. SCOLAIRE (Effectifs et Recettes) ---");
            System.out.println("Source : Onglet 'Simulation' (index 8)");
            Sheet sSim = wb.getSheetAt(8);
            for (int i = 7; i <= 15; i++) { // Tranches A  EXT
                Row row = sSim.getRow(i);
                if (row == null) {
                    continue;
                }
                String tranche = getCellString(row.getCell(1)); // Col B
                if (tranche.isEmpty()) {
                    tranche = getCellString(row.getCell(0)); // Col A pour EXT
                }
                
                double nbEnfants = getCellDouble(row.getCell(3)); // Col D
                double prixReel = getCellDouble(row.getCell(2));  // Col C
                
                System.out.printf("   Ligne %d | Tranche: %-4s | Col D (Effectif): %6.0f | Col C (Prix): %5.2f%n", 
                                  (i+1), tranche, nbEnfants, prixReel);
            }

            // --- 2. DASHBOARD SCOLAIRE (Depenses Reelles) ---
            System.out.println("\n--- 2. SCOLAIRE (Depenses Reelles RESTMICH) ---");
            System.out.println("Source : Onglet 'Depenses restau 2025' (index 0)");
            Sheet sDep = wb.getSheetAt(0);
            double totalScol = 0;
            int countScol = 0;
            for (int i = 1; i <= sDep.getLastRowNum(); i++) {
                Row row = sDep.getRow(i);
                if (row == null) {
                    continue;
                }
                String antenna = getCellString(row.getCell(19)); // Col T
                String service = getCellString(row.getCell(18)); // Col S
                String libelle = getCellString(row.getCell(3)).toUpperCase();
                
                if (antenna.equalsIgnoreCase("RESTMICH") || service.contains("2-RE")) {
                   // Exclusions (ados, loisirs)
                   if (!libelle.contains("ADOS") && !libelle.contains("LOISIRS") && !libelle.contains("COMMUNAL")) {
                       double montant = Math.abs(getCellDouble(row.getCell(7))); // Col H
                       totalScol += montant;
                       countScol++;
                       if (countScol < 5) { // Afficher les premieres lignes
                           System.out.printf("   Ligne %d | Col H (TTC): %8.2f | Libelle: %s%n", (i+1), montant, libelle);
                       }
                   }
                }
            }
            System.out.printf("   ... (%d lignes trouvees au total) ...%n", countScol);
            System.out.printf("   TOTAL SCOLAIRE CONSTATE : %.2f%n", totalScol);

            // --- 3. DASHBOARD LOISIRS (Depenses Simulation) ---
            System.out.println("\n--- 3. LOISIRS (Depenses par Segment) ---");
            System.out.println("Source : Onglet 'Simulation' (index 8), Colonne R");
            String[] segments = {"MDJ", "CLGAV", "CLJP1", "CLLMICH", "P'TIT PRINCE"};
            double totalLoi = 0;
            for (int i = 1; i <= sSim.getLastRowNum(); i++) {
                Row row = sSim.getRow(i);
                if (row == null) {
                    continue;
                }
                String text = getRowText(row).toUpperCase();
                
                String matchingSegment = null;
                for (String seg : segments) {
                    if (text.contains(seg)) {
                        matchingSegment = seg;
                        break;
                    }
                }
                
                if (matchingSegment != null) {
                    double montant = Math.abs(getCellDouble(row.getCell(17))); // Col R
                    if (montant > 0) {
                        totalLoi += montant;
                        System.out.printf("   Ligne %d | Segment: %-12s | Col R (Montant): %9.2f%n", 
                                          (i+1), matchingSegment, montant);
                    }
                }
            }
            System.out.printf("   TOTAL LOISIRS CONSTATE : %.2f%n", totalLoi);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCellString(Cell c) {
        if (c == null) {
            return "";
        }
        return c.toString().trim();
    }

    private static double getCellDouble(Cell c) {
        if (c == null) {
            return 0;
        }
        try {
            if (c.getCellType() == CellType.NUMERIC) {
                return c.getNumericCellValue();
            }
            if (c.getCellType() == CellType.FORMULA) {
                return c.getNumericCellValue();
            }
            String s = c.toString().replace(",", ".");
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getRowText(Row row) {
        StringBuilder sb = new StringBuilder();
        for (Cell c : row) {
            sb.append(getCellString(c)).append(" ");
        }
        return sb.toString();
    }
}
