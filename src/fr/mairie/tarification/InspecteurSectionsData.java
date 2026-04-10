package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class InspecteurSectionsData {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/Feuille_dataviz .xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            int[] starts = {4, 22, 39, 72, 106};
            String[] names = {"ENFANCE", "RESTAURATION", "LOISIRS", "ADOS", "PERISCOLAIRE"};
            
            for (int k = 0; k < starts.length; k++) {
                System.out.println("\n--- Section " + names[k] + " (Ligne " + (starts[k]) + ") ---");
                // On regarde les 8 lignes suivantes pour voir les tranches (A, B, C...)
                for (int i = starts[k]; i < starts[k] + 12; i++) {
                    Row r = s.getRow(i); if (r == null) continue;
                    Cell cB = r.getCell(1); // Col B (Tranche)
                    Cell cD = r.getCell(3); // Col D (Effectif)
                    if (cB != null && !cB.toString().trim().isEmpty()) {
                        System.out.println("  Tranche: " + cB.toString() + " | NB: " + (cD == null ? "0" : cD.toString()));
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
