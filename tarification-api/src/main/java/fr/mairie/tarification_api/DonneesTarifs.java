package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entrepot de donnees statiques et moteur de chargement pour les tarifs.
 * 
 * Cette classe permet de gérer soit la grille de référence 2025 "en dur",
 * soit de charger dynamiquement n'importe quelle grille annuelle via Excel.
 * 
 * @author Séri-khane YOLOU
 * @version 2.0
 */
public class DonneesTarifs {

    // --- Constantes techniques des services ---
    public static final String REPAS = "REPAS";
    public static final String ACCUEIL_JOURNEE = "ACCUEIL_JOURNEE";
    public static final String ACCUEIL_DEMI_REPAS = "ACCUEIL_DEMI_REPAS";
    public static final String PERISCOLAIRE_MATIN_SOIR = "PERISCOLAIRE_MATIN_SOIR";
    public static final String PERISCOLAIRE_MATIN_OU_SOIR = "PERISCOLAIRE_MATIN_OU_SOIR";
    public static final String ETUDES_FORFAIT_MENSUEL = "ETUDES_FORFAIT_MENSUEL";
    public static final String ETUDES_DEMI_FORFAIT = "ETUDES_DEMI_FORFAIT";
    public static final String ADOS_VAC_JOURNEE_REPAS = "ADOS_VAC_JOURNEE_REPAS";
    public static final String ADOS_VAC_JOURNEE_SANS = "ADOS_VAC_JOURNEE_SANS";
    public static final String ADOS_VAC_DEMI_REPAS = "ADOS_VAC_DEMI_REPAS";
    public static final String ADOS_VAC_DEMI_SANS = "ADOS_VAC_DEMI_SANS";
    public static final String ADOS_SORTIE_DEMI = "ADOS_SORTIE_DEMI";
    public static final String ADOS_SORTIE_JOURNEE = "ADOS_SORTIE_JOURNEE";
    public static final String TARIF_POST_ETUDES = "TARIF_POST_ETUDES";
    public static final String CLASSE_DECOUVERTE = "CLASSE_DECOUVERTE";
    public static final String SEJOUR_5_JOURS = "SEJOUR_5_JOURS";
    public static final String SEJOUR_6_JOURS = "SEJOUR_6_JOURS";

    /**
     * Charge une grille tarifaire depuis un fichier Excel de format "Standard" (ex:
     * Grille 2024).
     * Ce format possede les tranches en colonnes 1 et les prix de 2 a 9.
     * 
     * @param cheminFichier Chemin vers le fichier Excel.
     * @return Liste de Tarifs charges.
     */
    public static List<Tarif> chargerGrilleStandard(String cheminFichier) {
        List<Tarif> tarifs = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(cheminFichier);
                Workbook wb = WorkbookFactory.create(fis)) {

            // --- BLINDAGE 1 : Le fichier doit contenir au moins une feuille ---
            if (wb.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Le fichier Excel est vide (aucune feuille detectee).");
            }

            Sheet s = wb.getSheetAt(0);

            // --- BLINDAGE 2 : La feuille doit contenir assez de lignes (entete + donnees)
            // ---
            if (s.getLastRowNum() < 5) {
                throw new IllegalArgumentException(
                        "Structure invalide : moins de 6 lignes detectees. Verifiez que l'entete et les tranches QF sont presents.");
            }

            // --- BLINDAGE 3 : Verifier la presence d'au moins une colonne tarifaire ---
            Row entete = s.getRow(4); // Ligne d'entete (index 4, soit ligne 5 Excel)
            if (entete == null || entete.getLastCellNum() < 3) {
                throw new IllegalArgumentException(
                        "Structure invalide : l'en-tete de colonnes est absente ou incomplète (minimum 3 colonnes attendues).");
            }

            // --- DETECTION DYNAMIQUE DES COLONNES ---
            Map<String, Integer> mapping = scannerEntetes(s);

            // --- BLINDAGE 4 : Verifier que les colonnes metier essentielles sont presentes
            // ---
            // On choisit REPAS et ACCUEIL_JOURNEE comme colonnes "pivot" indispensables.
            // Si ces deux colonnes sont absentes du mapping, le fichier n'est pas une
            // grille tarifaire valide.
            List<String> colonnesManquantes = new ArrayList<>();
            if (!mapping.containsKey(REPAS)) {
                colonnesManquantes.add(REPAS);
            }
            if (!mapping.containsKey(ACCUEIL_JOURNEE)) {
                colonnesManquantes.add(ACCUEIL_JOURNEE);
            }
            if (!colonnesManquantes.isEmpty()) {
                throw new IllegalArgumentException(
                        "Colonne(s) metier introuvable(s) dans l'en-tete : " + colonnesManquantes
                                + ". Verifiez que la ligne d'entete contient bien les noms de services.");
            }

            // On commence a la ligne 6 (index 5) jusqu'a la fin
            for (int i = 5; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null)
                    continue;

                String texteQF = getValeurTexte(row.getCell(0));
                String tranche = getValeurTexte(row.getCell(1));

                if (tranche.isEmpty())
                    continue;

                double[] bornes = extraireBornesQF(texteQF, tranche);
                double qfMin = bornes[0];
                double qfMax = bornes[1];

                Map<String, Double> prix = new HashMap<>();

                // On utilise la detection dynamique, avec les colonnes 2024 en fallback par
                // securite
                prix.put(REPAS, getValeurDeColonne(row, mapping, REPAS, 2));
                prix.put(ACCUEIL_JOURNEE, getValeurDeColonne(row, mapping, ACCUEIL_JOURNEE, 3));
                prix.put(ACCUEIL_DEMI_REPAS, getValeurDeColonne(row, mapping, ACCUEIL_DEMI_REPAS, 4));
                prix.put(PERISCOLAIRE_MATIN_SOIR, getValeurDeColonne(row, mapping, PERISCOLAIRE_MATIN_SOIR, 5));
                prix.put(PERISCOLAIRE_MATIN_OU_SOIR, getValeurDeColonne(row, mapping, PERISCOLAIRE_MATIN_OU_SOIR, 6));
                prix.put(ETUDES_FORFAIT_MENSUEL, getValeurDeColonne(row, mapping, ETUDES_FORFAIT_MENSUEL, 7));
                prix.put(ETUDES_DEMI_FORFAIT, getValeurDeColonne(row, mapping, ETUDES_DEMI_FORFAIT, 8));
                prix.put(TARIF_POST_ETUDES, getValeurDeColonne(row, mapping, TARIF_POST_ETUDES, 9));
                prix.put(CLASSE_DECOUVERTE, getValeurDeColonne(row, mapping, CLASSE_DECOUVERTE, 10));

                // Ados & Sejours
                prix.put(ADOS_VAC_JOURNEE_REPAS, getValeurDeColonne(row, mapping, ADOS_VAC_JOURNEE_REPAS, 11));
                prix.put(ADOS_VAC_JOURNEE_SANS, getValeurDeColonne(row, mapping, ADOS_VAC_JOURNEE_SANS, 12));
                prix.put(ADOS_VAC_DEMI_REPAS, getValeurDeColonne(row, mapping, ADOS_VAC_DEMI_REPAS, 13));
                prix.put(ADOS_VAC_DEMI_SANS, getValeurDeColonne(row, mapping, ADOS_VAC_DEMI_SANS, 14));
                prix.put(ADOS_SORTIE_DEMI, getValeurDeColonne(row, mapping, ADOS_SORTIE_DEMI, 15));
                prix.put(ADOS_SORTIE_JOURNEE, getValeurDeColonne(row, mapping, ADOS_SORTIE_JOURNEE, 16));

                prix.put(SEJOUR_5_JOURS, getValeurDeColonne(row, mapping, SEJOUR_5_JOURS, 17));
                prix.put(SEJOUR_6_JOURS, getValeurDeColonne(row, mapping, SEJOUR_6_JOURS, 19));

                tarifs.add(new Tarif(tranche, qfMin, qfMax, prix));
            }
        } catch (IllegalArgumentException e) {
            throw e; // Laisser l'UI attraper cette structure pour afficher le message dédié.
        } catch (Exception e) {
            LogService.error("Erreur de lecture Grille Standard", e);
        }
        return tarifs;
    }

    /**
     * Analyse les premieres lignes de la feuille pour detecter dynamiquement les
     * colonnes des services.
     */
    private static Map<String, Integer> scannerEntetes(Sheet s) {
        Map<String, Integer> mapping = new HashMap<>();

        // On construit une "super-ligne" d'en-tête en concaténant les lignes 1, 2 et 3
        // Cela permet de garder le contexte du pôle (ex: RESTAURATION vs ADOS)
        int maxCols = 30;
        String[] fullHeaders = new String[maxCols];
        for (int c = 0; c < maxCols; c++)
            fullHeaders[c] = "";

        for (int i = 1; i <= 3; i++) {
            Row row = s.getRow(i);
            if (row == null)
                continue;
            for (int c = 0; c < maxCols; c++) {
                Cell cell = row.getCell(c);
                if (cell != null) {
                    fullHeaders[c] += " " + cell.toString().toLowerCase();
                }
            }
        }

        // On parcourt les en-têtes consolidés pour le mapping
        for (int c = 0; c < maxCols; c++) {
            String label = fullHeaders[c].trim();
            if (label.isEmpty())
                continue;

            // --- REPAS SCOLAIRE (Doit être dans le pôle RESTAURATION ou être seul) ---
            if (label.contains("repas")
                    && (label.contains("restauration") || (!label.contains("ados") && !label.contains("loisir")))) {
                if (!mapping.containsKey(REPAS))
                    mapping.put(REPAS, c);
            }

            // --- LOISIRS ---
            if (label.contains("loisir")) {
                if (label.contains("journée") || label.contains("journee"))
                    mapping.put(ACCUEIL_JOURNEE, c);
                if (label.contains("1/2"))
                    mapping.put(ACCUEIL_DEMI_REPAS, c);
            }

            // --- PERISCOLAIRE ---
            if (label.contains("periscola") || label.contains("périscola")) {
                if (label.contains("et") || label.contains("matin et soir"))
                    mapping.put(PERISCOLAIRE_MATIN_SOIR, c);
                if (label.contains("ou") || label.contains("matin ou soir"))
                    mapping.put(PERISCOLAIRE_MATIN_OU_SOIR, c);
            }

            // --- ETUDES ---
            if (label.contains("etude") || label.contains("étude")) {
                if (label.contains("mensuel") || label.contains("forfait"))
                    mapping.put(ETUDES_FORFAIT_MENSUEL, c);
                if (label.contains("1/2"))
                    mapping.put(ETUDES_DEMI_FORFAIT, c);
            }

            // --- POST ETUDES & DECOUVERTE ---
            if (label.contains("post") && (label.contains("etude") || label.contains("étude")))
                mapping.put(TARIF_POST_ETUDES, c);
            if (label.contains("decouverte") || label.contains("découverte"))
                mapping.put(CLASSE_DECOUVERTE, c);

            // --- ESPACE ADOS ---
            if (label.contains("ados")) {
                if (label.contains("vacances")) {
                    if (label.contains("repas") && !label.contains("sans")) {
                        if (label.contains("1/2"))
                            mapping.put(ADOS_VAC_DEMI_REPAS, c);
                        else
                            mapping.put(ADOS_VAC_JOURNEE_REPAS, c);
                    }
                    if (label.contains("sans")) {
                        if (label.contains("1/2"))
                            mapping.put(ADOS_VAC_DEMI_SANS, c);
                        else
                            mapping.put(ADOS_VAC_JOURNEE_SANS, c);
                    }
                }
                if (label.contains("sortie")) {
                    if (label.contains("1/2"))
                        mapping.put(ADOS_SORTIE_DEMI, c);
                    if (label.contains("journee") || label.contains("journée"))
                        mapping.put(ADOS_SORTIE_JOURNEE, c);
                }
            }

            // --- SEJOURS ---
            if (label.contains("sejour") || label.contains("séjour")) {
                if (label.contains("5"))
                    mapping.put(SEJOUR_5_JOURS, c);
                if (label.contains("6"))
                    mapping.put(SEJOUR_6_JOURS, c);
            }
        }
        return mapping;
    }

    private static double getValeurDeColonne(Row row, Map<String, Integer> mapping, String cle, int indexDefaut) {
        int index = mapping.getOrDefault(cle, indexDefaut);
        return getValeurNumerique(row.getCell(index));
    }

    /**
     * Analyse le texte pour en extraire les bornes numeriques du Quotient Familial.
     * 
     * @param texte   Le texte de la cellule (ex: "13 101e a 15 388e")
     * @param tranche Le code tranche (pour identifier EXT)
     * @return Un tableau [Min, Max]
     */
    private static double[] extraireBornesQF(String texte, String tranche) {
        // La tranche EXTétrique (Extérieurs) ne dépend pas du QF.
        // On la place à une valeur inatteignable pour qu'elle n'écrase pas la Tranche A
        // d'un résident à haut QF.
        if (tranche.contains("EXT"))
            return new double[] { Double.MAX_VALUE - 1, Double.MAX_VALUE };

        try {
            // Nettoyage prealable des accents courants et separateurs
            String prep = texte.toLowerCase()
                    .replace("\u00E0", "|") // à
                    .replace("à", "|")
                    .replace(" a ", "|")
                    .replace("-", "|")
                    .replace("\u2013", "|")
                    .replace("\u2014", "|");

            String[] segments = prep.split("\\|");
            List<Double> nombres = new ArrayList<>();

            for (String s : segments) {
                // Pour chaque segment, on ne garde QUE les chiffres
                String digits = s.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    try {
                        nombres.add(Double.parseDouble(digits));
                    } catch (NumberFormatException e) {
                        /* Ignorer les segments non numeriques */ }
                }
            }

            if (nombres.size() >= 2) {
                // Règle métier de continuité : le tableau Excel affiche [Min - Max inclusif].
                // Puisque TarificationService fait un contrôle strict (qf < max),
                // on doit ajouter +1 à la borne numérique supérieure pour inclure les
                // décimales.
                // Ex: "3 954 - 6 240" devient [3954.0, 6241.0[.
                return new double[] { nombres.get(0), nombres.get(1) + 1.0 };
            } else if (nombres.size() == 1) {
                if (texte.toLowerCase().contains("plus"))
                    return new double[] { nombres.get(0), Double.MAX_VALUE - 2 };
                if (texte.toLowerCase().contains("moins"))
                    return new double[] { 0, nombres.get(0) };
                return new double[] { nombres.get(0), nombres.get(0) + 1.0 };
            }
        } catch (Exception e) {
        }

        return new double[] { -1, -1 };
    }

    /**
     * Charge une grille tarifaire depuis un fichier Excel externe.
     * Le fichier doit comporter les colonnes suivantes :
     * Col 0: Nom Tranche, Col 1: QF Min, Col 2: QF Max, Col 3+: Tarifs
     * 
     * @param cheminFichier Chemin vers le fichier Excel (ex:
     *                      Donnees/grille_2026.xlsx)
     * @return Liste de Tarifs charges, ou liste vide en cas d'erreur.
     */
    public static List<Tarif> chargerTarifsDepuisExcel(String cheminFichier) {
        List<Tarif> tarifs = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(cheminFichier);
                Workbook wb = WorkbookFactory.create(fis)) {

            if (wb.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Le classeur Excel ne contient aucune feuille de calcul.");
            }

            Sheet s = wb.getSheetAt(0); // On prend la premiere feuille

            if (s.getLastRowNum() < 5) {
                throw new IllegalArgumentException(
                        "La grille semble vide ou trop courte pour être une grille tarifaire valide.");
            }

            Map<String, Integer> mapping = scannerEntetes(s);

            // Validation forte : on s'assure d'avoir trouvé des colonnes structurantes
            // incontournables.
            if (!mapping.containsKey(REPAS) || !mapping.containsKey(ACCUEIL_JOURNEE)) {
                throw new IllegalArgumentException(
                        "Structure invalide. Impossible de détecter les colonnes essentielles (ex: Repas, Accueil Loisirs).");
            }

            for (int i = 1; i <= s.getLastRowNum(); i++) { // On saute l'entete
                Row row = s.getRow(i);
                if (row == null)
                    continue;

                String tranche = getValeurTexte(row.getCell(0));
                double qfMin = getValeurNumerique(row.getCell(1));
                double qfMax = getValeurNumerique(row.getCell(2));

                if (tranche.isEmpty())
                    continue;

                Map<String, Double> prix = new HashMap<>();
                prix.put(REPAS, getValeurNumerique(row.getCell(3)));
                prix.put(ACCUEIL_JOURNEE, getValeurNumerique(row.getCell(4)));
                prix.put(ACCUEIL_DEMI_REPAS, getValeurNumerique(row.getCell(5)));
                prix.put(PERISCOLAIRE_MATIN_SOIR, getValeurNumerique(row.getCell(6)));
                prix.put(PERISCOLAIRE_MATIN_OU_SOIR, getValeurNumerique(row.getCell(7)));
                prix.put(ETUDES_FORFAIT_MENSUEL, getValeurNumerique(row.getCell(8)));
                prix.put(ETUDES_DEMI_FORFAIT, getValeurNumerique(row.getCell(9)));
                prix.put(ADOS_VAC_JOURNEE_REPAS, getValeurNumerique(row.getCell(10)));
                prix.put(ADOS_VAC_JOURNEE_SANS, getValeurNumerique(row.getCell(11)));
                prix.put(ADOS_VAC_DEMI_REPAS, getValeurNumerique(row.getCell(12)));
                prix.put(ADOS_VAC_DEMI_SANS, getValeurNumerique(row.getCell(13)));
                prix.put(ADOS_SORTIE_DEMI, getValeurNumerique(row.getCell(14)));
                prix.put(ADOS_SORTIE_JOURNEE, getValeurNumerique(row.getCell(15)));

                tarifs.add(new Tarif(tranche, qfMin, qfMax, prix));
            }
        } catch (Exception e) {
            LogService.error("Le blindage a detecte une erreur de lecture (grille statique)", e);
        }
        return tarifs;
    }

    /**
     * Charge l'integralite des tarifs de reference pour 2025 (Modele interne).
     * 
     * @return Une liste d'objets Tarif initialises avec leurs bornes QF.
     */
    public static List<Tarif> chargerTarifsReference() {
        List<Tarif> tarifs = new ArrayList<>();

        // // --- Tranche EXT (Exterieur, Habitants hors commune) ---
        // tarifs.add(new Tarif("EXT", 18000, Double.MAX_VALUE, map(
        // REPAS, 5.98, ACCUEIL_JOURNEE, 17.95, ACCUEIL_DEMI_REPAS, 11.97,
        // PERISCOLAIRE_MATIN_SOIR, 5.98, PERISCOLAIRE_MATIN_OU_SOIR, 2.98,
        // ETUDES_FORFAIT_MENSUEL, 30.26, ETUDES_DEMI_FORFAIT, 15.12,
        // TARIF_POST_ETUDES, 2.98, CLASSE_DECOUVERTE, 80.24,
        // ADOS_VAC_JOURNEE_REPAS, 12.60, ADOS_VAC_JOURNEE_SANS, 6.63,
        // ADOS_VAC_DEMI_REPAS, 9.30, ADOS_VAC_DEMI_SANS, 3.33,
        // ADOS_SORTIE_DEMI, 6.48, ADOS_SORTIE_JOURNEE, 10.80,
        // SEJOUR_5_JOURS, 60.00, SEJOUR_6_JOURS, 80.00)));

        // --- Tranche A (18000 euros et plus, habitants) ---
        tarifs.add(new Tarif("A", 18000, Double.MAX_VALUE, map(
                REPAS, 5.54, ACCUEIL_JOURNEE, 16.63, ACCUEIL_DEMI_REPAS, 11.08,
                PERISCOLAIRE_MATIN_SOIR, 5.54, PERISCOLAIRE_MATIN_OU_SOIR, 2.79,
                ETUDES_FORFAIT_MENSUEL, 27.51, ETUDES_DEMI_FORFAIT, 13.75,
                TARIF_POST_ETUDES, 2.22, CLASSE_DECOUVERTE, 64.20,
                ADOS_VAC_JOURNEE_REPAS, 11.69, ADOS_VAC_JOURNEE_SANS, 6.15,
                ADOS_VAC_DEMI_REPAS, 8.62, ADOS_VAC_DEMI_SANS, 3.07,
                ADOS_SORTIE_DEMI, 5.18, ADOS_SORTIE_JOURNEE, 8.64,
                SEJOUR_5_JOURS, 48.00, SEJOUR_6_JOURS, 64.00)));

        // --- Tranche B (15000 a 17999.99 euros) ---
        tarifs.add(new Tarif("B", 15000, 17999.99, map(
                REPAS, 4.89, ACCUEIL_JOURNEE, 14.73, ACCUEIL_DEMI_REPAS, 9.78,
                PERISCOLAIRE_MATIN_SOIR, 4.89, PERISCOLAIRE_MATIN_OU_SOIR, 2.43,
                ETUDES_FORFAIT_MENSUEL, 24.75, ETUDES_DEMI_FORFAIT, 12.38,
                TARIF_POST_ETUDES, 1.93, CLASSE_DECOUVERTE, 56.16,
                ADOS_VAC_JOURNEE_REPAS, 10.37, ADOS_VAC_JOURNEE_SANS, 5.49,
                ADOS_VAC_DEMI_REPAS, 7.64, ADOS_VAC_DEMI_SANS, 2.75,
                ADOS_SORTIE_DEMI, 4.54, ADOS_SORTIE_JOURNEE, 7.56,
                SEJOUR_5_JOURS, 42.00, SEJOUR_6_JOURS, 56.00)));

        // --- Tranche B2 (13000 a 14999.99 euros) ---
        tarifs.add(new Tarif("B2", 13000, 14999.99, map(
                REPAS, 4.32, ACCUEIL_JOURNEE, 12.46, ACCUEIL_DEMI_REPAS, 8.34,
                PERISCOLAIRE_MATIN_SOIR, 4.17, PERISCOLAIRE_MATIN_OU_SOIR, 2.11,
                ETUDES_FORFAIT_MENSUEL, 22.29, ETUDES_DEMI_FORFAIT, 11.15,
                TARIF_POST_ETUDES, 1.68, CLASSE_DECOUVERTE, 48.17,
                ADOS_VAC_JOURNEE_REPAS, 9.09, ADOS_VAC_JOURNEE_SANS, 4.91,
                ADOS_VAC_DEMI_REPAS, 6.63, ADOS_VAC_DEMI_SANS, 2.44,
                ADOS_SORTIE_DEMI, 3.89, ADOS_SORTIE_JOURNEE, 6.48,
                SEJOUR_5_JOURS, 36.00, SEJOUR_6_JOURS, 48.00)));

        // --- Tranche C (11000 a 12999.99 euros) ---
        tarifs.add(new Tarif("C", 11000, 12999.99, map(
                REPAS, 4.17, ACCUEIL_JOURNEE, 12.46, ACCUEIL_DEMI_REPAS, 8.34,
                PERISCOLAIRE_MATIN_SOIR, 4.17, PERISCOLAIRE_MATIN_OU_SOIR, 2.11,
                ETUDES_FORFAIT_MENSUEL, 22.29, ETUDES_DEMI_FORFAIT, 11.15,
                TARIF_POST_ETUDES, 1.68, CLASSE_DECOUVERTE, 48.17,
                ADOS_VAC_JOURNEE_REPAS, 9.09, ADOS_VAC_JOURNEE_SANS, 4.91,
                ADOS_VAC_DEMI_REPAS, 6.63, ADOS_VAC_DEMI_SANS, 2.44,
                ADOS_SORTIE_DEMI, 3.89, ADOS_SORTIE_JOURNEE, 6.48,
                SEJOUR_5_JOURS, 36.00, SEJOUR_6_JOURS, 48.00)));

        // --- Tranche D (9000 a 10999.99 euros) ---
        tarifs.add(new Tarif("D", 9000, 10999.99, map(
                REPAS, 3.51, ACCUEIL_JOURNEE, 10.55, ACCUEIL_DEMI_REPAS, 7.02,
                PERISCOLAIRE_MATIN_SOIR, 3.51, PERISCOLAIRE_MATIN_OU_SOIR, 1.76,
                ETUDES_FORFAIT_MENSUEL, 20.06, ETUDES_DEMI_FORFAIT, 10.03,
                TARIF_POST_ETUDES, 1.40, CLASSE_DECOUVERTE, 40.12,
                ADOS_VAC_JOURNEE_REPAS, 6.49, ADOS_VAC_JOURNEE_SANS, 2.96,
                ADOS_VAC_DEMI_REPAS, 5.01, ADOS_VAC_DEMI_SANS, 1.51,
                ADOS_SORTIE_DEMI, 3.24, ADOS_SORTIE_JOURNEE, 5.40,
                SEJOUR_5_JOURS, 30.00, SEJOUR_6_JOURS, 40.00)));

        // --- Tranche E (7000 a 8999.99 euros) ---
        tarifs.add(new Tarif("E", 7000, 8999.99, map(
                REPAS, 2.92, ACCUEIL_JOURNEE, 8.82, ACCUEIL_DEMI_REPAS, 5.83,
                PERISCOLAIRE_MATIN_SOIR, 2.92, PERISCOLAIRE_MATIN_OU_SOIR, 1.48,
                ETUDES_FORFAIT_MENSUEL, 18.06, ETUDES_DEMI_FORFAIT, 9.03,
                TARIF_POST_ETUDES, 1.20, CLASSE_DECOUVERTE, 32.08,
                ADOS_VAC_JOURNEE_REPAS, 5.18, ADOS_VAC_JOURNEE_SANS, 2.27,
                ADOS_VAC_DEMI_REPAS, 4.05, ADOS_VAC_DEMI_SANS, 1.13,
                ADOS_SORTIE_DEMI, 2.59, ADOS_SORTIE_JOURNEE, 4.32,
                SEJOUR_5_JOURS, 24.00, SEJOUR_6_JOURS, 32.00)));

        // --- Tranche F (5000 a 6999.99 euros) ---
        tarifs.add(new Tarif("F", 5000, 6999.99, map(
                REPAS, 2.16, ACCUEIL_JOURNEE, 6.50, ACCUEIL_DEMI_REPAS, 4.32,
                PERISCOLAIRE_MATIN_SOIR, 2.16, PERISCOLAIRE_MATIN_OU_SOIR, 1.08,
                ETUDES_FORFAIT_MENSUEL, 16.28, ETUDES_DEMI_FORFAIT, 8.13,
                TARIF_POST_ETUDES, 0.86, CLASSE_DECOUVERTE, 24.08,
                ADOS_VAC_JOURNEE_REPAS, 3.90, ADOS_VAC_JOURNEE_SANS, 1.74,
                ADOS_VAC_DEMI_REPAS, 3.00, ADOS_VAC_DEMI_SANS, 0.87,
                ADOS_SORTIE_DEMI, 1.94, ADOS_SORTIE_JOURNEE, 3.24,
                SEJOUR_5_JOURS, 18.00, SEJOUR_6_JOURS, 24.00)));

        // --- Tranche F2 (3000 a 4999.99 euros) ---
        tarifs.add(new Tarif("F2", 3000, 4999.99, map(
                REPAS, 1.57, ACCUEIL_JOURNEE, 6.50, ACCUEIL_DEMI_REPAS, 4.32,
                PERISCOLAIRE_MATIN_SOIR, 2.16, PERISCOLAIRE_MATIN_OU_SOIR, 1.08,
                ETUDES_FORFAIT_MENSUEL, 16.28, ETUDES_DEMI_FORFAIT, 8.13,
                TARIF_POST_ETUDES, 0.86, CLASSE_DECOUVERTE, 24.08,
                ADOS_VAC_JOURNEE_REPAS, 3.90, ADOS_VAC_JOURNEE_SANS, 1.74,
                ADOS_VAC_DEMI_REPAS, 3.00, ADOS_VAC_DEMI_SANS, 0.87,
                ADOS_SORTIE_DEMI, 1.94, ADOS_SORTIE_JOURNEE, 3.24,
                SEJOUR_5_JOURS, 18.00, SEJOUR_6_JOURS, 24.00)));

        // --- Tranche G (0 a 2999.99 euros) ---
        tarifs.add(new Tarif("G", 0, 2999.99, map(
                REPAS, 1.43, ACCUEIL_JOURNEE, 4.35, ACCUEIL_DEMI_REPAS, 2.85,
                PERISCOLAIRE_MATIN_SOIR, 1.43, PERISCOLAIRE_MATIN_OU_SOIR, 0.70,
                ETUDES_FORFAIT_MENSUEL, 14.62, ETUDES_DEMI_FORFAIT, 7.31,
                TARIF_POST_ETUDES, 0.56, CLASSE_DECOUVERTE, 16.05,
                ADOS_VAC_JOURNEE_REPAS, 2.60, ADOS_VAC_JOURNEE_SANS, 1.17,
                ADOS_VAC_DEMI_REPAS, 2.02, ADOS_VAC_DEMI_SANS, 0.58,
                ADOS_SORTIE_DEMI, 1.30, ADOS_SORTIE_JOURNEE, 2.16,
                SEJOUR_5_JOURS, 12.00, SEJOUR_6_JOURS, 16.00)));

        return tarifs;
    }

    private static String getValeurTexte(Cell cell) {
        if (cell == null)
            return "";
        if (cell.getCellType() == CellType.STRING)
            return cell.getStringCellValue().trim();
        return "";
    }

    private static double getValeurNumerique(Cell cell) {
        if (cell == null)
            return 0.0;

        // Si c'est un nombre pur
        if (cell.getCellType() == CellType.NUMERIC)
            return cell.getNumericCellValue();

        // Si c'est une formule
        if (cell.getCellType() == CellType.FORMULA) {
            try {
                return cell.getNumericCellValue();
            } catch (Exception e) {
                return 0.0;
            }
        }

        // Si c'est du texte (cas frequent avec les symboles monetaires ou virgules)
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (val.isEmpty())
                return 0.0;

            // 1. Nettoyage initial : espaces classiques, espaces insecables, symboles euros
            // potentiels
            val = val.replaceAll("\\s+", "").replace("\u00A0", "").replace("€", "").replace("e", "");

            // 2. Gestion des séparateurs (milliers vs decimales)
            if (val.contains(",") && val.contains(".")) {
                int lastDot = val.lastIndexOf(".");
                int lastComma = val.lastIndexOf(",");
                if (lastComma > lastDot) {
                    // ex: 1.234,56 -> virgule décimale
                    val = val.replace(".", "");
                    val = val.replace(",", ".");
                } else {
                    // ex: 1,234.56 -> point décimal
                    val = val.replace(",", "");
                }
            } else if (val.contains(",")) {
                // Seulement une virgule, on assume que c'est une décimale
                val = val.replace(",", ".");
            }

            // 3. Purge de tous les caracteres restants sauf chiffres, point decimal et
            // signe moins
            val = val.replaceAll("[^0-9.-]", "");

            try {
                if (!val.isEmpty() && !val.equals(".") && !val.equals("-")) {
                    return Double.parseDouble(val);
                }
            } catch (Exception e) {
                LogService.log("Avertissement -> Format inattendu ignoré : [" + cell.getStringCellValue() + "]");
                return 0.0;
            }
        }

        return 0.0;
    }

    private static Map<String, Double> map(Object... kv) {
        Map<String, Double> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            String service = (String) kv[i];
            Double prix = (Double) kv[i + 1];
            m.put(service, prix);
        }
        return m;
    }
}
