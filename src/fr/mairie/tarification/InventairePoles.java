package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

public class InventairePoles {
    public static void main(String[] args) {
        System.out.println("--- INVENTAIRE DES POLES DISPONIBLES ---");
        
        // 1. Recherche dans Ciril (D?penses)
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            Set<String> antennes = new HashSet<>();
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                Cell c = r.getCell(19); // Antenne
                if (c != null) antennes.add(c.toString().trim());
            }
            System.out.println("\nAntennes trouv?es dans la comptabilit? (Ciril) :");
            for (String a : antennes) if (!a.isEmpty()) System.out.println("- " + a);
        } catch (Exception e) { e.printStackTrace(); }

        // 2. Recherche dans Dataviz (Effectifs)
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/Feuille_dataviz .xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            System.out.println("\nSections trouv?es dans les effectifs (Dataviz) :");
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet s = wb.getSheetAt(i);
                System.out.println("\nOnglet : " + s.getSheetName());
                for (int j = 0; j <= s.getLastRowNum(); j++) {
                    Row r = s.getRow(j); if (r == null) continue;
                    Cell c = r.getCell(0);
                    if (c != null && c.toString().contains("Restauration - Donn")) {
                        System.out.println("  > Section d?tect?e : " + c.toString());
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
