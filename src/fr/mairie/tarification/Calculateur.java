package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Moteur de calcul automatique des indicateurs financiers.
 */
public class Calculateur {

    private static final String FICHIER_DEPENSES = "Donnees/Autres/CALC DEP.xlsx";
    private static final String FICHIER_VOLUMES = "Donnees/Autres/Feuille_dataviz .xlsx";

    // Index colonnes Depenses (CALC DEP.xlsx - Onglet 0)
    private static final int COL_DEP_MONTANT_TTC = 7;  // Colonne H (TTC)
    private static final int COL_DEP_SERVICE     = 18; // Colonne S (2-RE pour Restauration)
    private static final int COL_DEP_ANTENNE     = 19; // Colonne T (CLMICH, etc.)

    // Index colonnes Volumes (Feuille_dataviz)
    private static final int COL_VOL_CODE_TRANCHE = 1; 
    private static final int COL_VOL_NB_ENFANTS   = 3;
    private static final int LIGNE_DEBUT_VOLUMES  = 3; // Ligne 4 dans Excel

    /**
     * Calcule le total des dépenses TTC pour une antenne donnée.
     */
    public double calculerTotalDepenses(String codeAntenne) {
        double total = 0;
        int lignesTrouvees = 0;
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            // On regarde l'onglet "0" qui est le plus complet pour la restauration
            Sheet s = wb.getSheetAt(0);
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String antenneLigne = getValeurTexte(row.getCell(COL_DEP_ANTENNE));
                String serviceLigne = getValeurTexte(row.getCell(COL_DEP_SERVICE));

                // On filtre sur l'antenne (ex: CLMICH) OU le service global (2-RE)
                if (antenneLigne.equalsIgnoreCase(codeAntenne) || serviceLigne.contains("2-RE")) {
                    double montant = getValeurNumerique(row.getCell(COL_DEP_MONTANT_TTC));
                    total += Math.abs(montant);
                    lignesTrouvees++;
                }
            }
            System.out.println("[Debug] Dépenses : " + lignesTrouvees + " lignes trouvées dans " + FICHIER_DEPENSES);
        } catch (Exception e) {
            System.err.println("Erreur calcul dépenses : " + e.getMessage());
        }
        return total;
    }

    /**
     * Récupère le nombre d'enfants par tranche depuis le fichier Dataviz.
     */
    public Map<String, Double> chargerEffectifsParTranche() {
        Map<String, Double> effectifs = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_VOLUMES);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            Sheet s = wb.getSheetAt(0);
            for (int i = LIGNE_DEBUT_VOLUMES; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String code = getValeurTexte(row.getCell(COL_VOL_CODE_TRANCHE));
                if (code == null || code.isEmpty() || code.equals("Total")) continue;

                // On s'assure que c'est bien une tranche (A, B, C...)
                if (code.length() > 3) continue; 

                double nbEnfants = getValeurNumerique(row.getCell(COL_VOL_NB_ENFANTS));
                if (nbEnfants > 0) {
                    effectifs.put(code, nbEnfants);
                }
            }
            System.out.println("[Debug] Effectifs : " + effectifs.size() + " tranches chargées depuis Dataviz");
        } catch (Exception e) {
            System.err.println("Erreur chargement effectifs : " + e.getMessage());
        }
        return effectifs;
    }

    /**
     * Calcule les recettes théoriques annuelles basées sur les effectifs et les tarifs.
     */
    public double calculerRecettesTheoriques(Map<String, Double> effectifs) {
        double totalRecettes = 0;
        // On récupère les tarifs de référence (ceux qu'on a codés en dur)
        Map<String, Tarif> catalogue = new HashMap<>();
        for (Tarif t : DonneesTarifs.chargerTarifsReference()) {
            catalogue.put(t.getTranche(), t);
        }

        for (Map.Entry<String, Double> entry : effectifs.entrySet()) {
            String tranche = entry.getKey();
            double nbEnfants = entry.getValue();
            
            if (catalogue.containsKey(tranche)) {
                double prixRepas = catalogue.get(tranche).getRepas();
                // Formule validée : Nb Enfants * Tarif * 140 jours
                totalRecettes += (nbEnfants * prixRepas * 140);
            }
        }
        return totalRecettes;
    }

    // --- Utilitaires de lecture ---

    private double getValeurNumerique(Cell cell) {
        if (cell == null) return 0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
            if (cell.getCellType() == CellType.STRING) {
                return Double.parseDouble(cell.getStringCellValue().replaceAll("[^0-9,.-]", "").replace(',', '.'));
            }
        } catch (Exception e) { return 0; }
        return 0;
    }

    private String getValeurTexte(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int)cell.getNumericCellValue());
        return "";
    }
}
