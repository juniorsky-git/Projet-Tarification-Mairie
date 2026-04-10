package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class ScannerUltimeDataviz {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/Feuille_dataviz .xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            System.out.println("--- RECHERCHE DE TRANCHES (A, B, C...) DANS TOUT LE FICHIER ---");
            for (int i = 0; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell c = r.getCell(1); // Col B
                if (c != null && c.toString().trim().length() <= 2 && !c.toString().trim().isEmpty()) {
                    String val = c.toString().trim();
                    if (val.matches("[A-G2]*|EXT")) {
                        System.out.println("L" + (i + 1) + " : Tranche detected [" + val + "] | Contexte (A): " + r.getCell(0));
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
