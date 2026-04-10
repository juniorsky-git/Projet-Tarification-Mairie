package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class TestScolarestTotal {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            double totalScolarest = 0;
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell cTTC = r.getCell(7);
                Cell cTiers = r.getCell(9);
                if (cTTC == null || cTiers == null) continue;
                String tiers = cTiers.toString().toUpperCase();
                double montant = (cTTC.getCellType() == CellType.NUMERIC) ? cTTC.getNumericCellValue() : 0;
                if (tiers.contains("SCOLAREST")) {
                    totalScolarest += montant;
                }
            }
            System.out.println("TOTAL SCOLAREST UNIQUEMENT : " + totalScolarest + " euros");
            System.out.println("COUT MOYEN AVEC CE TOTAL   : " + (totalScolarest / (1128 * 140)) + " euros");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
