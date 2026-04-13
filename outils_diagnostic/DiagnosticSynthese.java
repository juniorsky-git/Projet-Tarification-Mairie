package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.Iterator;

/**
 * Diagnostic exhaustif de l'onglet 'syntheses charges' de CALC DEP (3).xlsx.
 * Extrait chaque donnee une par une pour validation.
 */
public class DiagnosticSynthese {

    private static final String FILE = "Donnees/Autres/CALC DEP (3).xlsx";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   DIAGNOSTIC EXHAUSTIF : CALC DEP (3) SYNTHESE  ");
        System.out.println("=================================================\n");

        try (FileInputStream fis = new FileInputStream(FILE);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("syntheses charges");
            if (s == null) {
                System.err.println("[ERREUR] Onglet 'syntheses charges' introuvable.");
                return;
            }

            System.out.println("--- 1. TABLEAU DES DEPENSES PAR POLE ---");
            System.out.printf("%-20s | %-12s | %-12s | %-12s | %-12s%n", 
                "Nature", "Restauration", "Loisirs", "Ados", "Etudes");
            System.out.println("--------------------------------------------------------------------------------");

            // Lecture des lignes 4 a 21 (Dépenses)
            for (int i = 3; i <= 20; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String nature = getCellString(row.getCell(1));
                if (nature.isEmpty()) nature = getCellString(row.getCell(0)); // Fallback
                
                double restau = getCellDouble(row.getCell(3));
                double loisirs = getCellDouble(row.getCell(4));
                double ados = getCellDouble(row.getCell(7));
                double etudes = getCellDouble(row.getCell(6));

                if (!nature.isEmpty() && (restau > 0 || loisirs > 0 || ados > 0 || etudes > 0)) {
                    System.out.printf("%-20s | %12.2f | %12.2f | %12.2f | %12.2f%n", 
                        nature, restau, loisirs, ados, etudes);
                }
            }

            System.out.println("\n--- 2. TABLEAU DES RECETTES & EFFECTIFS ---");
            System.out.printf("%-10s | %-5s | %-12s | %-12s | %-12s | %-12s%n", 
                "Tranche", "Cat", "Nb Enfants", "Px Restau", "Px Loisirs", "Px Etudes");
            System.out.println("--------------------------------------------------------------------------------");

            // Lecture des lignes 30 a 39 (Recettes)
            for (int i = 29; i <= 38; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String tranche = getCellString(row.getCell(0));
                String cat = getCellString(row.getCell(1));
                double nbEnfants = getCellDouble(row.getCell(2));
                double pxRestau = getCellDouble(row.getCell(3));
                double pxLoisirs = getCellDouble(row.getCell(4));
                double pxEtudes = getCellDouble(row.getCell(6));

                System.out.printf("%-10s | %-5s | %12.0f | %12.2f | %12.2f | %12.2f%n", 
                    tranche, cat, nbEnfants, pxRestau, pxLoisirs, pxEtudes);
            }

            System.out.println("\n--- 3. VERIFICATION DES TOTAUX ---");
            Row totalRow = s.getRow(21); // Ligne 22
            if (totalRow != null) {
                System.out.printf("Total Restauration : %.2f EUR%n", getCellDouble(totalRow.getCell(3)));
                System.out.printf("Total Loisirs      : %.2f EUR%n", getCellDouble(totalRow.getCell(4)));
                System.out.printf("Total Espace Ados  : %.2f EUR%n", getCellDouble(totalRow.getCell(7)));
                System.out.printf("Total Etudes       : %.2f EUR%n", getCellDouble(totalRow.getCell(6)));
            }

        } catch (Exception e) {
            System.err.println("Erreur diagnostic : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getCellString(Cell c) {
        if (c == null) return "";
        DataFormatter df = new DataFormatter();
        return df.formatCellValue(c).trim();
    }

    private static double getCellDouble(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            if (c.getCellType() == CellType.FORMULA) {
                CellValue cv = c.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator().evaluate(c);
                return cv.getNumberValue();
            }
            String s = c.toString().replace(",", ".").replaceAll("[^0-9.]", "");
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
