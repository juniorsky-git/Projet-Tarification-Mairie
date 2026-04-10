package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class AnalyseTotale {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            System.out.println("LISTE COMPLETE DES DEPENSES RETENUES :");
            double total = 0;
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell cTTC = r.getCell(7);
                Cell cTiers = r.getCell(9);
                Cell cLib = r.getCell(3);
                Cell cAnt = r.getCell(19);
                if (cTTC == null) continue;

                String tiers = cTiers == null ? "INCONNU" : cTiers.toString();
                String libelle = cLib == null ? "" : cLib.toString();
                String antenne = cAnt == null ? "" : cAnt.toString();
                double montant = (cTTC.getCellType() == CellType.NUMERIC) ? cTTC.getNumericCellValue() : 0;

                if (antenne.equalsIgnoreCase("CLMICH") || (r.getCell(18) != null && r.getCell(18).toString().contains("2-RE"))) {
                    System.out.println(String.format("- [%.2f euros] %s (%s)", montant, libelle, tiers));
                    total += montant;
                }
            }
            System.out.println("\nTOTAL CALCUL? : " + total + " euros");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
