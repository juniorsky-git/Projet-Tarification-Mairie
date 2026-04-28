package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Lecteur du fichier CALC DEP(4).xlsx — section Restauration.
 *
 * Lit directement l'onglet "CALC DEP(4)" via l'API Apache POI natif, sans besoin de CSV.
 *
 * @author Séri-khane YOLOU
 * @version 3.0 (POI)
 */
public class SimulationCalculateur {

    private final String fichierExcel;
    private static final String ONGLET_SIMULATION = "CALC DEP(4)";

    public SimulationCalculateur(String fichierExcel) {
        this.fichierExcel = fichierExcel;
    }

    public List<SimulationLigne> lireSimulationRestauration() {
        List<SimulationLigne> lignes = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(new java.io.File(fichierExcel))) {

            Sheet sheet = wb.getSheet(ONGLET_SIMULATION);
            if (sheet == null) {
                System.err.println("[SimulationCalculateur] Erreur : onglet introuvable : " + ONGLET_SIMULATION);
                return lignes;
            }

            // Lignes Excel 7 à 17 (index 6 à 16 dans POI)
            for (int i = 6; i <= 16; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String codeTranche = getTexte(row, 1);
                String labelTranche = getTexte(row, 0);
                
                // Si la colonne B est vide (comme pour EXT ou Total), on utilise la colonne A
                if (codeTranche.isEmpty()) {
                    codeTranche = labelTranche;
                }
                
                if (codeTranche.isEmpty()) continue;

                SimulationLigne s = new SimulationLigne();
                s.tranche         = labelTranche;
                s.codeTranche     = codeTranche;
                s.prixFacture     = getNombre(row, 2);
                s.nombreEnfants   = getNombre(row, 3);
                s.coutMoyen       = getNombre(row, 4);
                s.depenseAnnuelle = getNombre(row, 5);
                s.recetteAnnuelle = getNombre(row, 6);
                s.ecart           = getNombre(row, 7);
                s.tauxCouverture  = getNombrePourcentage(row, 8); // Taux

                lignes.add(s);
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur lecture Excel POI : " + e.getMessage());
            e.printStackTrace();
        }
        return lignes;
    }

    public double lireNombreEnfantsTotal() {
        try (Workbook wb = WorkbookFactory.create(new java.io.File(fichierExcel))) {
            Sheet sheet = wb.getSheet(ONGLET_SIMULATION);
            if (sheet != null) {
                Row row = sheet.getRow(16); // Excel ligne 17 (Total)
                if (row != null) {
                    return getNombre(row, 3);
                }
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur enfants total : " + e.getMessage());
        }
        return 0;
    }

    public Map<String, Double> lireDepensesReellesRestauration() {
        Map<String, Double> detail = new LinkedHashMap<>();
        try (Workbook wb = WorkbookFactory.create(new java.io.File(fichierExcel))) {
            Sheet sheet = wb.getSheet(ONGLET_SIMULATION);
            if (sheet != null) {
                Row row = sheet.getRow(33); // Excel ligne 34
                if (row != null) {
                    detail.put("Scolarest (prestations)", getNombre(row, 2));
                    detail.put("Personnel", getNombre(row, 3));
                    detail.put("Alimentation", getNombre(row, 4));
                    detail.put("Eau", getNombre(row, 5));
                    detail.put("Electricite", getNombre(row, 6));
                    detail.put("Gaz", getNombre(row, 7));
                    detail.put("TOTAL", getNombre(row, 8));
                }
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur depenses reelles : " + e.getMessage());
        }
        return detail;
    }

    // Helper method
    private Map<String, Double> lireTotalGeneral(int rowIndex, String[] etiquettes, int[] colonnes, int colonneTotal) {
        Map<String, Double> detail = new LinkedHashMap<>();
        try (Workbook wb = WorkbookFactory.create(new java.io.File(fichierExcel))) {
            Sheet sheet = wb.getSheet(ONGLET_SIMULATION);
            if (sheet != null) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    for (int i = 0; i < etiquettes.length; i++) {
                        double val = getNombre(row, colonnes[i]);
                        if (val != 0) detail.put(etiquettes[i], val);
                    }
                    detail.put("TOTAL", getNombre(row, colonneTotal));
                }
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur Helper POI : " + e.getMessage());
        }
        return detail;
    }

    public Map<String, Double> lireDepensesAccueilLoisirs() {
        return lireTotalGeneral(45, // Excel l.46
            new String[]{"Personnel", "Materiel", "Fournitures pedagogiques", "Materiel sportif", "Prestations (spectacles)", "Alimentation", "Transport", "Droits d'entree", "Electricite", "Eau", "Restauration", "Autres"},
            new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}, 15);
    }

    public Map<String, Double> lireDepensesEtudesSurveillees() {
        return lireTotalGeneral(59, // Excel l.60
            new String[]{"Personnel", "Fournitures scolaires", "Materiel"},
            new int[]{2, 3, 4}, 5);
    }

    public Map<String, Double> lireDepensesEspaceAdos() {
        return lireTotalGeneral(74, // Excel l.75
            new String[]{"Personnel", "Electricite", "Gaz", "Eau", "Materiel", "Fournitures", "Petit materiel", "Alimentation", "Autres", "Transport", "Droits d'entree"},
            new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, 13);
    }

    public Map<String, Double> lireDepensesSejours() {
        return lireTotalGeneral(94, // Excel l.95
            new String[]{"Personnel", "Transport (bus/mini bus)", "Peage/Parking", "Hebergement (centre vacances)", "Restauration", "Materiel", "Fournitures", "Autres"},
            new int[]{2, 3, 4, 5, 6, 7, 8, 9}, 10);
    }

    public Map<String, Double> lireDepensesAccueilPeriscolaire() {
        return lireTotalGeneral(111, // Excel l.112
            new String[]{"Personnel", "Fournitures", "Alimentation", "Eau", "Electricite", "Gaz"},
            new int[]{2, 3, 4, 5, 6, 7}, 8);
    }

    private String getTexte(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private double getNombre(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
            try { return cell.getNumericCellValue(); } catch(Exception e){ return 0;}
        }
        String txt = getTexte(row, index);
        try {
            txt = txt.replace(" ", "").replace("\u00A0", "").replace("€", "").replace("%", "").replace(",", ".");
            return Double.parseDouble(txt);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 
     * Assure le renvoi d'un ratio 0.xx pour les pourcentages, que la case POI 
     * soit correctement paramétrée en % ou qu'elle soit du texte.
     */
    private double getNombrePourcentage(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
            try { return cell.getNumericCellValue(); } catch(Exception e){ return 0;}
        }
        String txt = getTexte(row, index);
        try {
            txt = txt.replace(" ", "").replace("\u00A0", "").replace(",", ".");
            if (txt.contains("%")) {
                txt = txt.replace("%", "");
                return Double.parseDouble(txt) / 100.0;
            }
            return Double.parseDouble(txt) / 100.0; // Assume c'est un % texte formaté sans le sigle
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
