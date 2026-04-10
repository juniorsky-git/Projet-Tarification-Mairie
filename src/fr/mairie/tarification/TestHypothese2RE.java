package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class TestHypothese2RE {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            double totalScolairePur = 0;
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell cTTC = r.getCell(7);
                Cell cServ = r.getCell(18);
                Cell cAnt = r.getCell(19);
                if (cTTC == null || cServ == null) continue;
                
                String service = cServ.toString();
                String antenne = cAnt == null ? "" : cAnt.toString();
                double montant = (cTTC.getCellType() == CellType.NUMERIC) ? cTTC.getNumericCellValue() : 0;
                
                // HYPOTHESE : Service 2-RE (Ecoles) mais PAS l'antenne CLMICH (Centre de Loisirs)
                if (service.contains("2-RE") && !antenne.equalsIgnoreCase("CLMICH")) {
                    totalScolairePur += montant;
                }
            }
            System.out.println("TOTAL SERVICE 2-RE (HORS CLMICH) : " + totalScolairePur + " euros");
            System.out.println("COUT MOYEN AVEC CE TOTAL         : " + (totalScolairePur / (1128 * 140)) + " euros");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
