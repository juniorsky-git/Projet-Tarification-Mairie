package fr.mairie.tarification_api;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion des données budgétaires dynamiques.
 *
 * Supporte deux sources de données sélectionnables via le paramètre `source` :
 *
 * SOURCE A (par défaut) :
 *   - Dépenses : CALC DEP(4).xlsx (via Calculateur)
 *   - Recettes : Depenses recettes nf.xlsx (onglet "recettes")
 *
 * SOURCE B (VF_REC_DEP.xlsx) — Approche hybride :
 *   - Dépenses des pôles RE/SC/CL : lues depuis VF_REC_DEP.xlsx (onglet "DEP")
 *   - Recettes des pôles RE/SC/CL : lues depuis VF_REC_DEP.xlsx (onglet "REC")
 *   - Pôles absents de VF_REC_DEP (Périscolaire, Études, Ados, Séjours) :
 *     repli automatique sur Source A (CALC DEP(4) + Depenses recettes nf.xlsx)
 *
 * @author Stagiaire DG 2
 */
@Service
public class DonneesBudgetaires {

    private final Calculateur calculateur = new Calculateur();

    // --- PÔLES NATIFS DE LA SOURCE B ---
    private static final String POLE_RESTAURATION  = "Restauration";
    private static final String POLE_SCOLAIRE      = "Scolaire";
    private static final String POLE_LOISIRS       = "Accueil de Loisirs";

    // --- PÔLES DE SOURCE A (utilisés en repli dans Source B) ---
    private static final String[] POLES_SOURCE_A = {
        "Restauration", "Accueil de Loisirs", "Accueil periscolaire",
        "Etudes surveillees", "Espace Ados", "Sejours"
    };

    // =========================================================================
    // API PUBLIQUE — avec routage selon la source
    // =========================================================================

    /**
     * Charge la liste des pôles budgétaires selon la source active.
     * @param source "A" (défaut) ou "B" (VF_REC_DEP.xlsx + repli A)
     */
    public List<DepensePole> chargerPolesDynamiques(String source) {
        if ("B".equalsIgnoreCase(source)) {
            return chargerPolesDynamiquesHybride();
        }
        return chargerPolesDynamiquesSourceA();
    }

    /** Surcharge sans paramètre — Source A par défaut. */
    public List<DepensePole> chargerPolesDynamiques() {
        return chargerPolesDynamiquesSourceA();
    }

    /**
     * Charge les recettes réelles depuis la source active.
     * @param source "A" (défaut) ou "B"
     */
    public Map<String, Double> chargerRecettesReelles(String source) {
        if ("B".equalsIgnoreCase(source)) {
            return chargerRecettesReellesHybride();
        }
        return chargerRecettesReellesSourceA();
    }

    /** Surcharge sans paramètre — Source A par défaut. */
    public Map<String, Double> chargerRecettesReelles() {
        return chargerRecettesReellesSourceA();
    }

    // =========================================================================
    // SOURCE A — Logique originale (CALC DEP(4) + Depenses recettes nf.xlsx)
    // =========================================================================

    private List<DepensePole> chargerPolesDynamiquesSourceA() {
        List<DepensePole> poles = new ArrayList<>();
        Calculateur.SyntheseGlobale sg = calculateur.getSynthese();

        for (String nom : POLES_SOURCE_A) {
            double total = sg.totauxDepenses.getOrDefault(nom, 0.0);
            Map<String, Double> charges = sg.depenses.getOrDefault(nom, Map.of());

            double effectifTotal = sg.effectifs.values().stream()
                    .mapToDouble(Double::doubleValue).sum();
            double coutUnitaire = (effectifTotal > 0) ? (total / effectifTotal) : 0;

            poles.add(new DepensePole(
                nom, total, coutUnitaire, (int) effectifTotal,
                nom.equals(POLE_RESTAURATION) ? 157920 : null,
                charges,
                Map.copyOf(calculerDistributionTranches(sg))
            ));
        }
        return poles;
    }

    private Map<String, Double> chargerRecettesReellesSourceA() {
        Map<String, Double> recettes = new HashMap<>();
        try {
            java.io.File file = trouverFichier("Depenses recettes nf.xlsx");
            if (file == null) return recettes;

            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                 org.apache.poi.ss.usermodel.Workbook workbook =
                         org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {

                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("recettes");
                if (sheet == null) return recettes;

                int[] coords = localiserTableauSynthetique(sheet, "Tableau synthetique des recettes", 50, 10);
                if (coords == null) return recettes;

                int rowStart = coords[0], colStart = coords[1];
                org.apache.poi.ss.usermodel.Row rowTotal = sheet.getRow(rowStart + 4);
                if (rowTotal == null) return recettes;

                recettes.put(POLE_RESTAURATION,   getCell(rowTotal, colStart + 1));
                recettes.put(POLE_LOISIRS,         getCell(rowTotal, colStart + 2));
                recettes.put("Accueil periscolaire", getCell(rowTotal, colStart + 3));
                recettes.put("Etudes surveillees",   getCell(rowTotal, colStart + 4));
                recettes.put("Espace Ados",           getCell(rowTotal, colStart + 5));
                // Séjours + Classes de découverte (colonnes 6 et 7 fusionnées)
                recettes.put("Sejours",
                        getCell(rowTotal, colStart + 6) + getCell(rowTotal, colStart + 7));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recettes;
    }

    // =========================================================================
    // SOURCE B — Hybride (VF_REC_DEP.xlsx + repli Source A pour pôles absents)
    // =========================================================================

    /**
     * Charge les pôles en mode hybride :
     * - RE, SC, CL depuis VF_REC_DEP.xlsx (onglet DEP)
     * - Périscolaire, Études, Ados, Séjours depuis Source A en repli
     */
    private List<DepensePole> chargerPolesDynamiquesHybride() {
        List<DepensePole> poles = new ArrayList<>();

        // Pôles natifs de Source B
        poles.addAll(chargerPolesDepuisVfRecDep());

        // Repli Source A pour les pôles absents de VF_REC_DEP
        List<DepensePole> polesSourceA = chargerPolesDynamiquesSourceA();
        String[] polesRepli = {"Accueil periscolaire", "Etudes surveillees", "Espace Ados", "Sejours"};
        for (String nomRepli : polesRepli) {
            polesSourceA.stream()
                .filter(p -> p.nom().equalsIgnoreCase(nomRepli))
                .findFirst()
                .ifPresent(poles::add);
        }

        return poles;
    }

    /**
     * Lit les trois pôles natifs (RE, SC, CL) depuis VF_REC_DEP.xlsx onglet "DEP".
     *
     * Structure du fichier :
     *   - DEPENSES RE  : ligne 2 (header: 3), total: ligne 21 col 22, détails: L4-20 col 22, effectif: L22 col 3
     *   - DEPENSES SC  : ligne 28 (header: 29), total: ligne 46 col 11, détails: L30-45 col 11, effectif: L47 col 11
     *   - DEPENSES CL  : ligne 51 (header: 52), total: ligne 65 col 7,  détails: L53-64 col 7,  effectif: L66 col 7
     */
    private List<DepensePole> chargerPolesDepuisVfRecDep() {
        List<DepensePole> poles = new ArrayList<>();
        try {
            java.io.File file = trouverFichier("VF_REC_DEP.xlsx");
            if (file == null) {
                // Si le fichier n'existe pas, on retourne des pôles vides (le repli Source A prendra le relais)
                return poles;
            }

            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                 org.apache.poi.ss.usermodel.Workbook workbook =
                         org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {

                org.apache.poi.ss.usermodel.Sheet dep = workbook.getSheet("DEP");
                if (dep == null) return poles;

                // --- RESTAURATION (RE) ---
                // Total : ligne 21 (index 20), colonne 22 (index 21)
                // Détails : lignes 4 à 20 (index 3..19), colonne 22 (index 21)
                // Effectif : ligne 22 (index 21), colonne 3 (index 2)
                {
                    double total    = getCellByIndex(dep, 20, 21);
                    int effectif    = (int) getCellByIndex(dep, 21, 2);
                    double coutUnit = effectif > 0 ? total / effectif : 0;
                    Map<String, Double> charges = lireCharges(dep, 3, 19, 0, 21);
                    poles.add(new DepensePole(
                        POLE_RESTAURATION, total, coutUnit, effectif,
                        effectif, charges, Map.of()
                    ));
                }

                // --- SCOLAIRE (SC) ---
                // Total : ligne 46 (index 45), colonne 11 (index 10)
                // Détails : lignes 30 à 45 (index 29..44), colonne 11 (index 10)
                // Effectif : ligne 47 (index 46), colonne 11 (index 10)
                {
                    double total    = getCellByIndex(dep, 45, 10);
                    int effectif    = (int) getCellByIndex(dep, 46, 10);
                    double coutUnit = effectif > 0 ? total / effectif : 0;
                    Map<String, Double> charges = lireCharges(dep, 29, 44, 0, 10);
                    poles.add(new DepensePole(
                        POLE_SCOLAIRE, total, coutUnit, effectif,
                        effectif, charges, Map.of()
                    ));
                }

                // --- ACCUEIL DE LOISIRS / CENTRE DE LOISIRS (CL) ---
                // Total : ligne 65 (index 64), colonne 7 (index 6)
                // Détails : lignes 53 à 64 (index 52..63), colonne 7 (index 6)
                // Effectif : ligne 66 (index 65), colonne 7 (index 6)
                {
                    double total    = getCellByIndex(dep, 64, 6);
                    int effectif    = (int) getCellByIndex(dep, 65, 6);
                    double coutUnit = effectif > 0 ? total / effectif : 0;
                    Map<String, Double> charges = lireCharges(dep, 52, 63, 0, 6);
                    poles.add(new DepensePole(
                        POLE_LOISIRS, total, coutUnit, effectif,
                        effectif, charges, Map.of()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return poles;
    }

    /**
     * Recettes hybrides :
     * - Pour RE, SC, CL : lire depuis VF_REC_DEP.xlsx onglet "REC" (tableau synthétique)
     * - Pour les pôles manquants : repli sur Source A (Depenses recettes nf.xlsx)
     */
    private Map<String, Double> chargerRecettesReellesHybride() {
        // Démarrer avec toutes les recettes Source A (base de repli)
        Map<String, Double> recettes = new HashMap<>(chargerRecettesReellesSourceA());

        try {
            java.io.File file = trouverFichier("VF_REC_DEP.xlsx");
            if (file == null) return recettes; // repli total sur Source A

            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                 org.apache.poi.ss.usermodel.Workbook workbook =
                         org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {

                org.apache.poi.ss.usermodel.Sheet rec = workbook.getSheet("REC");
                if (rec == null) return recettes;

                // Localiser "Tableau synthetique des recettes" dans l'onglet REC
                int[] coords = localiserTableauSynthetique(rec, "Tableau synthetique des recettes", 30, 15);
                if (coords == null) return recettes;

                int rowStart = coords[0], colStart = coords[1];
                // La ligne de total est 4 lignes plus bas (même structure que Source A)
                org.apache.poi.ss.usermodel.Row rowTotal = rec.getRow(rowStart + 4);
                if (rowTotal == null) return recettes;

                // Colonnes : colStart+1=Restauration, +2=Loisirs, +3=Péri, +4=Études, +5=Ados, +6=Séjours, +7=Découverte
                // On écrase RE et CL avec les valeurs Source B
                double recRe = getCell(rowTotal, colStart + 1);
                double recCl = getCell(rowTotal, colStart + 2);

                if (recRe > 0) recettes.put(POLE_RESTAURATION, recRe);
                if (recCl > 0) recettes.put(POLE_LOISIRS, recCl);

                // Scolaire : pas de colonne directe dans le tableau synthétique REC,
                // on cherche la valeur RESTSCOLL (onglet REC, ligne 8, colonne C = index 2)
                double recSc = getCellByIndex(rec, 7, 2); // Ligne 8 col C
                recettes.put(POLE_SCOLAIRE, recSc > 0 ? recSc : 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recettes;
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    /**
     * Lit les charges détaillées d'une section Excel.
     * Pour chaque ligne dans [rowFromIdx..rowToIdx], si col 0 (description) n'est pas vide
     * ET la colonne totalColIdx a une valeur > 0, on ajoute l'entrée.
     */
    private Map<String, Double> lireCharges(
            org.apache.poi.ss.usermodel.Sheet sheet,
            int rowFromIdx, int rowToIdx,
            int descColIdx, int totalColIdx) {

        Map<String, Double> charges = new HashMap<>();
        for (int r = rowFromIdx; r <= rowToIdx; r++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
            if (row == null) continue;

            org.apache.poi.ss.usermodel.Cell descCell = row.getCell(descColIdx);
            if (descCell == null || descCell.toString().isBlank()) continue;

            double montant = getCellByIndex(sheet, r, totalColIdx);
            if (montant > 0) {
                String label = descCell.toString().trim();
                charges.put(label, montant);
            }
        }
        return charges;
    }

    /**
     * Localise la position d'un tableau synthétique dans un onglet
     * en cherchant une chaîne de texte dans les maxRows premières lignes / maxCols colonnes.
     * @return int[]{rowIndex, colIndex} ou null si non trouvé
     */
    private int[] localiserTableauSynthetique(
            org.apache.poi.ss.usermodel.Sheet sheet,
            String marqueur, int maxRows, int maxCols) {

        for (int r = 0; r < maxRows; r++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = 0; c < maxCols; c++) {
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                if (cell != null && cell.toString().contains(marqueur)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    /**
     * Résout le chemin d'un fichier Excel en testant plusieurs emplacements relatifs.
     * @return java.io.File existant, ou null si introuvable
     */
    private java.io.File trouverFichier(String nomFichier) {
        java.io.File f = new java.io.File(nomFichier);
        if (f.exists()) return f;
        f = new java.io.File("../" + nomFichier);
        if (f.exists()) return f;
        f = new java.io.File("tarification-api/" + nomFichier);
        if (f.exists()) return f;
        return null;
    }

    /** Valeur numérique d'une cellule par index de ligne et de colonne (0-indexé). */
    private double getCellByIndex(
            org.apache.poi.ss.usermodel.Sheet sheet, int rowIdx, int colIdx) {
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIdx);
        if (row == null) return 0.0;
        return getCell(row, colIdx);
    }

    /** Valeur numérique d'une cellule (retourne 0.0 si null ou non-numérique). */
    private double getCell(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return 0.0;
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case FORMULA:
                    // Les cellules formule (SUM, etc.) stockent leur résultat numérique en cache
                    return cell.getNumericCellValue();
                case STRING:
                    String s = cell.getStringCellValue().trim().replace(",", ".").replace(" ", "");
                    return s.isEmpty() ? 0.0 : Double.parseDouble(s);
                default:
                    return 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getCell(org.apache.poi.ss.usermodel.Row row, int colIdx) {
        return getCell(row.getCell(colIdx));
    }

    /** Calcule la distribution des tranches QF depuis la synthèse Source A. */
    private Map<String, Integer> calculerDistributionTranches(Calculateur.SyntheseGlobale sg) {
        Map<String, Integer> dist = new HashMap<>();
        sg.effectifs.forEach((tranche, nb) -> dist.put(tranche, nb.intValue()));
        return dist;
    }
}
