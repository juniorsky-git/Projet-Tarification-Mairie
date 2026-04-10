package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
public class InspecteurDepensesLourdes {
    public static void main(String[] args) {
        String[] fichiers = {"Donnees/Autres/CALC DEP.xlsx", "Donnees/Autres/CALC DEP (1).xlsx"};
        for (String chemin : fichiers) {
            System.out.println("\n--- " + chemin + " ---");
            try (FileInputStream fis = new FileInputStream(chemin); Workbook wb = WorkbookFactory.create(fis)) {
                for(int i=0; i<wb.getNumberOfSheets(); i++) {
                    System.out.println("Onglet " + i + " : " + wb.getSheetName(i));
                }
                Sheet s = wb.getSheetAt(0);
                for (int i = 0; i < 5; i++) {
                    Row r = s.getRow(i); if (r == null) continue;
                    for (Cell c : r) System.out.print(c.toString() + " | ");
                    System.out.println();
                }
            } catch (Exception e) { System.err.println(e.getMessage()); }
        }
    }
}
