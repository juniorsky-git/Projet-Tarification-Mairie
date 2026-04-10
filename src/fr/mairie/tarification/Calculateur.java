package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Moteur de calcul financier de l'application.
 * Cette classe gère l'extraction des données réelles depuis les fichiers 
 * comptables (Ciril) et statistiques (Dataviz).
 */
public class Calculateur {

    // --- Chemins des fichiers de données ---
    private static final String FICHIER_DEPENSES = "Donnees/Autres/CALC DEP.xlsx";
    private static final String FICHIER_VOLUMES  = "Donnees/Autres/Feuille_dataviz .xlsx";

    // --- Index colonnes Dépenses (Ciril) ---
    private static final int COL_DEP_MONTANT_TTC = 7; // Colonne H
    private static final int COL_DEP_SERVICE     = 18; // Colonne S (ex: 2-RE)
    private static final int COL_DEP_ANTENNE     = 19; // Colonne T (ex: CLMICH)

    // --- Index colonnes Volumes (Feuille_dataviz) ---
    private static final int COL_VOL_CODE_TRANCHE = 1; // Colonne B
    private static final int COL_VOL_NB_ENFANTS   = 3; // Colonne D
    private static final int LIGNE_DEBUT_VOLUMES  = 3; // Ligne 4 dans Excel

    /**
     * Calcule le total des dépenses scolaires d'une antenne spécifique.
     * Le calcul filtre les factures pour ne garder que la restauration scolaire "pure".
     * 
     * @param codeAntenne Le code de l'antenne à filtrer (ex: CLMICH)
     * @return Le montant total TTC des dépenses scolaires.
     */
    public double calculerTotalDepenses(String codeAntenne) {
        double total = 0;
        int lignesTrouvees = 0;
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            Sheet s = wb.getSheetAt(0);
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    continue;
                }

                String antenneLigne = getValeurTexte(row.getCell(COL_DEP_ANTENNE));
                String serviceLigne = getValeurTexte(row.getCell(COL_DEP_SERVICE));
                String libelle = getValeurTexte(row.getCell(3)).toUpperCase();

                // Filtrage fin : on exclut les services annexes pour cibler le scolaire pur
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
     * Extrait les effectifs d'enfants par tranche depuis le fichier Dataviz.
     * Gère automatiquement le décalage de la tranche "EXT" et l'arrêt au "Total".
     * 
     * @return Une Map associant le code de tranche au nombre d'enfants.
     */
    public Map<String, Double> chargerEffectifsParTranche() {
        Map<String, Double> effectifs = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_VOLUMES);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            Sheet s = wb.getSheetAt(0);
            for (int i = LIGNE_DEBUT_VOLUMES; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    continue;
                }

                String code = getValeurTexte(row.getCell(COL_VOL_CODE_TRANCHE));
                String desc = getValeurTexte(row.getCell(0));

                // Arrêt de la lecture au mot "Total" pour éviter de polluer les tranches scolaires
                if ("Total".equalsIgnoreCase(code) || "Total".equalsIgnoreCase(desc)) {
                    break;
                }

                if (code == null || code.isEmpty()) {
                    code = desc;
                }

                if (code == null || code.isEmpty()) {
                    continue;
                }
                
                if (code.contains("Restauration") || code.contains("Tranches") || code.length() > 5) {
                    continue;
                }

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

    /**
     * Somme les valeurs d'une Map d'effectifs.
     */
    private double sommeEffectifs(Map<String, Double> map) {
        double total = 0;
        for (double v : map.values()) {
            total += v;
        }
        return total;
    }

    /**
     * Calcule le total des recettes théoriques annuelles.
     * Formule : Somme(Nb Enfants * Prix Repas Tranche * 140 jours).
     * 
     * @param effectifs Map des enfants par tranche.
     * @return Montant total des recettes prévisionnelles.
     */
    public double calculerRecettesTheoriques(Map<String, Double> effectifs) {
        double total = 0;
        List<Tarif> grille = DonneesTarifs.chargerTarifsReference();
        
        for (Map.Entry<String, Double> entry : effectifs.entrySet()) {
            try {
                Tarif t = null;
                for (Tarif ref : grille) {
                    if (ref.getTranche().equalsIgnoreCase(entry.getKey())) {
                        t = ref;
                        break;
                    }
                }
                if (t != null) {
                    total += t.getRepas() * entry.getValue() * 140;
                }
            } catch (Exception e) {}
        }
        return total;
    }

    /**
     * @return Le coût moyen annuel de référence utilisé par la ville.
     */
    public double getCoutMoyenReference() {
        return 4.42;
    }

    // --- Utilitaires de lecture Excel (Sans ternaires) ---

    /**
     * Récupère le texte d'une cellule de manière sécurisée.
     */
    private String getValeurTexte(Cell c) {
        if (c == null) {
            return "";
        } else {
            return c.toString().trim();
        }
    }

    /**
     * Récupère la valeur numérique d'une cellule de manière sécurisée.
     */
    private double getValeurNumerique(Cell c) {
        if (c == null) {
            return 0;
        }
        
        try {
            if (c.getCellType() == CellType.NUMERIC) {
                return c.getNumericCellValue();
            } else {
                return Double.parseDouble(c.toString().trim());
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
