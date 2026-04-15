import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.File;

/**
 * Petit utilitaire pour créer une grille 2024 de test.
 */
public class GenerateurTest {
    public static void main(String[] args) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Grille 2024");
            
            // Entête
            Row header = sheet.createRow(0);
            String[] labels = {"Tranche", "QF Min", "QF Max", "REPAS", "LOISIRS", "DEMI", "MAT_SOIR", "MAT_OU_SOIR", "ETU_MOIS", "ETU_DEMI", "ADOS_R", "ADOS_S", "ADOS_DR", "ADOS_DS", "SORTIE_D", "SORTIE_J"};
            for(int i=0; i<labels.length; i++) header.createCell(i).setCellValue(labels[i]);

            // Tranche A (Exemple 2024 avec des prix différents de 2025)
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("A (2024)");
            r1.createCell(1).setCellValue(18000);
            r1.createCell(2).setCellValue(999999);
            for(int i=3; i<16; i++) r1.createCell(i).setCellValue(10.0 + i); // Prix bidon pour test

            File dir = new File("Donnees");
            if (!dir.exists()) dir.mkdir();

            try (FileOutputStream fileOut = new FileOutputStream("Donnees/grille_2024_exemple.xlsx")) {
                wb.write(fileOut);
            }
            System.out.println("Fichier de test genere avec succes dans Donnees/grille_2024_exemple.xlsx");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
