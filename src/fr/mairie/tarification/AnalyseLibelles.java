package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class AnalyseLibelles {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            System.out.println("Top 15 des libelles de depenses (Autres que Scolarest) :");
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                String libelle = r.getCell(3).toString();
                String tiers = r.getCell(9).toString();
                String service = r.getCell(18).toString();
                if (!tiers.contains("SCOLAREST") && (service.contains("2-RE") || r.getCell(19).toString().contains("CLMICH"))) {
                    System.out.println("- [" + r.getCell(7).toString() + " euros] " + libelle);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
