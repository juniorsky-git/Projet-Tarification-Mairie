package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic robuste pour l'analyse des libellés de facturation.
 * Gère les valeurs nulles et identifie les dépenses hors-prestataire.
 */
public class AnalyseLibellesProper {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/CALC DEP.xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            System.out.println("DEPENSES 'CACHEES' (Analyse des libelles hors Scolarest) :");
            
            double totalHorsScolarest = 0;
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                
                Cell cTTC = r.getCell(7);
                Cell cTiers = r.getCell(9);
                Cell cLib = r.getCell(3);
                Cell cAnt = r.getCell(19);
                
                if (cTTC == null || cTiers == null || cLib == null) {
                    continue;
                }

                String tiers = cTiers.toString().toUpperCase();
                String libelle = cLib.toString();
                String antenne = (cAnt == null) ? "" : cAnt.toString();
                double montant = (cTTC.getCellType() == CellType.NUMERIC) ? cTTC.getNumericCellValue() : 0;

                // On cherche ce qui n'est pas du Scolarest mais est lié au service restauration
                if (!tiers.contains("SCOLAREST") && (antenne.equalsIgnoreCase("CLMICH") || r.getCell(18).toString().contains("2-RE"))) {
                    System.out.printf("- [%.2f euros] %s (%s)%n", montant, libelle, tiers);
                    totalHorsScolarest += montant;
                }
            }
            
            System.out.println("\nTOTAL DES DEPENSES ANNEXES : " + totalHorsScolarest + " euros");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
