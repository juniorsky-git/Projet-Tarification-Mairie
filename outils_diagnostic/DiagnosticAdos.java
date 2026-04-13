package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Outil de diagnostic precis pour le pole Espace Ados.
 * Extrait toutes les depenses detaillees de la ligne 75 de l'onglet Simulation.
 */
public class DiagnosticAdos {

    private static final String FICHIER = "Donnees/Autres/CALC DEP.xlsx";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   DIAGNOSTIC TECHNIQUE : DEPENSES ESPACE ADOS   ");
        System.out.println("=================================================\n");

        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                System.err.println("Onglet 'Simulation' introuvable !");
                return;
            }

            // Les titres sont sur la ligne 74 (index 73)
            // Les valeurs sont sur la ligne 75 (index 74)
            Row rowHeaders = s.getRow(73);
            Row rowValues = s.getRow(74);

            if (rowHeaders == null || rowValues == null) {
                System.err.println("Le bloc Espace Ados est introuvable a la ligne 75.");
                return;
            }

            // Pour calculer le resultat des formules
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter();

            System.out.printf("%-35s | %-15s%n", "NATURE DE LA DEPENSE (Ligne 74)", "MONTANT CALCULE");
            System.out.println("-------------------------------------------------------------------");

            // On parcourt dynamiquement les colonnes a partir de l'index 2 (Charges)
            int nbCols = Math.min(rowHeaders.getLastCellNum(), rowValues.getLastCellNum());
            for (int j = 2; j < nbCols; j++) {
                Cell cHeader = rowHeaders.getCell(j);
                Cell cValue = rowValues.getCell(j);
                
                String label = (cHeader == null) ? "Inconnu" : cHeader.toString().replace("\n", " ").trim();
                
                // On tente d'evaluer la formule pour avoir le chiffre final
                String val;
                try {
                    val = formatter.formatCellValue(cValue, evaluator);
                } catch (Exception e) {
                    val = formatter.formatCellValue(cValue); // Fallback si lien externe casse
                }

                if (!label.isEmpty() && !val.isEmpty() && !val.equals("0") && !val.equals("0,00")) {
                    System.out.printf("%-35s | %15s %n", label, val);
                }
            }

            System.out.println("\n-------------------------------------------------");
            System.out.println("Source : Simulation - Ligne 75");

        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
