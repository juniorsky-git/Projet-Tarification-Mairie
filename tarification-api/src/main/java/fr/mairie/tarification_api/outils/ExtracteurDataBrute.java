package fr.mairie.tarification_api.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;

public class ExtracteurDataBrute {
    private static final String FICHIER = new File("Donnees/Autres/CALC DEP(4).xlsx").exists() 
        ? "Donnees/Autres/CALC DEP(4).xlsx" 
        : "../Donnees/Autres/CALC DEP(4).xlsx";

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            extraire(wb, "Conso eau", 10);
            extraire(wb, "CONSO GAZ", 15);
            extraire(wb, "CONSO ELEC", 15);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void extraire(Workbook wb, String nomFeuille, int maxRows) {
        Sheet s = wb.getSheet(nomFeuille);
        if (s == null) return;
        System.out.println("\n=== EXTRACTION : " + nomFeuille + " ===");
        for (int i = 0; i < maxRows; i++) {
            Row r = s.getRow(i);
            if (r == null) continue;
            System.out.print("R" + i + ": ");
            for (int j = 0; j < 25; j++) {
                Cell c = r.getCell(j);
                System.out.print("[" + (c == null ? "" : c.toString().replace("\n", " ")) + "] | ");
            }
            System.out.println();
        }
    }
}
