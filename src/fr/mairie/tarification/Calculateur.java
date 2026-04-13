package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Moteur de calcul financier simplifie.
 * Source UNIQUE de verite : Onglet 'syntheses charges' de CALC DEP (3).xlsx.
 */
public class Calculateur {

    private static final String FICHIER_DEPENSES = "Donnees/Autres/CALC DEP (3).xlsx";
    private static final String ONGLET_SYNTHESE = "syntheses charges";

    public static class SyntheseGlobale {
        public Map<String, Map<String, Double>> depenses = new LinkedHashMap<>();
        public Map<String, Map<String, Double>> tarifs = new LinkedHashMap<>();
        public Map<String, Double> effectifs = new LinkedHashMap<>();
        public Map<String, Double> totauxDepenses = new HashMap<>();
    }

    private SyntheseGlobale syntheseCachee = null;

    public SyntheseGlobale getSynthese() {
        if (syntheseCachee != null) return syntheseCachee;
        
        SyntheseGlobale sg = new SyntheseGlobale();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet(ONGLET_SYNTHESE);
            if (s == null) return sg;

            String[] poles = {"Restauration", "Accueil de Loisirs", "Accueil periscolaire", "Etudes surveillees", "Espace Ados", "Sejours"};
            int[] colIndices = {3, 4, 5, 6, 7, 8};

            // 1. DEPENSES (Lignes 4 a 21)
            for (int i = 3; i <= 20; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;
                String nature = getValeurTexte(row.getCell(1));
                if (nature.isEmpty()) nature = getValeurTexte(row.getCell(0));
                if (nature.isEmpty()) continue;
                for (int j = 0; j < poles.length; j++) {
                    double montant = Math.abs(getValeurNumerique(row.getCell(colIndices[j])));
                    if (montant > 0) sg.depenses.computeIfAbsent(poles[j], k -> new LinkedHashMap<>()).put(nature, montant);
                }
            }

            // Totaux (Ligne 22)
            Row totalRow = s.getRow(21);
            if (totalRow != null) {
                for (int j = 0; j < poles.length; j++) sg.totauxDepenses.put(poles[j], getValeurNumerique(totalRow.getCell(colIndices[j])));
            }

            // 2. RECETTES & EFFECTIFS (Lignes 30 a 39)
            for (int i = 29; i <= 38; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;
                String tranche = getValeurTexte(row.getCell(1));
                if (tranche.isEmpty()) tranche = getValeurTexte(row.getCell(0));
                sg.effectifs.put(tranche, getValeurNumerique(row.getCell(2)));
                for (int j = 0; j < poles.length; j++) {
                    sg.tarifs.computeIfAbsent(tranche, k -> new HashMap<>()).put(poles[j], getValeurNumerique(row.getCell(colIndices[j])));
                }
            }
            syntheseCachee = sg;
        } catch (Exception e) {
            System.err.println("Erreur chargement synthese : " + e.getMessage());
        }
        return sg;
    }

    public double calculerTotalDepenses(String pole) {
        return getSynthese().totauxDepenses.getOrDefault(pole, 0.0);
    }

    public double calculerRecettesAnnuelles(String pole, double multiplicateur) {
        SyntheseGlobale sg = getSynthese();
        double total = 0;
        for (String tranche : sg.effectifs.keySet()) {
            double nb = sg.effectifs.getOrDefault(tranche, 0.0);
            double tarif = sg.tarifs.getOrDefault(tranche, new HashMap<>()).getOrDefault(pole, 0.0);
            total += nb * tarif * multiplicateur;
        }
        return total;
    }

    public Map<String, Double> getDepensesDetaillees(String pole) {
        return getSynthese().depenses.getOrDefault(pole, new LinkedHashMap<>());
    }

    // Compatibilite avec Main.java
    public double calculerDepensesRestauration() { return calculerTotalDepenses("Restauration"); }
    public double calculerDepensesAccueilLoisirs() { return calculerTotalDepenses("Accueil de Loisirs"); }
    public double calculerDepensesAccueilPeriscolaire() { return calculerTotalDepenses("Accueil periscolaire"); }
    public double calculerDepensesEtudesSurveillees() { return calculerTotalDepenses("Etudes surveillees"); }
    public double calculerDepensesEspaceAdos() { return calculerTotalDepenses("Espace Ados"); }
    public double calculerDepensesSejours() { return calculerTotalDepenses("Sejours"); }

    public double calculerRecettesRestauration() { return calculerRecettesAnnuelles("Restauration", 140); }
    public double calculerRecettesAccueilLoisirs() { return calculerRecettesAnnuelles("Accueil de Loisirs", 1); }
    public double calculerRecettesAccueilPeriscolaire() { return calculerRecettesAnnuelles("Accueil periscolaire", 10); }
    public double calculerRecettesEtudesSurveillees() { return calculerRecettesAnnuelles("Etudes surveillees", 10); }
    public double calculerRecettesEspaceAdos() { return calculerRecettesAnnuelles("Espace Ados", 1); }
    public double calculerRecettesSejours() { return calculerRecettesAnnuelles("Sejours", 1); }

    private String getValeurTexte(Cell c) {
        return (c == null) ? "" : c.toString().trim();
    }

    private double getValeurNumerique(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC || c.getCellType() == CellType.FORMULA) return c.getNumericCellValue();
            String s = c.toString().trim().replace(",", ".");
            return s.isEmpty() ? 0 : Double.parseDouble(s);
        } catch (Exception e) { return 0; }
    }
}
