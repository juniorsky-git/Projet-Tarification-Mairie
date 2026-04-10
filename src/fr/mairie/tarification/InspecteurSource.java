package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class InspecteurSource {
    public static void main(String[] args) {
        String[] fichiers = {
            "Donnees/Autres/depenses centre de loisirs 2025.xlsx",
            "Donnees/Autres/Classeur pour la tarification.xlsx"
        };

        for (String chemin : fichiers) {
            System.out.println("\n--- Analyse de : " + chemin + " ---");
            try (FileInputStream fis = new FileInputStream(chemin);
                 Workbook workbook = WorkbookFactory.create(fis)) {
                
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    System.out.println("Onglet : " + sheet.getSheetName());
                    
                    // On affiche les 5 premières lignes pour comprendre la structure
                    for (int j = 0; j < Math.min(sheet.getLastRowNum(), 5); j++) {
                        Row row = sheet.getRow(j);
                        if (row == null) continue;
                        System.out.print("L" + (j+1) + " | ");
                        for (Cell cell : row) {
                            System.out.print(cell.toString() + " | ");
                        }
                        System.out.println();
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur sur " + chemin + " : " + e.getMessage());
            }
        }
    }
}
