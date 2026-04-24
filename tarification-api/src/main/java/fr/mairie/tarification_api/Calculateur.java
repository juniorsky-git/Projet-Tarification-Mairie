package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Moteur de calcul financier pour la tarification municipale (Version API).
 * Lit directement le fichier Excel source pour alimenter le Dashboard.
 */
public class Calculateur {

    private static final String FICHIER = new File("Donnees/Autres/CALC DEP (3).xlsx").exists() 
        ? "Donnees/Autres/CALC DEP (3).xlsx" 
        : "../Donnees/Autres/CALC DEP (3).xlsx";
    
    private static final String ONGLET_SYNTHESE = "syntheses charges";

    public static class SyntheseGlobale {
        public Map<String, Map<String, Double>> depenses = new LinkedHashMap<>();
        public Map<String, Map<String, Double>> tarifs = new LinkedHashMap<>();
        public Map<String, Double> effectifs = new LinkedHashMap<>();
        public Map<String, Double> totauxDepenses = new HashMap<>();
    }

    private SyntheseGlobale syntheseCachee = null;

    public SyntheseGlobale getSynthese() {
        if (syntheseCachee != null) {
            return syntheseCachee;
        }
        
        SyntheseGlobale sg = new SyntheseGlobale();
        try (FileInputStream fis = new FileInputStream(new File(FICHIER));
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet(ONGLET_SYNTHESE);
            if (s == null) {
                return sg;
            }

            String[] poles = {"Restauration", "Accueil de Loisirs", "Accueil periscolaire", "Etudes surveillees", "Espace Ados", "Sejours"};
            int[] colIndices = {3, 4, 5, 6, 7, 8};

            // Extraction des charges
            for (int i = 3; i <= 20; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;
                
                String nature = getValeurTexte(row.getCell(1));
                if (nature.isEmpty()) nature = getValeurTexte(row.getCell(0));
                if (nature.isEmpty()) continue;
                
                for (int j = 0; j < poles.length; j++) {
                    double montant = Math.abs(getValeurNumerique(row.getCell(colIndices[j])));
                    if (montant > 0) {
                        sg.depenses.computeIfAbsent(poles[j], k -> new LinkedHashMap<>()).put(nature, montant);
                    }
                }
            }

            // Extraction des totaux
            Row totalRow = s.getRow(21);
            if (totalRow != null) {
                for (int j = 0; j < poles.length; j++) {
                    sg.totauxDepenses.put(poles[j], getValeurNumerique(totalRow.getCell(colIndices[j])));
                }
            }

            // Extraction effectifs et tarifs
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
            System.err.println("Erreur de chargement Excel : " + e.getMessage());
        }
        return sg;
    }

    private String getValeurTexte(Cell c) {
        return (c == null) ? "" : c.toString().trim();
    }

    private double getValeurNumerique(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC || c.getCellType() == CellType.FORMULA) {
                return c.getNumericCellValue();
            }
            String s = c.toString().trim().replace(",", ".");
            return s.isEmpty() ? 0 : Double.parseDouble(s);
        } catch (Exception e) { 
            return 0; 
        }
    }
}
