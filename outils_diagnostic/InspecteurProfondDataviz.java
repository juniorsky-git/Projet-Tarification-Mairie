package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class InspecteurProfondDataviz {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/Feuille_dataviz .xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            System.out.println("--- RECHERCHE DE TITRES DE SECTIONS DANS DATAVIZ ---");
            for (int i = 0; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell c = r.getCell(0);
                if (c != null && !c.toString().trim().isEmpty()) {
                    String val = c.toString().trim();
                    if (val.contains("Restauration") || val.contains("Espace") || val.contains("Total")) {
                        System.out.println("Ligne " + (i + 1) + " : " + val);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
