package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Moteur de calcul financier.
 *
 * Source principale des effectifs et des prix factures :
 * Onglet "Simulation" du fichier CALC DEP.xlsx (index 8).
 * Ce fichier contient par tranche (A a G + EXT) :
 *   - Colonne B : Code de la tranche.
 *   - Colonne C : Prix reel facture par repas pour cette tranche.
 *   - Colonne D : Nombre d enfants inscrits dans cette tranche.
 *   - Colonne E : Cout moyen de reference de la mairie (4.42 euros, fixe).
 *
 * Le fichier Feuille_dataviz.xlsx n est plus utilise pour les effectifs.
 */
public class Calculateur {

    private static final String FICHIER_DEPENSES = "Donnees/Autres/CALC DEP.xlsx";

    // Index de l onglet Simulation dans CALC DEP.xlsx
    private static final int ONGLET_SIMULATION = 8;

    // Colonnes dans l onglet Simulation (index 0-based)
    private static final int COL_SIMU_CODE_TRANCHE = 1; // Colonne B
    private static final int COL_SIMU_PRIX_REEL    = 2; // Colonne C : Prix reel facture
    private static final int COL_SIMU_NB_ENFANTS   = 3; // Colonne D : Nombre d enfants
    private static final int COL_SIMU_COUT_REF     = 4; // Colonne E : Cout de reference (4.42)

    // Colonnes dans l onglet Depenses (index 0-based)
    private static final int COL_DEP_MONTANT_TTC   = 7;  // Colonne H
    private static final int COL_DEP_SERVICE        = 18; // Colonne S
    private static final int COL_DEP_ANTENNE        = 19; // Colonne T
    private static final int COL_DEP_LIBELLE        = 3;  // Colonne D

    /**
     * Lit l onglet Simulation et retourne les effectifs reels par tranche.
     *
     * Structure observee dans le fichier :
     *   - Ligne EXT : code "EXT" en colonne 0 (A), colonne 1 (B) vide.
     *   - Autres tranches : libelle en colonne 0, code (A,B,C...) en colonne 1.
     *   - Colonne 3 (D) : nombre d enfants.
     *   - La ligne "Total" en colonne 0 marque la fin du tableau.
     *
     * @return Map associant le code de tranche (EXT, A, B...) au nombre d enfants.
     */
    public Map<String, Double> chargerEffectifsDepuisSimulation() {
        Map<String, Double> effectifs = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(ONGLET_SIMULATION);
            for (int i = 6; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String col0 = getValeurTexte(row.getCell(0));
                String col1 = getValeurTexte(row.getCell(COL_SIMU_CODE_TRANCHE));

                // Arret sur la ligne Total
                if (col0.equalsIgnoreCase("Total")) break;

                // Determination du code de tranche
                String codeTranche = "";
                if (col0.equalsIgnoreCase("EXT")) {
                    codeTranche = "EXT";
                } else if (!col1.isEmpty()) {
                    codeTranche = col1;
                }

                if (codeTranche.isEmpty()) continue;

                double nbEnfants = getValeurNumerique(row.getCell(COL_SIMU_NB_ENFANTS));
                if (nbEnfants > 0) {
                    effectifs.put(codeTranche, nbEnfants);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture Simulation : " + e.getMessage());
        }
        return effectifs;
    }

    /**
     * Lit l onglet Simulation et calcule les recettes theoriques.
     * La recette par tranche est : Prix reel facture (col C) x Nb enfants (col D) x 140 jours.
     *
     * @return Total des recettes theoriques annuelles.
     */
    public double calculerRecettesDepuisSimulation() {
        double total = 0;
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(ONGLET_SIMULATION);
            for (int i = 6; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String col0 = getValeurTexte(row.getCell(0));
                String col1 = getValeurTexte(row.getCell(COL_SIMU_CODE_TRANCHE));

                if (col0.equalsIgnoreCase("Total")) break;

                boolean estTranche = col0.equalsIgnoreCase("EXT") || !col1.isEmpty();
                if (!estTranche) continue;

                double prixReel  = getValeurNumerique(row.getCell(COL_SIMU_PRIX_REEL));
                double nbEnfants = getValeurNumerique(row.getCell(COL_SIMU_NB_ENFANTS));

                total += prixReel * nbEnfants * 140;
            }
        } catch (Exception e) {
            System.err.println("Erreur calcul recettes : " + e.getMessage());
        }
        return total;
    }

    /**
     * Retourne le cout de reference de la mairie (4.42 euros) depuis la colonne E.
     * Ce chiffre est fixe dans le fichier et valide par le pole financier.
     *
     * @return Cout moyen de reference par repas.
     */
    public double getCoutMoyenReference() {
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(ONGLET_SIMULATION);
            Row row = s.getRow(7); // Premiere ligne de donnees (tranche A)
            if (row != null) {
                return getValeurNumerique(row.getCell(COL_SIMU_COUT_REF));
            }
        } catch (Exception e) {}
        return 4.42; // Valeur de secours
    }

    /**
     * Calcule les depenses reelles pour un pole donne.
     *
     * @param antennaPrecise Code de l antenne (ex : RESTMICH, RESTCA).
     * @param serviceType    Code du service (ex : 2-RE) ou null.
     * @param motCleLibelle  Mot-cle obligatoire dans le libelle (ex : ADOS) ou null.
     * @param exclusions     Mots-cles a exclure du libelle ou null.
     * @return Total des depenses TTC.
     */
    public double calculerDepensesPole(String antennaPrecise, String serviceType, String motCleLibelle, String[] exclusions) {
        double total = 0;
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0); // Onglet principal des depenses
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String ant = getValeurTexte(row.getCell(COL_DEP_ANTENNE));
                String ser = getValeurTexte(row.getCell(COL_DEP_SERVICE));
                String lib = getValeurTexte(row.getCell(COL_DEP_LIBELLE)).toUpperCase();

                boolean matchBase = false;
                if (antennaPrecise != null && ant.equalsIgnoreCase(antennaPrecise)) matchBase = true;
                if (serviceType != null && ser.contains(serviceType)) matchBase = true;

                if (!matchBase) continue;

                if (motCleLibelle != null && !lib.contains(motCleLibelle.toUpperCase())) continue;

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
        } catch (Exception e) {}
        return total;
    }

    /**
     * Retourne la valeur texte d une cellule, ou une chaine vide si la cellule est nulle.
     */
    private String getValeurTexte(Cell c) {
        if (c == null) return "";
        return c.toString().trim();
    }

    /**
     * Retourne la valeur numerique d une cellule, ou 0 si la cellule est nulle ou non numerique.
     */
    private double getValeurNumerique(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            return Double.parseDouble(c.toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
