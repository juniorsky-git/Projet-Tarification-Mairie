package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;

public class DebugLoisirs {
    public static void main(String[] args) {
        String filename = "Donnees/Autres/CALC DEP.xlsx";
        File f = new File(filename);
        System.out.println("Checking file: " + f.getAbsolutePath());
        System.out.println("Exists: " + f.exists());

        try (FileInputStream fis = new FileInputStream(f);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            int sheetIndex = 8;
            Sheet s = wb.getSheetAt(sheetIndex);
            System.out.println("Sheet name: " + s.getSheetName());

            // Check rows 41 to 47 (0-based) which are rows 42-48 in Excel
            for (int i = 41; i <= 47; i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    System.out.println("Row " + i + " is null");
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    Cell cell = row.getCell(c);
                    sb.append("[").append(c).append(":");
                    if (cell == null) sb.append("null");
                    else sb.append(cell.toString());
                    sb.append("] ");
                }
                System.out.println("Row " + (i+1) + ": " + sb.toString());
                
                Cell cellR = row.getCell(17);
                if (cellR != null) {
                    System.out.println("  -> Col R (17): Type=" + cellR.getCellType() + ", Value=" + cellR.toString());
                } else {
                    System.out.println("  -> Col R (17) is null");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
