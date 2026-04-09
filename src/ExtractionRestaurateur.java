import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracteur spécialisé pour les fichiers d'export Ciril
 * de type "Restauration - Données pôle enfance".
 *
 * Structure attendue du fichier :
 *  - Ligne 1 : titre "Restauration - Données pôle enfance"
 *  - Ligne 2 : en-têtes des colonnes
 *  - Ligne 3 : vide
 *  - Lignes 4+ : données (une tranche par ligne)
 *  - Col A : libellé de la tranche de QF (texte long) ou "EXT"
 *  - Col B : lettre de la tranche (A, B, B2, C...)
 *  - Col C : prix du repas facturé
 *  - Col D : nombre d'enfants (usagers)
 *  - Col F : dépenses annuelles en repas
 *  - Col G : recettes annuelles en repas
 */
public class ExtractionRestaurateur implements IExtracteurCiril {

    @Override
    public List<Tarif> chargerTarifs(String cheminFichier) {
        List<Tarif> tarifs = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(cheminFichier));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluateur = workbook.getCreationHelper().createFormulaEvaluator();

            // Les données commencent à la ligne 4 (index 3 en base-0)
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row ligne = sheet.getRow(i);

                if (ligne == null) {
                    continue;
                }

                if (estLigneFin(ligne)) {
                    break;
                }

                String tranche = lireTrancheDepuisLigne(ligne);
                if (tranche.isEmpty()) {
                    continue;
                }

                double repas    = lireValeurCellule(ligne.getCell(2), evaluateur); // Col C
                int    usagers  = (int) lireValeurCellule(ligne.getCell(3), evaluateur); // Col D
                double depenses = lireValeurCellule(ligne.getCell(5), evaluateur); // Col F
                double recettes = lireValeurCellule(ligne.getCell(6), evaluateur); // Col G

                double qfMin = borneQfMin(tranche);
                double qfMax = borneQfMax(tranche);

                tarifs.add(new Tarif(tranche, qfMin, qfMax, repas, 0, 0, 0, 0, usagers, depenses, recettes));
            }

        } catch (Exception e) {
            System.err.println("[ExtractionRestaurateur] Erreur de lecture : " + e.getMessage());
        }

        return tarifs;
    }

    // ─── MÉTHODES UTILITAIRES ───────────────────────────────────────────────────

    private boolean estLigneFin(Row ligne) {
        Cell c0 = ligne.getCell(0);
        if (c0 != null && c0.getCellType() == CellType.STRING
                && c0.getStringCellValue().trim().toLowerCase().startsWith("total")) {
            return true;
        }
        for (int col = 0; col < 9; col++) {
            Cell c = ligne.getCell(col);
            if (c != null && c.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String lireTrancheDepuisLigne(Row ligne) {
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

    private double lireValeurCellule(Cell cell, FormulaEvaluator evaluateur) {
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
            // Anti-crash
        }
        return 0.0;
    }

    // ─── BORNES QF OFFICIELLES ──────────────────────────────────────────────────

    private double borneQfMin(String tranche) {
        switch (tranche) {
            case "EXT":
            case "A":  { return 18000; }
            case "B":  { return 15000; }
            case "B2": { return 13000; }
            case "C":  { return 11000; }
            case "D":  { return 9000;  }
            case "E":  { return 7000;  }
            case "F":  { return 5000;  }
            case "F2": { return 3000;  }
            case "G":  { return 0;     }
            default:   { return 0;     }
        }
    }

    private double borneQfMax(String tranche) {
        switch (tranche) {
            case "EXT":
            case "A":  { return 999999;   }
            case "B":  { return 17999.99; }
            case "B2": { return 14999.99; }
            case "C":  { return 12999.99; }
            case "D":  { return 10999.99; }
            case "E":  { return 8999.99;  }
            case "F":  { return 6999.99;  }
            case "F2": { return 4999.99;  }
            case "G":  { return 2999.99;  }
            default:   { return 0;        }
        }
    }
}
