package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic pour isoler les factures les plus chères.
 * Utilisé pour identifier les mensualités Scolarest et les éventuels doublons.
 */
public class InspecteurDepensesLourdes {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/CALC DEP.xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            System.out.println("ANALYSE DES FACTURES > 10 000 euros :");
            
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                
                Cell cTTC = r.getCell(7); // Col H
                Cell cLib = r.getCell(3); // Col D
                
                if (cTTC == null || cLib == null) {
                    continue;
                }
                
                double montant = (cTTC.getCellType() == CellType.NUMERIC) ? cTTC.getNumericCellValue() : 0;
                
                // On isole les grosses factures scolaires
                if (montant > 10000 && r.getCell(18).toString().contains("2-RE")) {
                    System.out.printf("Ligne %d : [Montant: %.2f euros] %s%n", (i + 1), montant, cLib.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
