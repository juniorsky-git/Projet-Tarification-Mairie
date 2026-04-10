package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic pour isoler les factures Scolarest.
 * Utilisé pour identifier la part du prestataire dans le coût final.
 */
public class TestScolarestTotal {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/CALC DEP.xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            double totalScolarest = 0;
            
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                
                Cell cTTC = r.getCell(7);
                Cell cTiers = r.getCell(9);
                
                if (cTTC == null || cTiers == null) {
                    continue;
                }
                
                String tiers = cTiers.toString().toUpperCase();
                double montant = (cTTC.getCellType() == CellType.NUMERIC) ? cTTC.getNumericCellValue() : 0;
                
                // On isole uniquement le prestataire COMPASS GROUP (Scolarest)
                if (tiers.contains("SCOLAREST")) {
                    totalScolarest += montant;
                }
            }
            
            System.out.println("TOTAL SCOLAREST UNIQUEMENT : " + totalScolarest + " euros");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
