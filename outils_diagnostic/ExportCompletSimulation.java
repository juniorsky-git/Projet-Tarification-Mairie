package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.File;

/**
 * Outil d'exportation brute de l'intégralité de l'onglet Simulation.
 * Permet de sauvegarder tous les tarifs et effectifs dans un fichier texte.
 */
public class ExportCompletSimulation {

    private static final String FICHIER_EXCEL = "Donnees/Autres/CALC DEP.xlsx";
    private static final String FICHIER_EXPORT = "Documents_Gestion/export_complet_simulation.txt";

    public static void main(String[] args) {
        System.out.println("=== EXPORTATION BRUTE : SIMULATION ===");
        System.out.println("Cible : " + FICHIER_EXPORT);

        try (FileInputStream fis = new FileInputStream(FICHIER_EXCEL);
             Workbook wb = WorkbookFactory.create(fis);
             PrintWriter writer = new PrintWriter(FICHIER_EXPORT)) {

            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                System.err.println("Onglet 'Simulation' introuvable !");
                return;
            }

            DataFormatter formatter = new DataFormatter();
            int nbLignes = 0;
            for (int i = 0; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    writer.println();
                    continue;
                }

                int nbCols = row.getLastCellNum();
                for (int j = 0; j < nbCols; j++) {
                    Cell cell = row.getCell(j);
                    // On utilise DataFormatter pour avoir le resultat CALCULE et FORMATE de la cellule
                    String val = formatter.formatCellValue(cell).replace("\n", " ").trim();
                    writer.print(val + "\t");
                }
                writer.println();
                nbLignes++;
            }

            System.out.println("\nSUCCESS : " + nbLignes + " lignes exportees vers " + FICHIER_EXPORT);

        } catch (Exception e) {
            System.err.println("Erreur d'export : " + e.getMessage());
        }
    }
}
