package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class DebugSimulationCols {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(8);
            System.out.println("--- DEBUG COLONNES SIMULATON lignes 6 a 17 ---");
            for (int i = 6; i <= 16; i++) {
                Row r = s.getRow(i);
                if (r == null) { System.out.println("L" + (i+1) + " : NULL"); continue; }
                System.out.print("L" + (i+1) + " | ");
                for (int j = 0; j <= 8; j++) {
                    Cell c = r.getCell(j);
                    String val = (c == null) ? "NULL" : "[" + c.getCellType() + "=" + c.toString().trim() + "]";
                    System.out.print("Col" + j + ":" + val + " | ");
                }
                System.out.println();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
