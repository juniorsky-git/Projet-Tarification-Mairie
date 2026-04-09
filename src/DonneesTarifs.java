import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DonneesTarifs {

    // Ancienne méthode pour respecter le code pré-existant sans tout casser
    public static List<Tarif> chargerTarifs() {
        return chargerTarifs("Donnees/Tableau-grille/Classeur1.xlsx");
    }

    public static List<Tarif> chargerTarifs(String cheminFichier) {
        List<Tarif> tarifs = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(cheminFichier));
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // On boucle à partir de l'index 3 (parce que les données commencent ligne 4
            // dans l'Excel)
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row ligne = sheet.getRow(i);
                if (ligne == null) {
                    continue;
                }

                Cell premiereCellule = ligne.getCell(0);
                if (premiereCellule != null && premiereCellule.getCellType() == CellType.STRING) {
                    if (premiereCellule.getStringCellValue().startsWith("Total")) {
                        break; // Fin du tableau
                    }
                }

                // Colonne Tranche (B = index 1, fallback A = index 0)
                String tranche = "";
                Cell cellTranche = ligne.getCell(1);
                if (cellTranche != null && cellTranche.getCellType() == CellType.STRING
                        && !cellTranche.getStringCellValue().trim().isEmpty()) {
                    tranche = cellTranche.getStringCellValue().trim();
                } else if (premiereCellule != null && premiereCellule.getCellType() == CellType.STRING) {
                    tranche = premiereCellule.getStringCellValue().trim();
                }

                if (!tranche.isEmpty()) {
                    double repas = parseCellDouble(ligne.getCell(2)); // Col C = 2
                    int usagers = (int) parseCellDouble(ligne.getCell(3)); // Col D = 3
                    double depenses = parseCellDouble(ligne.getCell(5)); // Col F = 5
                    double recettes = parseCellDouble(ligne.getCell(6)); // Col G = 6

                    double qfMin = getQfMin(tranche);
                    double qfMax = getQfMax(tranche);

                    tarifs.add(new Tarif(tranche, qfMin, qfMax, repas, 0, 0, 0, 0, usagers, depenses, recettes));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur fatale de lecture du fichier Excel avec Apache POI : " + e.getMessage());
        }

        return tarifs;
    }

    private static double parseCellDouble(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.FORMULA) {
                // Dans Excel, les champs "Dépense" et "Recette" sont souvent le résultat de `A4 * B4`
                // getNumericCellValue extraira le dernier cache du résultat pré-calculé 
                try {
                    return cell.getNumericCellValue();
                } catch (Exception ex) {
                    // Si l'évaluation échoue (par exemple type chaîne issu d'une formule)
                    String stringResult = cell.getStringCellValue().replaceAll("[^0-9,\\.-]", "").replace(',', '.');
                    if (!stringResult.isEmpty()) return Double.parseDouble(stringResult);
                }
            } else if (cell.getCellType() == CellType.STRING) {
                // Repli si jamais une cellule contient "5,54 €" en mode texte pur
                String text = cell.getStringCellValue().replaceAll("[^0-9,\\.-]", "").replace(',', '.');
                if (!text.isEmpty()) {
                    return Double.parseDouble(text);
                }
            }
        } catch (Exception ignored) {
            // Sécurité anti-crash
        }
        
        return 0.0;
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