package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class InspecteurSourcesPoles {
    public static void main(String[] args) {
        String[] files = {
            "Donnees/Autres/Classeur pour la tarification.xlsx",
            "Donnees/Autres/depenses centre de loisirs 2025.xlsx"
        };
        for (String f : files) {
            try (FileInputStream fis = new FileInputStream(f);
                 Workbook wb = WorkbookFactory.create(fis)) {
                System.out.println("\n--- Fichier : " + f + " ---");
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet s = wb.getSheetAt(i);
                    System.out.println("Onglet : " + s.getSheetName());
                    // On regarde les 5 premieres lignes de chaque onglet
                    for (int j = 0; j < 5; j++) {
                        Row r = s.getRow(j); if (r == null) continue;
                        System.out.print("  L" + (j+1) + " : ");
                        for (int k = 0; k < 5; k++) {
                            Cell c = r.getCell(k);
                            System.out.print("[" + (c==null?"":c.toString()) + "] ");
                        }
                        System.out.println();
                    }
                }
            } catch (Exception e) { System.err.println("Erreur " + f + " : " + e.getMessage()); }
        }
    }
}
