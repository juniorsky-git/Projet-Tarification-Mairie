package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class InspecteurFrequence {
    public static void main(String[] args) {
        String[] fichiers = {"Donnees/Autres/Feuille_dataviz .xlsx", "Donnees/Autres/matrice_tarification_modele.xlsx"};
        for (String chemin : fichiers) {
            System.out.println("\n--- " + chemin + " ---");
            try (FileInputStream fis = new FileInputStream(chemin); Workbook wb = WorkbookFactory.create(fis)) {
                Sheet s = wb.getSheetAt(0);
                for (int i = 0; i < Math.min(s.getLastRowNum(), 10); i++) {
                    Row r = s.getRow(i); if (r == null) continue;
                    for (Cell c : r) System.out.print(c.toString() + " | ");
                    System.out.println();
                }
            } catch (Exception e) { System.err.println(e.getMessage()); }
        }
    }
}
