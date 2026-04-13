package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Outil de diagnostic precis pour le pole Sejours (Vacances).
 * Extrait les depenses detaillees des lignes 92 a 95 de l'onglet Simulation.
 */
public class DiagnosticSejours {

    private static final String FICHIER = "Donnees/Autres/CALC DEP.xlsx";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   DIAGNOSTIC TECHNIQUE : DEPENSES SEJOURS     ");
        System.out.println("=================================================\n");

        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                System.err.println("Onglet 'Simulation' introuvable !");
                return;
            }

            // En-têtes à la ligne 91 (index 90)
            Row rowHeaders = s.getRow(90);
            if (rowHeaders == null) {
                System.err.println("En-tetes Sejours introuvables.");
                return;
            }

            // Pour calculer les resultats des formules [3]Feuil1 ou [5]Feuil1
            // Note: DataFormatter recupere la valeur mise en cache si l'evaluator echoue
            DataFormatter formatter = new DataFormatter();

            // On parcourt les lignes de donnees (92 a 95 -> index 91 a 94)
            for (int i = 91; i <= 94; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                // Le nom du sejour est en colonne B (index 1)
                String nomSejour = formatter.formatCellValue(row.getCell(1));
                if (nomSejour == null || nomSejour.trim().isEmpty() || nomSejour.equals("0")) {
                     continue; // On saute les lignes vides
                }

                System.out.println("\n>>> SEJOUR : " + nomSejour.toUpperCase());
                System.out.println("---------------------------------------------------------------");
                System.out.printf("%-35s | %-15s%n", "POSTE DE DEPENSE", "MONTANT");
                System.out.println("---------------------------------------------------------------");

                // Les donnees commencent a la colonne C (index 2) jusqu'a K (index 10)
                for (int j = 2; j <= 10; j++) {
                    String header = formatter.formatCellValue(rowHeaders.getCell(j));
                    String val = formatter.formatCellValue(row.getCell(j));
                    
                    if (val != null && !val.isEmpty() && !val.equals("0") && !val.equals("0,00")) {
                        System.out.printf("%-35s | %15s %n", header, val);
                    }
                }
                
                // Le TOTAL de la ligne est en colonne L (index 11)
                String totalRaw = formatter.formatCellValue(row.getCell(11));
                if (!totalRaw.isEmpty() && !totalRaw.equals("0")) {
                    System.out.println("---------------------------------------------------------------");
                    System.out.printf("%-35s | %15s %n", "TOTAL DU SEJOUR", totalRaw);
                    System.out.println("---------------------------------------------------------------");
                }
            }

            // Total General a la ligne 96 (index 95)
            Row rowGlobal = s.getRow(95);
            if (rowGlobal != null) {
                String totalGlobal = formatter.formatCellValue(rowGlobal.getCell(11));
                System.out.println("\n=================================================");
                System.out.println("   TOTAL GENERAL SEJOURS : " + totalGlobal);
                System.out.println("=================================================");
            }

        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
