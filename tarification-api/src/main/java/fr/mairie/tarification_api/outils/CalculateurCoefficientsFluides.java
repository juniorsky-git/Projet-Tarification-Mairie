package fr.mairie.tarification_api.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;

public class CalculateurCoefficientsFluides {
    private static final String FICHIER = new File("Donnees/Autres/CALC DEP(4).xlsx").exists() 
        ? "Donnees/Autres/CALC DEP(4).xlsx" 
        : "../Donnees/Autres/CALC DEP(4).xlsx";

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            analyserFeuille(wb, "Conso eau", "m3");
            analyserFeuille(wb, "CONSO GAZ", "kWh");
            analyserFeuille(wb, "CONSO ELEC", "kWh");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyserFeuille(Workbook wb, String nomFeuille, String unite) {
        Sheet s = wb.getSheet(nomFeuille);
        if (s == null) return;

        System.out.println("\n--- ANALYSE " + nomFeuille + " ---");
        // On cherche une ligne de total ou de paramètre
        for (int i = 0; i < Math.min(100, s.getLastRowNum()); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;
            for (int j = 0; j < 30; j++) {
                Cell c = r.getCell(j);
                if (c == null) continue;
                String val = c.toString().toLowerCase();
                if (val.contains("tarif") || val.contains("prix") || val.contains("coût") || val.contains("coefficient")) {
                    System.out.println("Ligne " + i + " Col " + j + " : [" + val + "] -> Valeur suivante: " + getSafe(r, j+1));
                }
            }
        }
    }

    private static String getSafe(Row r, int col) {
        Cell c = r.getCell(col);
        return c == null ? "VIDE" : c.toString();
    }
}
