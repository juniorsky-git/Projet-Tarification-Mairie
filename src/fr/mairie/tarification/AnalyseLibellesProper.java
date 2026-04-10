package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class AnalyseLibellesProper {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            System.out.println("DEPENSES 'CACHEES' (Hors Scolarest) :");
            double totalHorsScolarest = 0;
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell cTTC = r.getCell(7);
                Cell cTiers = r.getCell(9);
                Cell cLib = r.getCell(3);
                Cell cAnt = r.getCell(19);
                if (cTTC == null || cTiers == null || cLib == null) continue;

                String tiers = cTiers.toString().toUpperCase();
                String libelle = cLib.toString();
                String antenne = cAnt == null ? "" : cAnt.toString();
                double montant = (cTTC.getCellType() == CellType.NUMERIC) ? cTTC.getNumericCellValue() : 0;

                if (!tiers.contains("SCOLAREST") && (antenne.equalsIgnoreCase("CLMICH") || r.getCell(18).toString().contains("2-RE"))) {
                    System.out.println("- [" + String.format("%.2f", montant) + " euros] " + libelle + " (" + tiers + ")");
                    totalHorsScolarest += montant;
                }
            }
            System.out.println("\nTOTAL DE CES DEPENSES ANNEXES : " + String.format("%.2f", totalHorsScolarest) + " euros");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
