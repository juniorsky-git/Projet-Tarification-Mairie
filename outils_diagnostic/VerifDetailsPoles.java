package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class VerifDetailsPoles {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/Feuille_dataviz .xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            // On regarde un peu plus loin pour chaque pole
            int[] scans = {72, 106, 56}; // Ados, Peri, Etudes
            for (int start : scans) {
                System.out.println("\n--- Scan a partir de la ligne " + start + " ---");
                for (int i = start; i < start + 20; i++) {
                    Row r = s.getRow(i); if (r == null) continue;
                    Cell cA = r.getCell(0);
                    Cell cB = r.getCell(1);
                    Cell cD = r.getCell(3);
                    System.out.println("L" + (i+1) + " : [A]" + cA + " | [B]" + cB + " | [D]" + cD);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
