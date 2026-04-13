package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import fr.mairie.tarification.LogService;
import java.io.FileInputStream;
import java.io.File;

/**
 * Exemple demand par l'utilisateur pour comprendre comment parcourir 
 * l'onglet "Conso d'eau" et compter les colonnes dynamiquement.
 */
public class ExempleConsoEau {

    private static final String FICHIER = "Donnees/Autres/CALC DEP.xlsx";

    public static void main(String[] args) {
        System.out.println("=== EXEMPLE D'EXTRACTION : CONSO D'EAU ===\n");

        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            // 1. Recherche de l'onglet par son nom exact (trouve : 'Conso eau')
            Sheet sheet = wb.getSheet("Conso eau");
            
            if (sheet == null) {
                System.out.println("[INFO] L'onglet 'Conso d'eau' n'existe pas dans ce fichier.");
                System.out.println("Liste des onglets disponibles :");
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    System.out.println(" - " + wb.getSheetAt(i).getSheetName());
                }
                return;
            }

            // 2. Parcours des lignes
            System.out.println("Analyse de l'onglet '" + sheet.getSheetName() + "' :");
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                // 3. Recuperation dynamique du nombre de colonnes de la ligne
                // getLastCellNum() renvoie l'index de la derniere cellule + 1
                int nbColonnes = row.getLastCellNum();
                
                System.out.println("Ligne " + (i + 1) + " : " + nbColonnes + " colonnes detectees.");

                // Exemple : Lecture de la derniere colonne de cette ligne
                if (nbColonnes > 0) {
                    Cell lastCell = row.getCell(nbColonnes - 1);
                    System.out.println("   -> Valeur derniere cellule : " + lastCell);
                }
            }

        } catch (Exception e) {
            LogService.error("Erreur lors de l'exemple Conso d'eau", e);
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
