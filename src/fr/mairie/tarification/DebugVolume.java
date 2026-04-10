package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class DebugVolume {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/Feuille_dataviz .xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            for (int i = 0; i < 10; i++) {
                Row r = s.getRow(i);
                if (r == null) continue;
                System.out.print("Ligne " + (i+1) + " : ");
                for (int j=0; j<5; j++) {
                    Cell c = r.getCell(j);
                    System.out.print("[" + j + "]" + (c == null ? "null" : c.toString()) + " | ");
                }
                System.out.println();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
