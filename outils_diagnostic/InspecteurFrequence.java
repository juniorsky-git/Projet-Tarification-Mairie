package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic pour analyser les fréquences de facturation.
 * Utilisé pour identifier la structure répétitive des factures mensuelles Scolarest.
 */
public class InspecteurFrequence {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/CALC DEP.xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            System.out.println("ANALYSE DE LA FREQUENCE DES LIGNES COMPTABLES :");
            
            for (int i = 0; i < 20; i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                
                Cell cLib = r.getCell(3);
                Cell cTiers = r.getCell(9);
                
                String lib = (cLib == null) ? "" : cLib.toString();
                String tiers = (cTiers == null) ? "" : cTiers.toString();
                
                System.out.println("L[" + (i + 1) + "] : [" + tiers + "] " + lib);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
