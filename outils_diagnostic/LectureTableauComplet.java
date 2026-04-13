package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;

/**
 * Extracteur bi-semestriel complet pour l'onglet 'Conso eau'.
 * Affiche les consommations (m3), les montants (EUR) et les dates pour toute l'annee.
 */
public class LectureTableauComplet {

    private static final String FICHIER = "Donnees/Autres/CALC DEP.xlsx";

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("   EXTRACTION ANNUELLE (1ER ET 2ND SEMESTRE) : CONSOMMATION D'EAU                        ");
        System.out.println("==========================================================================================\n");

        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Conso eau");
            if (s == null) {
                System.err.println("Onglet 'Conso eau' introuvable !");
                return;
            }

            // Entete du tableau console
            System.out.printf("%-12s | %-15s | %-8s | %-8s | %-8s | %-8s | %-10s%n", 
                              "REF PDL", "AFFECTATION", "m3 (1)", "EUR (1)", "m3 (2)", "EUR (2)", "TOTAL AN");
            System.out.println("------------------------------------------------------------------------------------------");

            // On commence apres les entetes (Ligne 6 -> index 5)
            for (int i = 5; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    continue;
                }

                // Identification PDL
                String ref      = getStr(row.getCell(0)); // REFERENCE PDL
                String affect   = getStr(row.getCell(2)); // AFFECTATION

                // 1er Semestre 2025
                double m3_1     = getVal(row.getCell(7)); // Consos m3
                double eur_1    = getVal(row.getCell(8)); // Montant TTC

                // 2nd Semestre 2025
                double m3_2     = getVal(row.getCell(17)); // Consos m3
                double eur_2    = getVal(row.getCell(18)); // Montant TTC

                // Calculs Annuel
                double totalEur = eur_1 + eur_2;

                // Affichage si la ligne n'est pas vide
                if (!ref.isEmpty() && !ref.equalsIgnoreCase("null")) {
                    System.out.printf("%-12s | %-15s | %8.1f | %8.2f | %8.1f | %8.2f | %10.2f%n", 
                                      ref, affect, m3_1, eur_1, m3_2, eur_2, totalEur);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur technique : " + e.getMessage());
        }
    }

    private static String getStr(Cell c) {
        if (c == null) return "";
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
