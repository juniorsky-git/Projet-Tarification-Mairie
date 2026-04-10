package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class DebugFullVolume {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/Feuille_dataviz .xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            for (int i = 0; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell cCode = r.getCell(1); // Col B
                Cell cDesc = r.getCell(0); // Col A
                Cell cNb = r.getCell(3);   // Col D
                String code = cCode == null ? "" : cCode.toString();
                String desc = cDesc == null ? "" : cDesc.toString();
                double nb = (cNb != null && cNb.getCellType() == CellType.NUMERIC) ? cNb.getNumericCellValue() : 0;
                if (nb > 0) {
                    System.out.println("Ligne " + (i+1) + " : [Col A]" + desc + " | [Col B]" + code + " | NB=" + nb);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
