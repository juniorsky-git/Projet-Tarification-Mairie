import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;

public class DebugExcel {
    public static void main(String[] args) throws Exception {
        // Lire AVEC formules (pas data_only)
        FileInputStream fis = new FileInputStream("CALC DEP(4).xlsx");
        Workbook wb = new XSSFWorkbook(fis);
        Sheet ws = wb.getSheet("CALC DEP(4)");

        System.out.println("=== EN-TETES (ligne 6, index 5) ===");
        Row header = ws.getRow(5);
        if (header != null) {
            for (int c = 0; c < 12; c++) {
                Cell cell = header.getCell(c);
                if (cell != null) System.out.println("  Col " + c + ": " + cell);
            }
        }

        System.out.println("\n=== DONNEES ET FORMULES (lignes 7-17, index 6-16) ===");
        for (int r = 6; r <= 16; r++) {
            Row row = ws.getRow(r);
            if (row == null) { System.out.println("Row " + (r+1) + ": NULL"); continue; }
            System.out.println("--- Ligne Excel " + (r + 1) + " ---");
            for (int c = 0; c <= 8; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) { System.out.println("  Col " + c + ": (vide)"); continue; }
                String type = cell.getCellType().toString();
                String val;
                if (cell.getCellType() == CellType.FORMULA) {
                    val = "FORMULE=" + cell.getCellFormula();
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    val = "NUM=" + cell.getNumericCellValue();
                } else {
                    val = "TXT=" + cell.getStringCellValue();
                }
                System.out.println("  Col " + c + " [" + type + "]: " + val);
            }
        }

        wb.close();
        fis.close();
    }
}
