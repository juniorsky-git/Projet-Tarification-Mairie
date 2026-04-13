package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Moteur de calcul financier.
 *
 * Source principale des effectifs et des prix factures :
 * Onglet "Simulation" du fichier CALC DEP.xlsx (index 8).
 * Ce fichier contient par tranche (A a G + EXT) :
 * - Colonne B : Code de la tranche.
 * - Colonne C : Prix reel facture par repas pour cette tranche.
 * - Colonne D : Nombre d enfants inscrits dans cette tranche.
 * - Colonne E : Cout moyen de reference de la mairie (4.42 euros, fixe).
 *
 * Le fichier Feuille_dataviz.xlsx n est plus utilise pour les effectifs.
 */
public class Calculateur {

    private static final String FICHIER_DEPENSES = "Donnees/Autres/CALC DEP.xlsx";

    // Index de l onglet Simulation dans CALC DEP.xlsx
    private static final int ONGLET_SIMULATION = 8;

    // Colonnes dans l onglet Simulation (index 0-based)
    private static final int COL_SIMU_CODE_TRANCHE = 1; // Colonne B
    private static final int COL_SIMU_PRIX_REEL = 2; // Colonne C : Prix reel facture
    private static final int COL_SIMU_NB_ENFANTS = 3; // Colonne D : Nombre d enfants
    private static final int COL_SIMU_COUT_REF = 4; // Colonne E : Cout de reference (4.42)
    private static final int COL_SIMU_DEPENSES_REELLES = 17; // Colonne R : depenses reelles accueil loisirs

    // Colonnes dans l onglet Depenses (index 0-based)
    private static final int COL_DEP_MONTANT_TTC = 7; // Colonne H
    private static final int COL_DEP_SERVICE = 18; // Colonne S
    private static final int COL_DEP_ANTENNE = 19; // Colonne T
    private static final int COL_DEP_LIBELLE = 3; // Colonne D

    /**
     * Structure de donnees regroupant les resultats de l onglet Simulation.
     */
    public static class SimulationData {
        private final Map<String, Double> effectifs = new HashMap<>();
        private double recettesTheoriques = 0;
        private double coutMoyenReference = 4.42;

        public Map<String, Double> getEffectifs() {
            return effectifs;
        }

        public double getRecettesTheoriques() {
            return recettesTheoriques;
        }

        public double getCoutMoyenReference() {
            return coutMoyenReference;
        }
    }

    /**
     * Lit l'onglet Simulation une seule fois pour extraire toutes les donnees.
     */
    public SimulationData chargerDonneesSimulation() {
        SimulationData data = new SimulationData();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                LogService.log("Onglet 'Simulation' introuvable dans " + FICHIER_DEPENSES);
                return data;
            }

            // 1. Cout de reference (fixe ligne 7, col E)
            Row refRow = s.getRow(7);
            if (refRow != null) {
                data.coutMoyenReference = getValeurNumerique(refRow.getCell(COL_SIMU_COUT_REF));
            }

            // 2. Parours des tranches
            for (int i = 6; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    continue;
                }

                String col0 = getValeurTexte(row.getCell(0));
                String col1 = getValeurTexte(row.getCell(COL_SIMU_CODE_TRANCHE));

                if (col0.equalsIgnoreCase("Total")) {
                    break;
                }

                String codeTranche = "";
                if (col0.equalsIgnoreCase("EXT")) {
                    codeTranche = "EXT";
                } else if (!col1.isEmpty()) {
                    codeTranche = col1;
                }

                if (codeTranche.isEmpty()) {
                    continue;
                }

                double prixReel = getValeurNumerique(row.getCell(COL_SIMU_PRIX_REEL));
                double nbEnfants = getValeurNumerique(row.getCell(COL_SIMU_NB_ENFANTS));

                if (nbEnfants > 0) {
                    data.effectifs.put(codeTranche, nbEnfants);
                    data.recettesTheoriques += prixReel * nbEnfants * 140;
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur critique lors du chargement de la Simulation", e);
        }
        return data;
    }

    /**
     * Calcule le total des depenses reelles accueil loisirs (somme de tous les
     * segments).
     */
    public double calculerTotalDepensesLoisirs() {
        double total = 0;
        Map<String, Double> details = calculerDepensesReellesAccueilLoisirsParSegment();
        for (Double montant : details.values()) {
            total += montant;
        }
        return total;
    }

    public Map<String, Double> calculerDepensesReellesAccueilLoisirsParSegment() {
        Map<String, Double> totaux = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                LogService.log("Onglet 'Simulation' introuvable");
                return totaux;
            }

            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    continue;
                }

                String ligne = getRowTexte(row).toUpperCase();
                String segment = determinerSegmentAccueilLoisirs(ligne);
                if (segment != null) {
                    double montant = Math.abs(getValeurNumerique(row.getCell(COL_SIMU_DEPENSES_REELLES)));
                    if (montant > 0) {
                        totaux.put(segment, totaux.getOrDefault(segment, 0.0) + montant);
                    }
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur lecture segments Loisirs", e);
        }
        return totaux;
    }

    private String determinerSegmentAccueilLoisirs(String ligne) {
        if (ligne.contains("MDJ")) {
            return "MDJ";
        }
        if (ligne.contains("CLGAV")) {
            return "CLGAV";
        }
        if (ligne.contains("CLJP1")) {
            return "CLJP1";
        }
        if (ligne.contains("CLLMICH")) {
            return "CLLMICH";
        }
        if (ligne.contains("P'TIT") || ligne.contains("PTIT") || ligne.contains("PRINCE")) {
            return "P'TIT PRINCE";
        }
        return null;
    }

    /**
     * Retourne le texte complet d une ligne pour recherche de mots-cles.
     */
    private String getRowTexte(Row row) {
        StringBuilder sb = new StringBuilder();
        for (Cell c : row) {
            if (c != null) {
                sb.append(c.toString()).append(' ');
            }
        }
        return sb.toString().trim();
    }

    /**
     * Calcule les depenses reelles pour un pole donne de maniere universelle.
     *
     * @param antenne   Code de l antenne (ex : RESTMICH) ou null.
     * @param service   Code du service (ex : 2-RE) ou null.
     * @param inclusion Mot-cle obligatoire dans le libelle ou null.
     * @param exclusions Mots-cles a exclure du libelle ou null.
     * @return Total des depenses TTC.
     */
    public double calculerDepenses(String antenne, String service, String inclusion, String[] exclusions) {
        double total = 0;
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
                Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0); // Onglet principal des depenses
            if (s == null) {
                LogService.log("Onglet 'Depenses restau 2025' introuvable");
                return 0;
            }

            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null)
                    continue;

                String ant = getValeurTexte(row.getCell(COL_DEP_ANTENNE));
                String ser = getValeurTexte(row.getCell(COL_DEP_SERVICE));
                String lib = getValeurTexte(row.getCell(COL_DEP_LIBELLE)).toUpperCase();

                // Filtrage Antenne/Service
                boolean match = false;
                if (antenne != null && ant.equalsIgnoreCase(antenne)) {
                    match = true;
                }
                if (service != null && ser.contains(service)) {
                    match = true;
                }

                if (!match) {
                    continue;
                }

                // Filtrage Inclusion
                if (inclusion != null && !lib.contains(inclusion.toUpperCase())) {
                    continue;
                }

                // Filtrage Exclusions
                boolean exclu = false;
                if (exclusions != null) {
                    for (String ex : exclusions) {
                        if (lib.contains(ex.toUpperCase())) {
                            exclu = true;
                            break;
                        }
                    }
                }

                if (!exclu) {
                    total += Math.abs(getValeurNumerique(row.getCell(COL_DEP_MONTANT_TTC)));
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur calcul depenses generique", e);
        }
        return total;
    }

    /**
     * Retourne le detail des depenses reelles pour le pole Ados.
     * Source : Onglet Simulation, Ligne 74 (Titres) et 75 (Valeurs).
     * @return Map ordonnee (Libelle -> Montant)
     */
    public Map<String, Double> getDepensesAdosDetaillees() {
        Map<String, Double> details = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
                Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                return details;
            }
            Row rowHeaders = s.getRow(73); // Ligne 74
            Row rowValues = s.getRow(74);  // Ligne 75

            if (rowHeaders == null || rowValues == null) {
                return details;
            }

            int nbCols = Math.min(rowHeaders.getLastCellNum(), rowValues.getLastCellNum());
            for (int j = 2; j < nbCols; j++) {
                Cell cHeader = rowHeaders.getCell(j);
                Cell cValue = rowValues.getCell(j);
                
                String label = (cHeader == null) ? "Inconnu" : cHeader.toString().replace("\n", " ").trim();
                double montant = Math.abs(getValeurNumerique(cValue));

                if (!label.isEmpty() && montant > 0 && !label.equalsIgnoreCase("TOTAL")) {
                    details.put(label, montant);
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur lecture detail depenses Ados", e);
        }
        return details;
    }

    /**
     * Retourne la valeur texte d une cellule, ou une chaine vide si la cellule est
     * nulle.
     */
    private String getValeurTexte(Cell c) {
        if (c == null)
            return "";
        return c.toString().trim();
    }

    /**
     * Retourne la valeur numerique d une cellule, ou 0 si la cellule est nulle ou
     * non numerique.
     */
    private double getValeurNumerique(Cell c) {
        if (c == null)
            return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC)
                return c.getNumericCellValue();
            if (c.getCellType() == CellType.FORMULA) {
                try {
                    return c.getNumericCellValue(); // Rcupre le rsultat calcul mis en cache
                } catch (Exception e) {
                    // Si pas numérique, on tombe dans le toString
                }
            }
            String s = c.toString().trim().replace(",", ".");
            if (s.isEmpty())
                return 0;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
