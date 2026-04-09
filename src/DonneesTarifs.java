import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DonneesTarifs {

    // Méthode par défaut vers le fichier connu
    public static List<Tarif> chargerTarifs() {
        return chargerTarifs("Donnees/Tableau-grille/Classeur1.xlsx");
    }

    public static List<Tarif> chargerTarifs(String cheminFichier) {
        List<Tarif> tarifs = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(cheminFichier));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Evaluateur POI : détecte RÉELLEMENT les résultats des formules Excel
            FormulaEvaluator evaluateur = workbook.getCreationHelper().createFormulaEvaluator();

            // Ligne 4 dans l'Excel = index 3 en Java (base 0)
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row ligne = sheet.getRow(i);

                if (ligne == null) {
                    continue;
                }

                // Arrêt propre si on atteint la ligne "Total" ou une ligne vide
                if (estLigneFin(ligne)) {
                    break;
                }

                // Lecture de la Tranche (colonne B = index 1, fallback colonne A = index 0)
                String tranche = lireTrancheDepuisLigne(ligne);

                if (tranche.isEmpty()) {
                    continue;
                }

                // Lecture des valeurs avec l'évaluateur de formules
                double repas    = lireValeurCellule(ligne.getCell(2), evaluateur); // Col C
                int    usagers  = (int) lireValeurCellule(ligne.getCell(3), evaluateur); // Col D
                double depenses = lireValeurCellule(ligne.getCell(5), evaluateur); // Col F
                double recettes = lireValeurCellule(ligne.getCell(6), evaluateur); // Col G

                // Les bornes QF ne sont pas dans l'Excel : on les déduit de la tranche officielle
                double qfMin = borneQfMin(tranche);
                double qfMax = borneQfMax(tranche);

                tarifs.add(new Tarif(tranche, qfMin, qfMax, repas, 0, 0, 0, 0, usagers, depenses, recettes));
            }

        } catch (Exception e) {
            System.err.println("[DonneesTarifs] Erreur de lecture du fichier Excel : " + e.getMessage());
        }

        return tarifs;
    }

    // ─── MÉTHODES UTILITAIRES ───────────────────────────────────────────────────

    /**
     * Détermine si une ligne marque la fin du tableau utile.
     * Critères : ligne "Total" OU ligne entièrement composée de cellules vides.
     */
    private static boolean estLigneFin(Row ligne) {
        Cell c0 = ligne.getCell(0);
        if (c0 != null && c0.getCellType() == CellType.STRING
                && c0.getStringCellValue().trim().toLowerCase().startsWith("total")) {
            return true;
        }
        // Ligne vide si aucune cellule parmi les 9 premières n'a de valeur utile
        for (int col = 0; col < 9; col++) {
            Cell c = ligne.getCell(col);
            if (c != null && c.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lit la lettre de la tranche tarifaire depuis une ligne.
     * Priorité : colonne B (pour "A", "B", "B2"...) puis colonne A (pour "EXT").
     */
    private static String lireTrancheDepuisLigne(Row ligne) {
        Cell colB = ligne.getCell(1);
        if (colB != null && colB.getCellType() == CellType.STRING) {
            String val = colB.getStringCellValue().trim();
            if (!val.isEmpty()) {
                return val;
            }
        }
        Cell colA = ligne.getCell(0);
        if (colA != null && colA.getCellType() == CellType.STRING) {
            return colA.getStringCellValue().trim();
        }
        return "";
    }

    /**
     * Lit la valeur numérique d'une cellule, quelle que soit sa nature :
     * - Valeur numérique simple (ex: 5.54)
     * - Résultat d'une formule Excel évalué par POI (ex: =C4*D4*140)
     * - Texte contenant un nombre avec virgule ou symbole euro (ex: "5,54 €")
     */
    private static double lireValeurCellule(Cell cell, FormulaEvaluator evaluateur) {
        if (cell == null) {
            return 0.0;
        }
        try {
            CellValue valeur = evaluateur.evaluate(cell);
            if (valeur == null) {
                return 0.0;
            }

            switch (valeur.getCellType()) {
                case NUMERIC: {
                    return valeur.getNumberValue();
                }
                case STRING: {
                    String text = valeur.getStringValue().replaceAll("[^0-9,\\.-]", "").replace(',', '.');
                    if (!text.isEmpty()) {
                        return Double.parseDouble(text);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (Exception ignored) {
            // Anti-crash de sécurité
        }
        return 0.0;
    }

    // ─── CORRESPONDANCES TRANCHE → BORNES QF OFFICIELLES ───────────────────────

    private static double borneQfMin(String tranche) {
        switch (tranche) {
            case "EXT":
            case "A": {
                return 18000;
            }
            case "B": {
                return 15000;
            }
            case "B2": {
                return 13000;
            }
            case "C": {
                return 11000;
            }
            case "D": {
                return 9000;
            }
            case "E": {
                return 7000;
            }
            case "F": {
                return 5000;
            }
            case "F2": {
                return 3000;
            }
            case "G": {
                return 0;
            }
            default: {
                return 0;
            }
        }
    }

    private static double borneQfMax(String tranche) {
        switch (tranche) {
            case "EXT":
            case "A": {
                return 999999;
            }
            case "B": {
                return 17999.99;
            }
            case "B2": {
                return 14999.99;
            }
            case "C": {
                return 12999.99;
            }
            case "D": {
                return 10999.99;
            }
            case "E": {
                return 8999.99;
            }
            case "F": {
                return 6999.99;
            }
            case "F2": {
                return 4999.99;
            }
            case "G": {
                return 2999.99;
            }
            default: {
                return 0;
            }
        }
    }
}