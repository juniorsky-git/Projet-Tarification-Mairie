package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic pour inspecter l'intégralité du fichier Dataviz.
 * Utilisé pour identifier la ligne "ESPACE ADOS" qui polluait les calculs.
 */
public class DebugFullVolume {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/Feuille_dataviz .xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            System.out.println("ANALYSE DETAILLEE DES LIGNES DATAVIZ (Index 0 a Max) :");
            
            for (int i = 0; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                
                Cell cCode = r.getCell(1); // Col B
                Cell cDesc = r.getCell(0); // Col A
                Cell cNb = r.getCell(3);   // Col D
                
                String code = (cCode == null) ? "" : cCode.toString();
                String desc = (cDesc == null) ? "" : cDesc.toString();
                double nb = (cNb != null && cNb.getCellType() == CellType.NUMERIC) ? cNb.getNumericCellValue() : 0;
                
                // On affiche les lignes ayant des données numériques pour debugging
                if (nb > 0) {
                    System.out.println("Ligne " + (i + 1) + " : [Col A]" + desc + " | [Col B]" + code + " | NB=" + nb);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
