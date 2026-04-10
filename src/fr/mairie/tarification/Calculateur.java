package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
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
                String libelle = getValeurTexte(row.getCell(3)).toUpperCase();

                // On exclut uniquement ce qui est clairement identifié comme "non-scolaire"
                boolean estAnnexe = libelle.contains("ADOS") || libelle.contains("LOISIRS") || libelle.contains("COMMUNAL");

                if ((antenneLigne.equalsIgnoreCase(codeAntenne) || serviceLigne.contains("2-RE")) && !estAnnexe) {
                    double montant = getValeurNumerique(row.getCell(COL_DEP_MONTANT_TTC));
                    total += Math.abs(montant);
                    lignesTrouvees++;
                }
            }
            System.out.println("[Debug] Dépenses Scolaires : " + lignesTrouvees + " lignes (Total: " + String.format("%.2f", total) + " euros)");
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

                // On regarde en priorité la colonne 1, puis la 0 (pour EXT)
                String code = getValeurTexte(row.getCell(COL_VOL_CODE_TRANCHE));
                String desc = getValeurTexte(row.getCell(0));

                // Si on voit "Total", on arrête la lecture pour ne pas prendre les autres services (Ados, etc.)
                if ("Total".equalsIgnoreCase(code) || "Total".equalsIgnoreCase(desc)) break;

                if (code == null || code.isEmpty()) {
                    code = desc;
                }

                if (code == null || code.isEmpty()) continue;
                if (code.contains("Restauration") || code.contains("Tranches") || code.length() > 5) continue;

                double nbEnfants = getValeurNumerique(row.getCell(COL_VOL_NB_ENFANTS));
                if (nbEnfants > 0) {
                    effectifs.put(code, nbEnfants);
                }
            }
            System.out.println("[Debug] Effectifs : " + effectifs.size() + " tranches chargées (" + sommeEffectifs(effectifs) + " enfants)");
        } catch (Exception e) {
            System.err.println("Erreur chargement effectifs : " + e.getMessage());
        }
        return effectifs;
    }

    private double sommeEffectifs(Map<String, Double> map) {
        double total = 0;
        for (double v : map.values()) total += v;
        return total;
    }

    /**
     * Calcule le total des recettes théoriques annuelles basées sur les effectifs et les tarifs.
     */
    public double calculerRecettesTheoriques(Map<String, Double> effectifs) {
        double total = 0;
        List<Tarif> grille = DonneesTarifs.chargerTarifsReference();
        
        for (Map.Entry<String, Double> entry : effectifs.entrySet()) {
            try {
                // Pour chaque tranche, on cherche le tarif repas
                // On utilise un QF arbitraire au milieu de la tranche pour trouver le prix
                Tarif t = null;
                for(Tarif ref : grille) {
                    if(ref.getTranche().equalsIgnoreCase(entry.getKey())) {
                        t = ref; break;
                    }
                }
                if (t != null) {
                    total += t.getRepas() * entry.getValue() * 140;
                }
            } catch (Exception e) {}
        }
        return total;
    }

    public double getCoutMoyenReference() {
        return 4.42;
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
