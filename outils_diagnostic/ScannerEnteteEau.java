package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;

public class ScannerEnteteEau {
    private static final String FICHIER = "Donnees/Autres/CALC DEP(4).xlsx";

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Conso eau");
            if (s == null) {
                System.out.println("Onglet 'Conso eau' introuvable !");
                return;
            }

            // Scanner la ligne d'entête (index 4 ou 5)
            Row headers = s.getRow(4); 
            if (headers == null) headers = s.getRow(5);

            if (headers != null) {
                System.out.println("=== ANALYSE DES COLONNES (Onglet Conso eau) ===");
                for (int i = 0; i < headers.getLastCellNum(); i++) {
                    Cell c = headers.getCell(i);
                    System.out.println("Col " + i + " : [" + (c != null ? c.toString().replace("\n", " ") : "VIDE") + "]");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
