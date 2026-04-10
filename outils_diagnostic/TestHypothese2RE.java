package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic pour valider l'hypothèse du service "2-RE".
 * Utilisé pour comparer le coût "Scolaire pur" vs "Global".
 */
public class TestHypothese2RE {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/CALC DEP.xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            double totalScolairePur = 0;
            
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                
                Cell cTTC = r.getCell(7);
                Cell cServ = r.getCell(18);
                Cell cAnt = r.getCell(19);
                
                if (cTTC == null || cServ == null) {
                    continue;
                }
                
                String service = cServ.toString();
                String antenne = (cAnt == null) ? "" : cAnt.toString();
                double montant = (cTTC.getCellType() == CellType.NUMERIC) ? cTTC.getNumericCellValue() : 0;
                
                // HYPOTHESE : Le service 2-RE (Cantine Ecoles) mais sans Louise Michel (Centre)
                if (service.contains("2-RE") && !antenne.equalsIgnoreCase("CLMICH")) {
                    totalScolairePur += montant;
                }
            }
            
            System.out.println("TOTAL SERVICE 2-RE (HORS CLMICH) : " + totalScolairePur + " euros");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
