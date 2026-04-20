package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Lecteur de l'onglet "Simulation" du fichier CALC DEP(4).xlsx.
 *
 * Cette classe lit les données de simulation budgétaire pour la restauration :
 * tranches, prix facturés, nombre d'enfants, coût moyen, dépenses, recettes,
 * écart et taux de couverture.
 *
 * @author Séri-khane YOLOU
 * @version 1.0
 */
public class SimulationCalculateur {

    /** Chemin vers le fichier Excel contenant les données de simulation. */
    private final String fichierExcel;

    /** Nom exact de l'onglet à lire dans le classeur. */
    private static final String ONGLET_SIMULATION = "Simulation";

    /**
     * Construit un calculateur pointant vers le fichier Excel indiqué.
     *
     * @param fichierExcel Chemin relatif ou absolu vers CALC DEP(4).xlsx.
     */
    public SimulationCalculateur(String fichierExcel) {
        this.fichierExcel = fichierExcel;
    }

    /**
     * Lit l'onglet "Simulation" et retourne la liste des lignes budgétaires
     * pour la restauration scolaire.
     *
     * Les lignes sont lues à partir de la ligne 7 (index 6) jusqu'à la ligne 16
     * (index 15). Si une ligne ne possède pas de code tranche, elle est ignorée.
     *
     * @return Liste de {@link SimulationLigne} extraites du fichier, jamais null.
     */
    public List<SimulationLigne> lireSimulationRestauration() {
        List<SimulationLigne> lignes = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(fichierExcel);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet(ONGLET_SIMULATION);
            if (s == null) {
                System.err.println("[SimulationCalculateur] Onglet '" + ONGLET_SIMULATION + "' introuvable dans " + fichierExcel);
                return lignes;
            }

            // Lignes 7 à 16 du fichier Excel (index 6 à 15, base 0)
            // À ajuster si besoin selon la vraie zone de l'onglet Simulation
            for (int i = 6; i <= 15; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                SimulationLigne l = new SimulationLigne();
                l.tranche        = getValeurTexte(row.getCell(0));
                l.codeTranche    = getValeurTexte(row.getCell(1));
                l.prixFacture    = getValeurNumerique(row.getCell(2));
                l.nombreEnfants  = getValeurNumerique(row.getCell(3));
                l.coutMoyen      = getValeurNumerique(row.getCell(4));
                l.depenseAnnuelle = getValeurNumerique(row.getCell(5));
                l.recetteAnnuelle = getValeurNumerique(row.getCell(6));
                l.ecart          = getValeurNumerique(row.getCell(7));
                l.tauxCouverture = getValeurNumerique(row.getCell(8));

                // On n'ajoute la ligne que si le code tranche est renseigné
                if (!l.codeTranche.isEmpty()) {
                    lignes.add(l);
                }
            }

        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur lecture onglet Simulation : " + e.getMessage());
            e.printStackTrace();
        }

        return lignes;
    }

    /**
     * Extrait la valeur textuelle d'une cellule.
     *
     * @param c La cellule à lire (peut être null).
     * @return Le texte de la cellule, ou une chaîne vide si null.
     */
    private String getValeurTexte(Cell c) {
        if (c == null) return "";
        return c.toString().trim();
    }

    /**
     * Extrait la valeur numérique d'une cellule, avec gestion des formules
     * et du texte (virgule → point).
     *
     * @param c La cellule à lire (peut être null).
     * @return La valeur numérique, ou 0 en cas d'absence ou d'erreur.
     */
    private double getValeurNumerique(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            if (c.getCellType() == CellType.FORMULA)  return c.getNumericCellValue();
            String s = c.toString().trim().replace(",", ".");
            if (s.isEmpty()) return 0;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
