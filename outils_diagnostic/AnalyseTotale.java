package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Script de diagnostic complet pour lister toutes les dépenses.
 * Utilisé pour identifier l'origine de l'écart budgétaire (Périmètre scolaire vs global).
 */
public class AnalyseTotale {

    /**
     * Méthode de diagnostic principal.
     */
    public static void main(String[] args) {
        String fichier = "Donnees/Autres/CALC DEP.xlsx";
        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            System.out.println("LISTE COMPLETE DES DEPENSES RETENUES (Service 2-RE et CLMICH) :");
            
            double total = 0;
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i);
                if (r == null) {
                    continue;
                }
                
                Cell cTTC = r.getCell(7);
                Cell cTiers = r.getCell(9);
                Cell cLib = r.getCell(3);
                Cell cAnt = r.getCell(19);
                
                if (cTTC == null) {
                    continue;
                }

                String tiers = "INCONNU";
                if (cTiers != null) {
                    tiers = cTiers.toString();
                }

                String libelle = "";
                if (cLib != null) {
                    libelle = cLib.toString();
                }

                String antenne = "";
                if (cAnt != null) {
                    antenne = cAnt.toString();
                }

                double montant = 0;
                if (cTTC.getCellType() == CellType.NUMERIC) {
                    montant = cTTC.getNumericCellValue();
                }

                // On filtre sur les codes services identifiés
                if (antenne.equalsIgnoreCase("CLMICH") || (r.getCell(18) != null && r.getCell(18).toString().contains("2-RE"))) {
                    System.out.printf("- [%.2f euros] %s (%s)%n", montant, libelle, tiers);
                    total += montant;
                }
            }
            System.out.println("\nTOTAL CALCULE : " + total + " euros");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
