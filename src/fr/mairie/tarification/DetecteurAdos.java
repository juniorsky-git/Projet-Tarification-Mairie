package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class DetecteurAdos {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            System.out.println("--- RECHERCHE DE ADOS DANS LES DEPENSES ---");
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i); if (r == null) continue;
                String lib = r.getCell(3).toString().toUpperCase();
                if (lib.contains("ADOS")) {
                    System.out.println("L" + (i+1) + " : Lib=[" + lib + "] | Antenne(T)=[" + r.getCell(19) + "] | Service(S)=[" + r.getCell(18) + "]");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
