package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class ScannerTotalDataviz {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/Feuille_dataviz .xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            System.out.println("--- SCAN DES 200 PREMIERES LIGNES ---");
            for (int i = 0; i < 200; i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell c = r.getCell(0);
                if (c != null && !c.toString().trim().isEmpty()) {
                    System.out.println("Ligne " + (i + 1) + " : " + c.toString().trim());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
