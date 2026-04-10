package fr.mairie.tarification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class InspecteurCalcDep {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("Donnees/Autres/CALC DEP.xlsx");
             Workbook wb = WorkbookFactory.create(fis)) {

            System.out.println("Nombre d onglets dans CALC DEP : " + wb.getNumberOfSheets());
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet s = wb.getSheetAt(i);
                System.out.println("Onglet " + i + " : " + s.getSheetName() + " (" + s.getLastRowNum() + " lignes)");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
