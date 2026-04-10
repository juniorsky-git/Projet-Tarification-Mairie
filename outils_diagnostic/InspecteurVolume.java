package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic ciblé sur la feuille des effectifs (Dataviz).
 * Utilisé pour valider le calcul des tranches "Scolaire" et "Espace Ados".
 */
public class InspecteurVolume {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/Feuille_dataviz .xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            System.out.println("ANALYSE DES VOLUMES PAR TRANCHE :");

            // On inspecte les 10 premières lignes du tableau Dataviz
            for (int i = 0; i < 15; i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                
                Cell cTranche = r.getCell(1); // Col B
                Cell cEnfants = r.getCell(3); // Col D
                
                String tranche = (cTranche == null) ? "VIDE" : cTranche.toString();
                String enfants = (cEnfants == null) ? "0" : cEnfants.toString();
                
                System.out.println("Ligne " + (i + 1) + " : Tranche=[" + tranche + "] | Enfants=[" + enfants + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
