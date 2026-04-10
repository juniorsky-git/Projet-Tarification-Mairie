package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class InspecteurSimulationEntete {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(8); // Onglet Simulation
            System.out.println("--- LIGNES 1 A 20 DE L ONGLET SIMULATION ---");
            for (int i = 0; i <= 20; i++) {
                Row r = s.getRow(i);
                if (r == null) continue;
                System.out.print("L" + (i + 1) + " | ");
                for (int j = 0; j < 10; j++) {
                    Cell c = r.getCell(j);
                    String val = (c == null) ? "" : c.toString().trim();
                    System.out.print("[" + val + "] ");
                }
                System.out.println();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
