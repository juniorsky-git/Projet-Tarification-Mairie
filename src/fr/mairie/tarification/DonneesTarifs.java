package fr.mairie.tarification;

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

    /**
     * Charge une grille tarifaire depuis un fichier Excel externe.
     * Le fichier doit comporter les colonnes suivantes :
     * Col 0: Nom Tranche, Col 1: QF Min, Col 2: QF Max, Col 3+: Tarifs
     * 
     * @param cheminFichier Chemin vers le fichier Excel (ex: Donnees/grille_2026.xlsx)
     * @return Liste de Tarifs charges, ou liste vide en cas d'erreur.
     */
    public static List<Tarif> chargerTarifsDepuisExcel(String cheminFichier) {
        List<Tarif> tarifs = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(cheminFichier);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            Sheet s = wb.getSheetAt(0); // On prend la premiere feuille
            for (int i = 1; i <= s.getLastRowNum(); i++) { // On saute l'entete
                Row row = s.getRow(i);
                if (row == null) continue;

                String tranche = getValeurTexte(row.getCell(0));
                double qfMin = getValeurNumerique(row.getCell(1));
                double qfMax = getValeurNumerique(row.getCell(2));

                if (tranche.isEmpty()) continue;

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
            System.err.println("Le blindage a detecte une erreur de lecture : " + e.getMessage());
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

        // --- Tranche EXT (Exterieur, Habitants hors commune) ---
        tarifs.add(new Tarif("EXT", 18000, Double.MAX_VALUE, map(
                REPAS, 5.98, ACCUEIL_JOURNEE, 17.95, ACCUEIL_DEMI_REPAS, 11.97,
                PERISCOLAIRE_MATIN_SOIR, 5.98, PERISCOLAIRE_MATIN_OU_SOIR, 2.98,
                ETUDES_FORFAIT_MENSUEL, 30.26, ETUDES_DEMI_FORFAIT, 15.12,
                ADOS_VAC_JOURNEE_REPAS, 12.60, ADOS_VAC_JOURNEE_SANS, 6.63,
                ADOS_VAC_DEMI_REPAS, 9.30, ADOS_VAC_DEMI_SANS, 3.33,
                ADOS_SORTIE_DEMI, 6.18, ADOS_SORTIE_JOURNEE, 10.80)));

        // --- Tranche A (18000 euros et plus, habitants) ---
        tarifs.add(new Tarif("A", 18000, Double.MAX_VALUE, map(
                REPAS, 5.54, ACCUEIL_JOURNEE, 16.63, ACCUEIL_DEMI_REPAS, 11.08,
                PERISCOLAIRE_MATIN_SOIR, 5.54, PERISCOLAIRE_MATIN_OU_SOIR, 2.79,
                ETUDES_FORFAIT_MENSUEL, 27.51, ETUDES_DEMI_FORFAIT, 13.75,
                ADOS_VAC_JOURNEE_REPAS, 11.69, ADOS_VAC_JOURNEE_SANS, 6.15,
                ADOS_VAC_DEMI_REPAS, 8.62, ADOS_VAC_DEMI_SANS, 3.07,
                ADOS_SORTIE_DEMI, 5.18, ADOS_SORTIE_JOURNEE, 8.64)));

        // --- Tranche B (15000 a 17999.99 euros) ---
        tarifs.add(new Tarif("B", 15000, 17999.99, map(
                REPAS, 4.89, ACCUEIL_JOURNEE, 14.73, ACCUEIL_DEMI_REPAS, 9.78,
                PERISCOLAIRE_MATIN_SOIR, 4.89, PERISCOLAIRE_MATIN_OU_SOIR, 2.43,
                ETUDES_FORFAIT_MENSUEL, 24.75, ETUDES_DEMI_FORFAIT, 12.38,
                ADOS_VAC_JOURNEE_REPAS, 10.37, ADOS_VAC_JOURNEE_SANS, 5.49,
                ADOS_VAC_DEMI_REPAS, 7.64, ADOS_VAC_DEMI_SANS, 2.75,
                ADOS_SORTIE_DEMI, 4.54, ADOS_SORTIE_JOURNEE, 7.56)));

        // --- Tranche B2 (13000 a 14999.99 euros) ---
        tarifs.add(new Tarif("B2", 13000, 14999.99, map(
                REPAS, 4.32, ACCUEIL_JOURNEE, 12.46, ACCUEIL_DEMI_REPAS, 8.34,
                PERISCOLAIRE_MATIN_SOIR, 4.17, PERISCOLAIRE_MATIN_OU_SOIR, 2.11,
                ETUDES_FORFAIT_MENSUEL, 22.29, ETUDES_DEMI_FORFAIT, 11.15,
                ADOS_VAC_JOURNEE_REPAS, 9.09, ADOS_VAC_JOURNEE_SANS, 4.91,
                ADOS_VAC_DEMI_REPAS, 6.63, ADOS_VAC_DEMI_SANS, 2.44,
                ADOS_SORTIE_DEMI, 3.89, ADOS_SORTIE_JOURNEE, 6.48)));

        // --- Tranche C (11000 a 12999.99 euros) ---
        tarifs.add(new Tarif("C", 11000, 12999.99, map(
                REPAS, 4.17, ACCUEIL_JOURNEE, 12.46, ACCUEIL_DEMI_REPAS, 8.34,
                PERISCOLAIRE_MATIN_SOIR, 4.17, PERISCOLAIRE_MATIN_OU_SOIR, 2.11,
                ETUDES_FORFAIT_MENSUEL, 22.29, ETUDES_DEMI_FORFAIT, 11.15,
                ADOS_VAC_JOURNEE_REPAS, 9.09, ADOS_VAC_JOURNEE_SANS, 4.91,
                ADOS_VAC_DEMI_REPAS, 6.63, ADOS_VAC_DEMI_SANS, 2.44,
                ADOS_SORTIE_DEMI, 3.89, ADOS_SORTIE_JOURNEE, 6.48)));

        // --- Tranche D (9000 a 10999.99 euros) ---
        tarifs.add(new Tarif("D", 9000, 10999.99, map(
                REPAS, 3.51, ACCUEIL_JOURNEE, 10.55, ACCUEIL_DEMI_REPAS, 7.02,
                PERISCOLAIRE_MATIN_SOIR, 3.51, PERISCOLAIRE_MATIN_OU_SOIR, 1.76,
                ETUDES_FORFAIT_MENSUEL, 20.06, ETUDES_DEMI_FORFAIT, 10.03,
                ADOS_VAC_JOURNEE_REPAS, 6.49, ADOS_VAC_JOURNEE_SANS, 2.96,
                ADOS_VAC_DEMI_REPAS, 5.01, ADOS_VAC_DEMI_SANS, 1.51,
                ADOS_SORTIE_DEMI, 3.24, ADOS_SORTIE_JOURNEE, 5.40)));

        // --- Tranche E (7000 a 8999.99 euros) ---
        tarifs.add(new Tarif("E", 7000, 8999.99, map(
                REPAS, 2.92, ACCUEIL_JOURNEE, 8.82, ACCUEIL_DEMI_REPAS, 5.83,
                PERISCOLAIRE_MATIN_SOIR, 2.92, PERISCOLAIRE_MATIN_OU_SOIR, 1.48,
                ETUDES_FORFAIT_MENSUEL, 18.06, ETUDES_DEMI_FORFAIT, 9.03,
                ADOS_VAC_JOURNEE_REPAS, 5.18, ADOS_VAC_JOURNEE_SANS, 2.27,
                ADOS_VAC_DEMI_REPAS, 4.05, ADOS_VAC_DEMI_SANS, 1.13,
                ADOS_SORTIE_DEMI, 2.59, ADOS_SORTIE_JOURNEE, 4.32)));

        // --- Tranche F (5000 a 6999.99 euros) ---
        tarifs.add(new Tarif("F", 5000, 6999.99, map(
                REPAS, 2.16, ACCUEIL_JOURNEE, 6.50, ACCUEIL_DEMI_REPAS, 4.32,
                PERISCOLAIRE_MATIN_SOIR, 2.16, PERISCOLAIRE_MATIN_OU_SOIR, 1.08,
                ETUDES_FORFAIT_MENSUEL, 16.28, ETUDES_DEMI_FORFAIT, 8.13,
                ADOS_VAC_JOURNEE_REPAS, 3.90, ADOS_VAC_JOURNEE_SANS, 1.74,
                ADOS_VAC_DEMI_REPAS, 3.00, ADOS_VAC_DEMI_SANS, 0.87,
                ADOS_SORTIE_DEMI, 1.94, ADOS_SORTIE_JOURNEE, 3.24)));

        // --- Tranche F2 (3000 a 4999.99 euros) ---
        tarifs.add(new Tarif("F2", 3000, 4999.99, map(
                REPAS, 1.57, ACCUEIL_JOURNEE, 6.50, ACCUEIL_DEMI_REPAS, 4.32,
                PERISCOLAIRE_MATIN_SOIR, 2.16, PERISCOLAIRE_MATIN_OU_SOIR, 1.08,
                ETUDES_FORFAIT_MENSUEL, 16.28, ETUDES_DEMI_FORFAIT, 8.13,
                ADOS_VAC_JOURNEE_REPAS, 3.90, ADOS_VAC_JOURNEE_SANS, 1.74,
                ADOS_VAC_DEMI_REPAS, 3.00, ADOS_VAC_DEMI_SANS, 0.87,
                ADOS_SORTIE_DEMI, 1.94, ADOS_SORTIE_JOURNEE, 3.24)));

        // --- Tranche G (0 a 2999.99 euros) ---
        tarifs.add(new Tarif("G", 0, 2999.99, map(
                REPAS, 1.43, ACCUEIL_JOURNEE, 4.35, ACCUEIL_DEMI_REPAS, 2.85,
                PERISCOLAIRE_MATIN_SOIR, 1.43, PERISCOLAIRE_MATIN_OU_SOIR, 0.70,
                ETUDES_FORFAIT_MENSUEL, 14.62, ETUDES_DEMI_FORFAIT, 7.31,
                ADOS_VAC_JOURNEE_REPAS, 2.60, ADOS_VAC_JOURNEE_SANS, 1.17,
                ADOS_VAC_DEMI_REPAS, 2.02, ADOS_VAC_DEMI_SANS, 0.58,
                ADOS_SORTIE_DEMI, 1.30, ADOS_SORTIE_JOURNEE, 2.16)));

        return tarifs;
    }

    private static String getValeurTexte(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        return "";
    }

    private static double getValeurNumerique(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.FORMULA) return cell.getNumericCellValue();
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
