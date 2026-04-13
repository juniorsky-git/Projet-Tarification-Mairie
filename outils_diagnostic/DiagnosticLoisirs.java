package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Diagnostic technique pour le pole Loisirs dans CALC DEP (1).xlsx.
 * Verifie les montants des lignes 42 a 46 colonne O (14).
 */
public class DiagnosticLoisirs {

    private static final String FILE = "Donnees/Autres/CALC DEP (1).xlsx";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   DIAGNOSTIC TECHNIQUE : ACCUEIL DE LOISIRS   ");
        System.out.println("=================================================\n");

        try (FileInputStream fis = new FileInputStream(FILE);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                System.err.println("[ERREUR] Onglet 'Simulation' introuvable.");
                return;
            }

            double totalGlobalRecap = 0;

            // Parcours des lignes 42 a 46 (index 41 a 45)
            for (int i = 41; i <= 45; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String nom = row.getCell(1) != null ? row.getCell(1).toString() : "Inconnu";
                Cell cellTotal = row.getCell(14); // Colonne O
                
                double montant = 0;
                if (cellTotal != null) {
                    if (cellTotal.getCellType() == CellType.NUMERIC) {
                        montant = cellTotal.getNumericCellValue();
                    } else {
                        try {
                            montant = Double.parseDouble(cellTotal.toString().replace(",", "."));
                        } catch (Exception e) {}
                    }
                }

                System.out.printf("   %-20s : %12.2f EUR%n", nom, montant);
                totalGlobalRecap += montant;
            }

            System.out.println("\n-------------------------------------------------");
            System.out.printf("   TOTAL CALCULE        : %12.2f EUR%n", totalGlobalRecap);
            
            // Verification de la ligne Total General (Ligne 47)
            Row totalRow = s.getRow(46);
            if (totalRow != null && totalRow.getCell(14) != null) {
                double totalSheet = totalRow.getCell(14).getNumericCellValue();
                System.out.printf("   TOTAL FEUILLE (L47)  : %12.2f EUR%n", totalSheet);
            }
            System.out.println("-------------------------------------------------");

        } catch (Exception e) {
            System.err.println("Erreur diagnostic : " + e.getMessage());
        }
    }
}
