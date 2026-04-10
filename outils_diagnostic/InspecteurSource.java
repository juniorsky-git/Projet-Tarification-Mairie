package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic pour inspecter les premières lignes d'un fichier Excel.
 * Utilisé pour identifier les index de colonnes et la structure des données réelles.
 */
public class InspecteurSource {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/CALC DEP.xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            System.out.println("Structure du fichier : " + fichier);

            // On inspecte les 10 premières lignes
            for (int i = 0; i < 10; i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                System.out.print("Ligne " + (i + 1) + " : ");
                for (int j = 0; j < 25; j++) {
                    Cell c = r.getCell(j);
                    String val = (c == null) ? "NULL" : c.toString();
                    System.out.print("[" + j + "]" + val + " | ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
