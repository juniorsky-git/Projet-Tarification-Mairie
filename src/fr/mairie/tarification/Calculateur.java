package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Moteur de calcul financier intelligent pour le multi-pôle.
 */
public class Calculateur {

    private static final String FICHIER_DEPENSES = "Donnees/Autres/CALC DEP.xlsx";
    private static final String FICHIER_VOLUMES  = "Donnees/Autres/Feuille_dataviz .xlsx";

    private static final int COL_DEP_MONTANT_TTC = 7;
    private static final int COL_DEP_SERVICE     = 18; // Colonne S
    private static final int COL_DEP_ANTENNE     = 19; // Colonne T
    private static final int COL_VOL_CODE_TRANCHE = 1;
    private static final int COL_VOL_NB_ENFANTS   = 3;

    /**
     * Calcule les dépenses pour un pôle avec des règles d'inclusion/exclusion fines.
     */
    public double calculerDepensesPole(String antennaPrecise, String serviceType, String motCleLibelle, String[] exclusions) {
        double total = 0;
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            Sheet s = wb.getSheetAt(0);
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String ant = getValeurTexte(row.getCell(COL_DEP_ANTENNE));
                String ser = getValeurTexte(row.getCell(COL_DEP_SERVICE));
                String lib = getValeurTexte(row.getCell(3)).toUpperCase();

                boolean matchBase = false;
                // Si l'antenne correspond (ex: RESTMICH)
                if (antennaPrecise != null && ant.equalsIgnoreCase(antennaPrecise)) matchBase = true;
                // OU si le service correspond (ex: 2-RE)
                if (serviceType != null && ser.contains(serviceType)) matchBase = true;

                if (matchBase) {
                    // On vérifie le mot-clé obligatoire (ex: ADOS pour le pôle ados)
                    if (motCleLibelle != null && !lib.contains(motCleLibelle.toUpperCase())) continue;

                    // On vérifie les exclusions (ex: ne pas mettre LOISIRS dans le SCOLAIRE)
                    boolean exclu = false;
                    if (exclusions != null) {
                        for (String ex : exclusions) {
                            if (lib.contains(ex.toUpperCase())) {
                                exclu = true;
                                break;
                            }
                        }
                    }

                    if (!exclu) {
                        total += Math.abs(getValeurNumerique(row.getCell(COL_DEP_MONTANT_TTC)));
                    }
                }
            }
        } catch (Exception e) {}
        return total;
    }

    public Map<String, Double> chargerEffectifs(int ligneDepart) {
        Map<String, Double> effectifs = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_VOLUMES);
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheetAt(0);
            for (int i = ligneDepart; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;
                String code = getValeurTexte(row.getCell(COL_VOL_CODE_TRANCHE));
                String desc = getValeurTexte(row.getCell(0));
                
                // On arrête si on trouve un titre de section suivante ou Total
                if (desc.contains("RESTAURATION") || desc.contains("LOISIRS") || desc.contains("ADOS") || "Total".equalsIgnoreCase(desc)) {
                    if (i > ligneDepart + 1) break; 
                }

                double nb = getValeurNumerique(row.getCell(COL_VOL_NB_ENFANTS));
                if (nb > 0) {
                    String key = (code == null || code.isEmpty()) ? desc : code;
                    effectifs.put(key, nb);
                }
            }
        } catch (Exception e) {}
        return effectifs;
    }

    public double calculerRecettes(Map<String, Double> effectifs, String cleTarif, double multiplicateur) {
        double total = 0;
        List<Tarif> grille = DonneesTarifs.chargerTarifsReference();
        for (Map.Entry<String, Double> entry : effectifs.entrySet()) {
            for (Tarif t : grille) {
                if (t.getTranche().equalsIgnoreCase(entry.getKey())) {
                    total += t.getPrix(cleTarif) * entry.getValue() * multiplicateur;
                    break;
                }
            }
        }
        return total;
    }

    private String getValeurTexte(Cell c) { return (c == null) ? "" : c.toString().trim(); }
    private double getValeurNumerique(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            return Double.parseDouble(c.toString().trim());
        } catch (Exception e) { return 0; }
    }
}
