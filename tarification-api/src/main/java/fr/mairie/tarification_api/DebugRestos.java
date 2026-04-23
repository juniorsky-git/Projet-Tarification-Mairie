package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;

public class DebugRestos {
    public static void main(String[] args) throws Exception {
        String path = "Donnees/Autres/CALC DEP(4).xlsx";
        if (!new File(path).exists()) path = "../" + path;
        
        FileInputStream fis = new FileInputStream(path);
        Workbook wb = WorkbookFactory.create(fis);
        Sheet s = wb.getSheet("CONSO ELEC");
        
        for (int i = 0; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;
            Cell c = r.getCell(5); // Adresse
            if (c != null && c.toString().contains("RESTOS")) {
                System.out.println("LIGNE TROUVEE : " + i);
                for (int col = 0; col < r.getLastCellNum(); col++) {
                    System.out.print("[" + col + "]:" + r.getCell(col) + " | ");
                }
                System.out.println();
            }
        }
        wb.close();
    }
}
