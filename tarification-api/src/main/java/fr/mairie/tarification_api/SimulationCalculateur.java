package fr.mairie.tarification_api;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lecteur du fichier CALC DEP(4).csv — section Restauration.
 *
 * Le fichier CSV utilise le point-virgule (;) comme séparateur et le format
 * français pour les nombres ("1 234,56 €", "85,01%").
 *
 * Structure de la section Restauration (1-indexé) :
 * - Ligne 1-3  : vides
 * - Ligne 4    : en-tête des colonnes
 * - Ligne 5    : vide
 * - Ligne 6-15 : données par tranche (EXT, A, B, B2, C, D, E, F, F2, G)
 * - Ligne 16   : ligne Total (ignorée — col 1 vide)
 *
 * Colonnes :
 * Col 0 = libellé tranche | Col 1 = code tranche | Col 2 = prix facturé
 * Col 3 = nombre enfants  | Col 4 = coût moyen   | Col 5 = dépense annuelle
 * Col 6 = recette annuelle | Col 7 = écart        | Col 8 = taux de couverture
 *
 * @author Séri-khane YOLOU
 * @version 2.0
 */
public class SimulationCalculateur {

    /** Chemin vers le fichier CSV de simulation. */
    private final String fichierCsv;

    /** Séparateur de colonnes du CSV. */
    private static final String SEPARATEUR = ";";

    /**
     * Construit un calculateur pointant vers le fichier CSV indiqué.
     *
     * @param fichierCsv Chemin relatif ou absolu vers CALC DEP(4).csv.
     */
    public SimulationCalculateur(String fichierCsv) {
        this.fichierCsv = fichierCsv;
    }

    /**
     * Lit la section Restauration du CSV et retourne la liste des lignes
     * budgétaires par tranche tarifaire.
     *
     * Les lignes dont le code tranche (col 1) est vide sont ignorées
     * (ligne Total, lignes vides, autres sections du fichier).
     *
     * @return Liste de {@link SimulationLigne} extraites du CSV, jamais null.
     */
    public List<SimulationLigne> lireSimulationRestauration() {
        List<SimulationLigne> lignes = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(fichierCsv);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {

            String ligne;
            int numeroLigne = 0;

            while ((ligne = reader.readLine()) != null) {
                numeroLigne++;

                // Les données de Restauration commencent à la ligne 6
                // et se terminent à la ligne 15 (ligne 16 = Total, ignorée)
                if (numeroLigne < 6 || numeroLigne > 16) continue;

                String[] cols = ligne.split(SEPARATEUR, -1);

                // Col 1 = code tranche : vide → ligne Total ou vide → on ignore
                if (cols.length < 2) continue;
                String codeTranche = cols[1].trim();
                if (codeTranche.isEmpty()) continue;

                SimulationLigne s = new SimulationLigne();
                s.tranche         = getTexte(cols, 0);
                s.codeTranche     = codeTranche;
                s.prixFacture     = getNombre(cols, 2);
                s.nombreEnfants   = getNombre(cols, 3);
                s.coutMoyen       = getNombre(cols, 4);
                s.depenseAnnuelle = getNombre(cols, 5);
                s.recetteAnnuelle = getNombre(cols, 6);
                s.ecart           = getNombre(cols, 7);
                s.tauxCouverture  = getNombre(cols, 8);

                lignes.add(s);
            }

        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur lecture CSV : " + e.getMessage());
            e.printStackTrace();
        }

        return lignes;
    }

    /**
     * Lit le nombre total d'enfants de la restauration depuis la ligne "Total"
     * de la section Simulation (ligne 16 du CSV, col 3).
     *
     * @return Nombre total d'enfants (ex: 1128), ou 0 si non trouvé.
     */
    public double lireNombreEnfantsTotal() {
        try (FileInputStream fis = new FileInputStream(fichierCsv);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {

            String ligne;
            int numeroLigne = 0;
            while ((ligne = reader.readLine()) != null) {
                numeroLigne++;
                if (numeroLigne == 16) {
                    // Ligne : Total;;;1128;;698 006,40 €;...
                    String[] cols = ligne.split(SEPARATEUR, -1);
                    return getNombre(cols, 3); // col 3 = nombre total enfants
                }
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur lecture nombre enfants : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Lit le détail des dépenses réelles de la section Restauration.
     *
     * Extrait la ligne "Total général" (ligne 33 du CSV) qui contient
     * la ventilation des dépenses par nature et le total général.
     *
     * Structure de la ligne (séparateur ;) :
     * Col 1 = "Total général" | Col 2 = Scolarest | Col 3 = Personnel
     * Col 4 = Alimentation    | Col 5 = Eau       | Col 6 = Électricité
     * Col 7 = Gaz             | Col 8 = TOTAL
     *
     * @return Map ordonnée {nature → montant}, incluant la clé "TOTAL"
     *         avec le montant total des dépenses réelles.
     */
    public java.util.Map<String, Double> lireDepensesReellesRestauration() {
        java.util.Map<String, Double> detail = new java.util.LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(fichierCsv);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {

            String ligne;
            int numeroLigne = 0;
            while ((ligne = reader.readLine()) != null) {
                numeroLigne++;

                // Ligne 33 : ;Total général;713 752,47;1 051 019,64;...
                if (numeroLigne == 33) {
                    String[] cols = ligne.split(SEPARATEUR, -1);
                    detail.put("Scolarest (prestations)",  getNombre(cols, 2));
                    detail.put("Personnel",                getNombre(cols, 3));
                    detail.put("Alimentation",             getNombre(cols, 4));
                    detail.put("Eau",                      getNombre(cols, 5));
                    detail.put("Electricite",              getNombre(cols, 6));
                    detail.put("Gaz",                      getNombre(cols, 7));
                    detail.put("TOTAL",                    getNombre(cols, 8));
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur lecture dépenses réelles : " + e.getMessage());
        }

        return detail;
    }

    /**
     * Extrait une valeur textuelle depuis un tableau de colonnes CSV.
     *
     * @param cols  Tableau des colonnes de la ligne.
     * @param index Index de la colonne à lire.
     * @return La valeur trimée, ou une chaîne vide si hors limites.
     */
    private String getTexte(String[] cols, int index) {
        if (index >= cols.length) return "";
        return cols[index].trim();
    }

    /**
     * Extrait et parse une valeur numérique depuis un tableau de colonnes CSV.
     *
     * Gère le format français : "6 806,80 €", "-53 816,00 €", "85,01%", "429"
     *
     * @param cols  Tableau des colonnes de la ligne.
     * @param index Index de la colonne à lire.
     * @return La valeur numérique parsée, ou 0 en cas d'absence ou d'erreur.
     */
    private double getNombre(String[] cols, int index) {
        if (index >= cols.length) return 0;
        String val = cols[index].trim();
        if (val.isEmpty()) return 0;

        try {
            // Nettoyage du format français : "6 806,80 €" → "6806.80"
            val = val
                    .replace("\u00A0", "") // espace insécable
                    .replace(" ", "")      // espace (séparateur milliers)
                    .replace("€", "")      // symbole euro
                    .replace("%", "")      // symbole pourcentage
                    .replace(",", ".");     // virgule décimale → point
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // =========================================================================
    // HELPER GÉNÉRIQUE — lecture d'une ligne "Total général" du CSV
    // =========================================================================

    /**
     * Méthode utilitaire : lit la ligne {@code numeroLigne} du CSV et construit
     * une map ordonnée {étiquette → montant} pour le tableau de bord.
     *
     * Les entrées dont le montant est 0 sont exclues du détail (sauf "TOTAL").
     *
     * @param numeroLigne  Numéro de ligne dans le CSV (base 1).
     * @param etiquettes   Noms des natures de dépenses (dans l'ordre des colonnes).
     * @param colonnes     Index de colonnes correspondants aux étiquettes.
     * @param colonneTotal Index de la colonne contenant le total général.
     * @return Map {libellé → montant} avec la clé "TOTAL" pour le total global.
     */
    private java.util.Map<String, Double> lireTotalGeneral(
            int numeroLigne, String[] etiquettes, int[] colonnes, int colonneTotal) {

        java.util.Map<String, Double> detail = new java.util.LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(fichierCsv);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {

            String ligne;
            int num = 0;
            while ((ligne = reader.readLine()) != null) {
                if (++num == numeroLigne) {
                    String[] cols = ligne.split(SEPARATEUR, -1);
                    for (int i = 0; i < etiquettes.length; i++) {
                        double val = getNombre(cols, colonnes[i]);
                        if (val != 0) detail.put(etiquettes[i], val); // on exclut les 0
                    }
                    detail.put("TOTAL", getNombre(cols, colonneTotal));
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur lecture ligne "
                    + numeroLigne + " : " + e.getMessage());
        }

        return detail;
    }

    // =========================================================================
    // DÉPENSES PAR PÔLE (extraites du CSV CALC DEP(4).csv)
    // =========================================================================

    /**
     * Accueil de Loisirs — Total général (ligne 46 du CSV).
     *
     * Colonnes : Personnel(2) | Matériel(3) | Fournitures pédago.(4)
     *            Matériel sportif(5) | Prestations(6) | Alimentation(7)
     *            Transport(8) | Droits d'entrée(9) | Electricité(10)
     *            Eau(11) | Restauration(12) | Autres(13) | TOTAL(15)
     *
     * @return Map {nature → montant} + clé "TOTAL" = 1 596 116,14 €
     */
    public java.util.Map<String, Double> lireDepensesAccueilLoisirs() {
        return lireTotalGeneral(46,
                new String[]{
                        "Personnel", "Materiel", "Fournitures pedagogiques",
                        "Materiel sportif", "Prestations (spectacles)",
                        "Alimentation", "Transport", "Droits d'entree",
                        "Electricite", "Eau", "Restauration", "Autres"
                },
                new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13},
                15);
    }

    /**
     * Études surveillées — Total général (ligne 60 du CSV).
     *
     * Colonnes : Personnel(2) | Fournitures scolaires(3) | Matériel(4) | TOTAL(5)
     *
     * @return Map {nature → montant} + clé "TOTAL" = 58 598,14 €
     */
    public java.util.Map<String, Double> lireDepensesEtudesSurveillees() {
        return lireTotalGeneral(60,
                new String[]{"Personnel", "Fournitures scolaires", "Materiel"},
                new int[]{2, 3, 4},
                5);
    }

    /**
     * Espace Ados — Total général (ligne 75 du CSV).
     *
     * Colonnes : Personnel(2) | Electricité(3) | Gaz(4) | Eau(5)
     *            Matériel(6) | Fournitures(7) | Petit matériel(8)
     *            Alimentation(9) | Autres(10) | Transport(11) | Droits d'entrée(12)
     *            TOTAL(13)
     *
     * @return Map {nature → montant} + clé "TOTAL" = 140 775,54 €
     */
    public java.util.Map<String, Double> lireDepensesEspaceAdos() {
        return lireTotalGeneral(75,
                new String[]{
                        "Personnel", "Electricite", "Gaz", "Eau",
                        "Materiel", "Fournitures", "Petit materiel",
                        "Alimentation", "Autres", "Transport", "Droits d'entree"
                },
                new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
                13);
    }

    /**
     * Séjours — Total général (ligne 95 du CSV).
     *
     * Colonnes : Personnel(2) | Transport bus(3) | Péage/Parking(4)
     *            Hébergement(5) | Restauration(6) | Matériel(7)
     *            Fournitures(8) | Autres(9) | TOTAL(10)
     *
     * @return Map {nature → montant} + clé "TOTAL" = 107 127,71 €
     */
    public java.util.Map<String, Double> lireDepensesSejours() {
        return lireTotalGeneral(95,
                new String[]{
                        "Personnel", "Transport (bus/mini bus)", "Peage/Parking",
                        "Hebergement (centre vacances)", "Restauration",
                        "Materiel", "Fournitures", "Autres"
                },
                new int[]{2, 3, 4, 5, 6, 7, 8, 9},
                10);
    }

    /**
     * Accueil périscolaire — Total général (ligne 112 du CSV).
     *
     * Colonnes : Personnel(2) | Fournitures(3) | Alimentation(4)
     *            Eau(5) | Electricité(6) | Gaz(7) | TOTAL(8)
     *
     * @return Map {nature → montant} + clé "TOTAL" = 658 856,78 €
     */
    public java.util.Map<String, Double> lireDepensesAccueilPeriscolaire() {
        return lireTotalGeneral(112,
                new String[]{
                        "Personnel", "Fournitures", "Alimentation",
                        "Eau", "Electricite", "Gaz"
                },
                new int[]{2, 3, 4, 5, 6, 7},
                8);
    }
}
