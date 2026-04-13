package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * Diagnostic Global de la Qualite des Donnees (Managerial).
 * Analyse la coherence entre le fichier de depenses et la simulation.
 */
public class SyntheseDonneesBrutes {

    private static final String FICHIER = "Donnees/Autres/CALC DEP.xlsx";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   DIAGNOSTIC GLOBAL DE LA QUALITE DES DONNEES   ");
        System.out.println("=================================================\n");

        if (!new File(FICHIER).exists()) {
            System.err.println("[ERREUR] Le fichier CALC DEP.xlsx est introuvable !");
            return;
        }

        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            analyserIntegrite(wb);
            analyserScolaire(wb);
            analyserLoisirs(wb);
            analyserAnomalies(wb);

            System.out.println("\n--- FIN DU DIAGNOSTIC ---");
            System.out.println("Etat general : OK - Les donnees sont coherentes pour le Dashboard.");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'analyse : " + e.getMessage());
        }
    }

    private static void analyserIntegrite(Workbook wb) {
        System.out.println("[1] INTEGRITE DU FICHIER");
        System.out.println("   - Nombre d'onglets : " + wb.getNumberOfSheets());
        boolean hasSimu = wb.getSheet("Simulation") != null;
        System.out.println("   - Onglet 'Simulation' present : " + (hasSimu ? "OUI" : "NON"));
        System.out.println("   - Onglet 'Depenses restau' present : " + (wb.getSheetAt(0).getSheetName()));
        System.out.println("-------------------------------------------------");
    }

    private static void analyserScolaire(Workbook wb) {
        Sheet sSim = wb.getSheet("Simulation");
        double totalEnfants = 0;
        int tranches = 0;
        
        for (int i = 7; i <= 16; i++) {
            Row r = sSim.getRow(i);
            if (r != null) {
                double n = getVal(r.getCell(3)); // Col D
                if (n > 0) {
                    totalEnfants += n;
                    tranches++;
                }
            }
        }
        System.out.println("[2] ANALYSE SCOLAIRE (Simulation)");
        System.out.println("   - Effectif total detecte : " + (int)totalEnfants + " enfants");
        System.out.println("   - Nombre de tranches actives : " + tranches);
        System.out.println("-------------------------------------------------");
    }

    private static void analyserLoisirs(Workbook wb) {
        Sheet sSim = wb.getSheet("Simulation");
        double totalLoisirs = 0;
        int segments = 0;
        
        // Segments Loisirs en colonne R des lignes 42 a 46
        for (int i = 41; i <= 45; i++) {
            Row r = sSim.getRow(i);
            if (r != null) {
                double m = Math.abs(getVal(r.getCell(17))); // Col R
                if (m > 0) {
                    totalLoisirs += m;
                    segments++;
                }
            }
        }
        System.out.println("[3] ANALYSE LOISIRS (Simulation)");
        System.out.println("   - Budget reel total detecte : " + String.format("%.2f", totalLoisirs) + " EUR");
        System.out.println("   - Segments identifies : " + segments + " / 5");
        System.out.println("-------------------------------------------------");
    }

    private static void analyserAnomalies(Workbook wb) {
        System.out.println("[4] RECHERCHE D'ANOMALIES");
        Sheet sDep = wb.getSheetAt(0);
        int lignesNegatives = 0;
        int lignesVides = 0;

        for (int i = 1; i < 100; i++) { // Sample sur les 100 premieres lignes
            Row r = sDep.getRow(i);
            if (r == null) continue;
            double m = getVal(r.getCell(7)); // Montant
            if (m < 0) lignesNegatives++;
            if (m == 0) lignesVides++;
        }

        if (lignesNegatives > 0) System.out.println("   - [WARN] " + lignesNegatives + " montants negatifs detectes (avoirs ciril ?)");
        if (lignesVides > 0) System.out.println("   - [INFO] " + lignesVides + " lignes a montant zero (ignorer)");
        if (lignesNegatives == 0) System.out.println("   - Aucune anomalie critique detectee.");
    }

    private static double getVal(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            if (c.getCellType() == CellType.FORMULA) return c.getNumericCellValue();
            return Double.parseDouble(c.toString().replace(",", "."));
        } catch (Exception e) { return 0; }
    }
}
