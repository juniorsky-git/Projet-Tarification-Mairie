package fr.mairie.tarification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonneesTarifs {

    // =========================================================================
    // CONSTANTES - Clés des activités
    // =========================================================================

    // Repas
    public static final String REPAS = "repas";

    // Accueil de Loisirs
    public static final String ACCUEIL_JOURNEE = "accueil-journee";
    public static final String ACCUEIL_DEMI_REPAS = "accueil-demi-repas";

    // Accueil Périscolaire
    public static final String PERISCOLAIRE_MATIN_SOIR = "periscolaire-matin-soir";
    public static final String PERISCOLAIRE_MATIN_OU_SOIR = "periscolaire-matin-ou-soir";

    // Études surveillées
    public static final String ETUDES_FORFAIT_MENSUEL = "etudes-forfait-mensuel";
    public static final String ETUDES_DEMI_FORFAIT = "etudes-demi-forfait";

    // Espace Ados (vacances scolaires)
    public static final String ADOS_VAC_JOURNEE_REPAS = "ados-journee-repas";
    public static final String ADOS_VAC_JOURNEE_SANS = "ados-journee-sans";
    public static final String ADOS_VAC_DEMI_REPAS = "ados-demi-repas";
    public static final String ADOS_VAC_DEMI_SANS = "ados-demi-sans";
    public static final String ADOS_SORTIE_DEMI = "ados-sortie-demi";
    public static final String ADOS_SORTIE_JOURNEE = "ados-sortie-journee";

    // GRILLE TARIFAIRE DE RÉFÉRENCE 2025 (+8%) - Données complètes
    public static List<Tarif> chargerTarifsReference() {
        List<Tarif> tarifs = new ArrayList<>();

        // EXT (Extérieur, 18000€ et plus)
        tarifs.add(new Tarif("EXT", 18000, Double.MAX_VALUE, map(
                REPAS, 5.98,
                ACCUEIL_JOURNEE, 17.95,
                ACCUEIL_DEMI_REPAS, 11.97,
                PERISCOLAIRE_MATIN_SOIR, 5.98,
                PERISCOLAIRE_MATIN_OU_SOIR, 2.98,
                ETUDES_FORFAIT_MENSUEL, 30.26,
                ETUDES_DEMI_FORFAIT, 15.12,
                ADOS_VAC_JOURNEE_REPAS, 12.60,
                ADOS_VAC_JOURNEE_SANS, 6.63,
                ADOS_VAC_DEMI_REPAS, 9.30,
                ADOS_VAC_DEMI_SANS, 3.33,
                ADOS_SORTIE_DEMI, 6.18,
                ADOS_SORTIE_JOURNEE, 10.80)));

        // A (18000€ et plus, habitants)
        tarifs.add(new Tarif("A", 18000, Double.MAX_VALUE, map(
                REPAS, 5.54,
                ACCUEIL_JOURNEE, 16.63,
                ACCUEIL_DEMI_REPAS, 11.08,
                PERISCOLAIRE_MATIN_SOIR, 5.54,
                PERISCOLAIRE_MATIN_OU_SOIR, 2.79,
                ETUDES_FORFAIT_MENSUEL, 27.51,
                ETUDES_DEMI_FORFAIT, 13.75,
                ADOS_VAC_JOURNEE_REPAS, 11.69,
                ADOS_VAC_JOURNEE_SANS, 6.15,
                ADOS_VAC_DEMI_REPAS, 8.62,
                ADOS_VAC_DEMI_SANS, 3.07,
                ADOS_SORTIE_DEMI, 5.18,
                ADOS_SORTIE_JOURNEE, 8.64)));

        // B (15000 à 17999€)
        tarifs.add(new Tarif("B", 15000, 17999.99, map(
                REPAS, 4.89,
                ACCUEIL_JOURNEE, 14.73,
                ACCUEIL_DEMI_REPAS, 9.78,
                PERISCOLAIRE_MATIN_SOIR, 4.89,
                PERISCOLAIRE_MATIN_OU_SOIR, 2.43,
                ETUDES_FORFAIT_MENSUEL, 24.75,
                ETUDES_DEMI_FORFAIT, 12.38,
                ADOS_VAC_JOURNEE_REPAS, 10.37,
                ADOS_VAC_JOURNEE_SANS, 5.49,
                ADOS_VAC_DEMI_REPAS, 7.64,
                ADOS_VAC_DEMI_SANS, 2.75,
                ADOS_SORTIE_DEMI, 4.54,
                ADOS_SORTIE_JOURNEE, 7.56)));

        // B2 (13000 à 14999€)
        tarifs.add(new Tarif("B2", 13000, 14999.99, map(
                REPAS, 4.32,
                ACCUEIL_JOURNEE, 12.46,
                ACCUEIL_DEMI_REPAS, 8.34,
                PERISCOLAIRE_MATIN_SOIR, 4.17,
                PERISCOLAIRE_MATIN_OU_SOIR, 2.11,
                ETUDES_FORFAIT_MENSUEL, 22.29,
                ETUDES_DEMI_FORFAIT, 11.15,
                ADOS_VAC_JOURNEE_REPAS, 9.09,
                ADOS_VAC_JOURNEE_SANS, 4.91,
                ADOS_VAC_DEMI_REPAS, 6.63,
                ADOS_VAC_DEMI_SANS, 2.44,
                ADOS_SORTIE_DEMI, 3.89,
                ADOS_SORTIE_JOURNEE, 6.48)));

        // C (11000 à 12999€)
        tarifs.add(new Tarif("C", 11000, 12999.99, map(
                REPAS, 4.17,
                ACCUEIL_JOURNEE, 12.46,
                ACCUEIL_DEMI_REPAS, 8.34,
                PERISCOLAIRE_MATIN_SOIR, 4.17,
                PERISCOLAIRE_MATIN_OU_SOIR, 2.11,
                ETUDES_FORFAIT_MENSUEL, 22.29,
                ETUDES_DEMI_FORFAIT, 11.15,
                ADOS_VAC_JOURNEE_REPAS, 9.09,
                ADOS_VAC_JOURNEE_SANS, 4.91,
                ADOS_VAC_DEMI_REPAS, 6.63,
                ADOS_VAC_DEMI_SANS, 2.44,
                ADOS_SORTIE_DEMI, 3.89,
                ADOS_SORTIE_JOURNEE, 6.48)));

        // D (9000 à 10999€)
        tarifs.add(new Tarif("D", 9000, 10999.99, map(
                REPAS, 3.51,
                ACCUEIL_JOURNEE, 10.55,
                ACCUEIL_DEMI_REPAS, 7.02,
                PERISCOLAIRE_MATIN_SOIR, 3.51,
                PERISCOLAIRE_MATIN_OU_SOIR, 1.76,
                ETUDES_FORFAIT_MENSUEL, 20.06,
                ETUDES_DEMI_FORFAIT, 10.03,
                ADOS_VAC_JOURNEE_REPAS, 6.49,
                ADOS_VAC_JOURNEE_SANS, 2.96,
                ADOS_VAC_DEMI_REPAS, 5.01,
                ADOS_VAC_DEMI_SANS, 1.51,
                ADOS_SORTIE_DEMI, 3.24,
                ADOS_SORTIE_JOURNEE, 5.40)));

        // E (7000 à 8999€)
        tarifs.add(new Tarif("E", 7000, 8999.99, map(
                REPAS, 2.92,
                ACCUEIL_JOURNEE, 8.82,
                ACCUEIL_DEMI_REPAS, 5.83,
                PERISCOLAIRE_MATIN_SOIR, 2.92,
                PERISCOLAIRE_MATIN_OU_SOIR, 1.48,
                ETUDES_FORFAIT_MENSUEL, 18.06,
                ETUDES_DEMI_FORFAIT, 9.03,
                ADOS_VAC_JOURNEE_REPAS, 5.18,
                ADOS_VAC_JOURNEE_SANS, 2.27,
                ADOS_VAC_DEMI_REPAS, 4.05,
                ADOS_VAC_DEMI_SANS, 1.13,
                ADOS_SORTIE_DEMI, 2.59,
                ADOS_SORTIE_JOURNEE, 4.32)));

        // F (5000 à 6999€)
        tarifs.add(new Tarif("F", 5000, 6999.99, map(
                REPAS, 2.16,
                ACCUEIL_JOURNEE, 6.50,
                ACCUEIL_DEMI_REPAS, 4.32,
                PERISCOLAIRE_MATIN_SOIR, 2.16,
                PERISCOLAIRE_MATIN_OU_SOIR, 1.08,
                ETUDES_FORFAIT_MENSUEL, 16.28,
                ETUDES_DEMI_FORFAIT, 8.13,
                ADOS_VAC_JOURNEE_REPAS, 3.90,
                ADOS_VAC_JOURNEE_SANS, 1.74,
                ADOS_VAC_DEMI_REPAS, 3.00,
                ADOS_VAC_DEMI_SANS, 0.87,
                ADOS_SORTIE_DEMI, 1.94,
                ADOS_SORTIE_JOURNEE, 3.24)));

        // F2 (3000 à 4999€)
        tarifs.add(new Tarif("F2", 3000, 4999.99, map(
                REPAS, 1.57,
                ACCUEIL_JOURNEE, 6.50,
                ACCUEIL_DEMI_REPAS, 4.32,
                PERISCOLAIRE_MATIN_SOIR, 2.16,
                PERISCOLAIRE_MATIN_OU_SOIR, 1.08,
                ETUDES_FORFAIT_MENSUEL, 16.28,
                ETUDES_DEMI_FORFAIT, 8.13,
                ADOS_VAC_JOURNEE_REPAS, 3.90,
                ADOS_VAC_JOURNEE_SANS, 1.74,
                ADOS_VAC_DEMI_REPAS, 3.00,
                ADOS_VAC_DEMI_SANS, 0.87,
                ADOS_SORTIE_DEMI, 1.94,
                ADOS_SORTIE_JOURNEE, 3.24)));

        // G (0 à 2999€)
        tarifs.add(new Tarif("G", 0, 2999.99, map(
                REPAS, 1.43,
                ACCUEIL_JOURNEE, 4.35,
                ACCUEIL_DEMI_REPAS, 2.85,
                PERISCOLAIRE_MATIN_SOIR, 1.43,
                PERISCOLAIRE_MATIN_OU_SOIR, 0.70,
                ETUDES_FORFAIT_MENSUEL, 14.62,
                ETUDES_DEMI_FORFAIT, 7.31,
                ADOS_VAC_JOURNEE_REPAS, 2.60,
                ADOS_VAC_JOURNEE_SANS, 1.17,
                ADOS_VAC_DEMI_REPAS, 2.02,
                ADOS_VAC_DEMI_SANS, 0.58,
                ADOS_SORTIE_DEMI, 1.30,
                ADOS_SORTIE_JOURNEE, 2.16)));

        return tarifs;
    }

    // =========================================================================
    // CHARGEMENT DEPUIS LE CSV (données réelles usagers/recettes)
    // =========================================================================

    public static List<Tarif> chargerTarifs() {
        List<Tarif> tarifs = new ArrayList<>();
        String cheminFichier = "Donnees/Tableau-grille/Classeur1.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            int numLigne = 0;

            while ((ligne = br.readLine()) != null) {
                numLigne++;

                if (numLigne < 4)
                    continue;

                if (ligne.startsWith("Total") || ligne.startsWith(";;") || ligne.trim().isEmpty()) {
                    break;
                }

                String[] valeurs = ligne.split(";", -1);

                if (valeurs.length >= 7) {
                    String tranche = valeurs[1].trim();
                    if (tranche.isEmpty()) {
                        tranche = valeurs[0].trim();
                    }

                    double repasVal = parseValeurNumerique(valeurs[2]);
                    int usagers = (int) parseValeurNumerique(valeurs[3]);
                    double recettes = parseValeurNumerique(valeurs[6]);

                    Map<String, Double> tarifsMap = new HashMap<>();
                    tarifsMap.put(REPAS, repasVal);

                    double qfMin = getQfMin(tranche);
                    double qfMax = getQfMax(tranche);

                    tarifs.add(new Tarif(tranche, qfMin, qfMax, tarifsMap, usagers, recettes));
                }
            }
        } catch (IOException e) {
            System.err.println(
                    "Avertissement : Erreur de lecture du fichier " + cheminFichier + " (" + e.getMessage() + ")");
        } catch (Exception e) {
            System.err.println("Avertissement : Erreur de format dans le fichier CSV (" + e.getMessage() + ")");
        }

        return tarifs;
    }

    // =========================================================================
    // UTILITAIRES PUBLICS POUR EXCEL
    // =========================================================================

    /** Accessible par ExcelReader */
    public static double getQfMinPublic(String tranche) {
        return getQfMin(tranche);
    }

    /** Accessible par ExcelReader */
    public static double getQfMaxPublic(String tranche) {
        return getQfMax(tranche);
    }

    /**
     * Charge les tarifs depuis un fichier Excel (.xlsx ou .xls).
     * @param cheminFichier chemin vers le fichier Excel
     * @return liste des tarifs extraits
     */
    public static List<Tarif> chargerTarifsDepuisExcel(String cheminFichier) {
        return ExcelReader.lire(cheminFichier);
    }

    // =========================================================================
    // UTILITAIRES PRIVÉS
    // =========================================================================

    /** Construit une Map depuis une liste alternée clé/valeur. */
    private static Map<String, Double> map(Object... args) {
        Map<String, Double> m = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            m.put((String) args[i], (Double) args[i + 1]);
        }
        return m;
    }

    private static double parseValeurNumerique(String valeur) {
        if (valeur == null || valeur.trim().isEmpty())
            return 0.0;
        String clean = valeur.replaceAll("[^0-9,\\.-]", "").replace(',', '.');
        if (clean.isEmpty())
            return 0.0;
        return Double.parseDouble(clean);
    }

    private static double getQfMin(String tranche) {
        switch (tranche) {
            case "EXT":
            case "A":
                return 18000;
            case "B":
                return 15000;
            case "B2":
                return 13000;
            case "C":
                return 11000;
            case "D":
                return 9000;
            case "E":
                return 7000;
            case "F":
                return 5000;
            case "F2":
                return 3000;
            case "G":
                return 0;
            default:
                return 0;
        }
    }

    private static double getQfMax(String tranche) {
        switch (tranche) {
            case "EXT":
            case "A":
                return 999999;
            case "B":
                return 17999.99;
            case "B2":
                return 14999.99;
            case "C":
                return 12999.99;
            case "D":
                return 10999.99;
            case "E":
                return 8999.99;
            case "F":
                return 6999.99;
            case "F2":
                return 4999.99;
            case "G":
                return 2999.99;
            default:
                return 0;
        }
    }
}
