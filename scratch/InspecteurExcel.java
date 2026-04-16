
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;

public class InspecteurExcel {
    public static void main(String[] args) {
        String path = "Donnees/Grille-tarifaire-2024-(1).xlsx";
        File file = new File("c:/Users/stagedg2/Projet_mairie_outil_tarification/" + path);
        if (!file.exists()) {
            System.out.println("Le fichier n'existe pas : " + file.getAbsolutePath());
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            System.out.println("Fichier : " + path);
            System.out.println("Nombre d'onglets : " + wb.getNumberOfSheets());
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                System.out.println("Onglet " + i + " : " + wb.getSheetName(i));
            }

            // Inspectons l'onglet 0
            Sheet s = wb.getSheetAt(0);
            System.out.println("\n--- Apercu de l'onglet : " + s.getSheetName() + " ---");
            for (int i = 0; i < Math.min(30, s.getLastRowNum() + 1); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    System.out.println("Ligne " + i + " : VIDE");
                    continue;
                }
                System.out.print("Ligne " + i + " : ");
                for (int j = 0; j < Math.min(10, row.getLastCellNum()); j++) {
                    Cell cell = row.getCell(j);
                    System.out.print("[" + (cell == null ? "" : cell.toString()) + "] ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
