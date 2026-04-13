package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.File;

/**
 * Outil d'exportation brute de l'intégralité du tableau de consommation d'eau.
 * Génère un fichier texte Tabulé (.txt) pour conserver la structure.
 */
public class ExportCompletEau {

    private static final String FICHIER_EXCEL = "Donnees/Autres/CALC DEP.xlsx";
    private static final String FICHIER_EXPORT = "Documents_Gestion/export_complet_eau.txt";

    public static void main(String[] args) {
        System.out.println("=== EXPORTATION BRUTE : CONSO EAU ===");
        System.out.println("Cible : " + FICHIER_EXPORT);

        try (FileInputStream fis = new FileInputStream(FICHIER_EXCEL);
             Workbook wb = WorkbookFactory.create(fis);
             PrintWriter writer = new PrintWriter(FICHIER_EXPORT)) {

            Sheet s = wb.getSheet("Conso eau");
            if (s == null) {
                System.err.println("Onglet 'Conso eau' introuvable !");
                return;
            }

            int nbLignes = 0;
            for (int i = 0; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    writer.println(); // Ligne vide
                    continue;
                }

                // On parcourt dynamiquement TOUTES les colonnes de la ligne
                int nbCols = row.getLastCellNum();
                for (int j = 0; j < nbCols; j++) {
                    Cell cell = row.getCell(j);
                    String val = (cell == null) ? "" : cell.toString().replace("\n", " ").trim();
                    writer.print(val + "\t"); // Separation par tabulation
                }
                writer.println(); // Fin de ligne
                nbLignes++;
            }

            System.out.println("\nSUCCESS : " + nbLignes + " lignes exportees vers " + FICHIER_EXPORT);
            System.out.println("Vous pouvez ouvrir ce fichier avec le Bloc-notes ou Excel.");

        } catch (Exception e) {
            System.err.println("Erreur d'export : " + e.getMessage());
        }
    }
}
