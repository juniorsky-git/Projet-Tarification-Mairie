package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe dédiée à la lecture des fichiers Excel (.xlsx / .xls).
 *
 * Structure attendue du fichier (ex: Classeur1.xlsx) — partie Restauration :
 *
 *  Ligne 1 : Titre (ignorée)
 *  Ligne 2 : En-têtes de colonnes (ignorée)
 *  Ligne 3 : Ligne vide (ignorée)
 *  Lignes 4+ : Données des tranches
 *
 *  Pour chaque ligne de données :
 *   Colonne A (0) : Libellé de la tranche (ex: "Plus de 18 000€")
 *   Colonne B (1) : Code tranche             (ex: A, B, B2, C, D, E, F, F2, G, EXT)  ← clé principale
 *   Colonne C (2) : Prix repas facturé        (ex: 5.13)                               ← tarif appliqué
 *   Colonne D (3) : Nombre d'enfants (usagers)(ex: 429)                                ← volume
 *   Colonne E (4) : Coût moyen réel d'un repas(ex: 4.42)                               ← coût réel unitaire
 *   Colonne F (5) : Dépense annuelle en repas (ex: 265465.20)                          ← dépenses totales
 *   Colonne G (6) : Recette annuelle en repas (ex: 308107.80)                          ← recettes
 *   Colonne H (7) : Écart Recettes-Dépenses    (ex: 42642.60)                          ← bilan
 *   Colonne I (8) : Taux de couverture (%)     (ex: 116.06)                            ← indicateur clé
 */
public class ExcelReader {

    // Index des colonnes dans le fichier Excel (Classeur1.xlsx - onglet principal)
    private static final int COL_LIBELLE_TRANCHE = 0; // A : libellé humain
    private static final int COL_CODE_TRANCHE    = 1; // B : code tranche (A, B, B2...)
    private static final int COL_PRIX_REPAS      = 2; // C : prix repas facturé
    private static final int COL_NB_USAGERS      = 3; // D : nombre d'enfants
    private static final int COL_COUT_MOYEN      = 4; // E : coût moyen réel d'un repas
    private static final int COL_DEPENSES        = 5; // F : dépenses annuelles
    private static final int COL_RECETTES        = 6; // G : recettes annuelles
    private static final int COL_ECART           = 7; // H : écart (recettes - dépenses)
    private static final int COL_TAUX_COUVERTURE = 8; // I : taux de couverture %

    // Ligne à partir de laquelle les données commencent (0-indexé, donc ligne 4 = index 3)
    private static final int PREMIERE_LIGNE_DONNEES = 3;

    /**
     * Lit un fichier Excel et retourne une liste de Tarif chargés.
     * Supporte les formats .xlsx et .xls.
     *
     * @param cheminFichier chemin vers le fichier Excel
     * @return liste des tarifs extraits
     */
    public static List<Tarif> lire(String cheminFichier) {
        List<Tarif> tarifs = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(cheminFichier);
             Workbook workbook = ouvrirWorkbook(cheminFichier, fis)) {

            // On lit le premier onglet du fichier
            Sheet sheet = workbook.getSheetAt(0);
            System.out.println("Onglet lu : \"" + sheet.getSheetName() + "\" (" + cheminFichier + ")");

            for (int i = PREMIERE_LIGNE_DONNEES; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Arrêt si on rencontre une ligne "Total" ou une ligne vide de code tranche
                String codeTrancheRaw = valeurTexte(row.getCell(COL_CODE_TRANCHE));
                if (codeTrancheRaw.isEmpty()) continue; // ligne vide → on passe
                if (codeTrancheRaw.equalsIgnoreCase("Total")) break; // fin du tableau

                String codeTranche = codeTrancheRaw.trim();

                // On ne garde que les codes de tranche officiels (A, B, B2, C, D, E, F, F2, G, EXT)
                if (!estCodeTranche(codeTranche)) continue;

                double prixRepas  = valeurNumerique(row.getCell(COL_PRIX_REPAS));
                int    nbUsagers  = (int) valeurNumerique(row.getCell(COL_NB_USAGERS));
                double depenses   = valeurNumerique(row.getCell(COL_DEPENSES));
                double recettes   = valeurNumerique(row.getCell(COL_RECETTES));

                // Construction de la Map des tarifs pour cette tranche (pour l'instant : repas uniquement)
                Map<String, Double> tarifsMap = new HashMap<>();
                tarifsMap.put(DonneesTarifs.REPAS, prixRepas);

                double qfMin = DonneesTarifs.getQfMinPublic(codeTranche);
                double qfMax = DonneesTarifs.getQfMaxPublic(codeTranche);

                tarifs.add(new Tarif(codeTranche, qfMin, qfMax, tarifsMap, nbUsagers, recettes));

                System.out.printf("  Ligne %d → Tranche %-3s | Repas: %.2f € | Usagers: %4d | Recettes: %10.2f € | Dépenses: %10.2f €%n",
                        i + 1, codeTranche, prixRepas, nbUsagers, recettes, depenses);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier Excel : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la lecture du fichier Excel : " + e.getMessage());
            e.printStackTrace();
        }

        return tarifs;
    }

    // =========================================================================
    // UTILITAIRES PRIVÉS
    // =========================================================================

    /**
     * Ouvre un Workbook selon l'extension du fichier (.xlsx → XSSF, .xls → HSSF).
     */
    private static Workbook ouvrirWorkbook(String chemin, FileInputStream fis) throws IOException {
        if (chemin.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (chemin.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new IllegalArgumentException("Format de fichier non supporté : " + chemin + " (doit être .xlsx ou .xls)");
        }
    }

    /**
     * Retourne true si la valeur est un code de tranche QF officiel.
     * Permet de filtrer les lignes parasites du fichier (titres de sections, lignes Total, etc.)
     */
    private static boolean estCodeTranche(String code) {
        switch (code) {
            case "EXT":
            case "A":
            case "B":
            case "B2":
            case "C":
            case "D":
            case "E":
            case "F":
            case "F2":
            case "G":
                return true;
            default:
                return false;
        }
    }

    /**
     * Extrait la valeur numérique d'une cellule Excel, quelle que soit son type.
     * Retourne 0.0 si la cellule est vide ou non numérique.
     */
    private static double valeurNumerique(Cell cell) {
        if (cell == null) return 0.0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                // Essaie de parser un texte de type "5,13 €" ou "1 234,56"
                String val = cell.getStringCellValue()
                        .replaceAll("[^0-9,\\.-]", "")
                        .replace(',', '.');
                // Supprime les espaces qui peuvent apparaître comme séparateurs de milliers
                val = val.replaceAll("\\s", "");
                if (val.isEmpty()) return 0.0;
                try {
                    return Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }

    /**
     * Extrait la valeur textuelle d'une cellule, retourne "" si vide ou null.
     */
    private static String valeurTexte(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BLANK:   return "";
            default:      return "";
        }
    }
}
