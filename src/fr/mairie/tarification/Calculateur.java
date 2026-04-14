package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Moteur de calcul financier pour la tarification municipale.
 * 
 * Cette classe est le coeur du systeme. Elle extrait les donnees de l'onglet 
 * 'syntheses charges' du fichier Excel CALC DEP (3).xlsx qui sert de source 
 * de verite unique.
 * 
 * Elle gere le cache des donnees via la structure SyntheseGlobale pour optimiser
 * les performances et garantir la coherence entre les depenses (par nature) 
 * et les recettes (par tranche QF).
 * 
 * @author Séri-khane YOLOU (Refonte CALC DEP 3)
 * @version 1.2
 */
public class Calculateur {

    /** Chemin vers le fichier Excel source consolidé. */
    private static final String FICHIER_DEPENSES = "Donnees/Autres/CALC DEP (3).xlsx";
    
    /** Nom de l'onglet de synthèse servant de source de données. */
    private static final String ONGLET_SYNTHESE = "syntheses charges";

    /**
     * Structure interne regroupant l'integralite de la synthese chargee en memoire.
     */
    public static class SyntheseGlobale {
        /** Detail des depenses : Map<NomDuPole, Map<NatureComptable, Montant>>. */
        public Map<String, Map<String, Double>> depenses = new LinkedHashMap<>();
        
        /** Grille tarifaire : Map<TrancheQF, Map<NomDuPole, TarifUnitaire>>. */
        public Map<String, Map<String, Double>> tarifs = new LinkedHashMap<>();
        
        /** Effectifs par tranche : Map<TrancheQF, NombreEnfants>. */
        public Map<String, Double> effectifs = new LinkedHashMap<>();
        
        /** Totaux depenses consolides par pole : Map<NomDuPole, MontantTotal>. */
        public Map<String, Double> totauxDepenses = new HashMap<>();
    }

    /** Cache memoire pour eviter de relire le fichier Excel a chaque calcul. */
    private SyntheseGlobale syntheseCachee = null;

    /**
     * Charge l'integralite des donnees de l'onglet 'syntheses charges'.
     * 
     * Cette methode parcourt l'Excel ligne par ligne pour extraire :
     * 1. Les charges par nature (Personnel, Fluides, etc.) pour les 6 poles.
     * 2. Les effectifs totaux par tranche de Quotient Familial.
     * 3. Les tarifs applicables par tranche pour chaque service.
     * 
     * @return Un objet SyntheseGlobale contenant toutes les donnees indexees.
     */
    public SyntheseGlobale getSynthese() {
        if (syntheseCachee != null) {
            return syntheseCachee;
        }
        
        SyntheseGlobale sg = new SyntheseGlobale();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet(ONGLET_SYNTHESE);
            if (s == null) {
                return sg;
            }

            // Identification des poles et de leurs colonnes respectives dans l'Excel
            String[] poles = {"Restauration", "Accueil de Loisirs", "Accueil periscolaire", "Etudes surveillees", "Espace Ados", "Sejours"};
            int[] colIndices = {3, 4, 5, 6, 7, 8};

            // 1. EXTRACTION DES DEPENSES (Lignes 4 a 21 de l'onglet)
            for (int i = 3; i <= 20; i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // On recupere le libelle de la nature de dépense
                String nature = getValeurTexte(row.getCell(1));
                if (nature.isEmpty()) {
                    nature = getValeurTexte(row.getCell(0));
                }
                
                if (nature.isEmpty()) {
                    continue;
                }
                
                // Pour chaque pole, on stocke le montant associe a cette nature
                for (int j = 0; j < poles.length; j++) {
                    double montant = Math.abs(getValeurNumerique(row.getCell(colIndices[j])));
                    if (montant > 0) {
                        sg.depenses.computeIfAbsent(poles[j], k -> new LinkedHashMap<>()).put(nature, montant);
                    }
                }
            }

            // Extraction des Totaux de depense (Ligne 22 - Index 21)
            Row totalRow = s.getRow(21);
            if (totalRow != null) {
                for (int j = 0; j < poles.length; j++) {
                    sg.totauxDepenses.put(poles[j], getValeurNumerique(totalRow.getCell(colIndices[j])));
                }
            }

            // 2. EXTRACTION DES RECETTES & EFFECTIFS (Lignes 30 a 39 de l'onglet)
            for (int i = 29; i <= 38; i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    continue;
                }
                
                String tranche = getValeurTexte(row.getCell(1));
                if (tranche.isEmpty()) {
                    tranche = getValeurTexte(row.getCell(0));
                }
                
                // Stockage du volume d'enfants pour cette tranche
                sg.effectifs.put(tranche, getValeurNumerique(row.getCell(2)));
                
                // Stockage des tarifs pour chaque service
                for (int j = 0; j < poles.length; j++) {
                    sg.tarifs.computeIfAbsent(tranche, k -> new HashMap<>()).put(poles[j], getValeurNumerique(row.getCell(colIndices[j])));
                }
            }
            
            // Mise en cache pour les futurs appels
            syntheseCachee = sg;
            
        } catch (Exception e) {
            System.err.println("Erreur critique lors du chargement de la synthese : " + e.getMessage());
        }
        return sg;
    }

    /**
     * Calcule le total des depenses réelles pour un pôle donné.
     * 
     * @param pole Le nom du pôle (ex: "Restauration").
     * @return Le montant total TTC des charges constatées.
     */
    public double calculerTotalDepenses(String pole) {
        return getSynthese().totauxDepenses.getOrDefault(pole, 0.0);
    }

    /**
     * Calcule les recettes théoriques annuelles pour un pôle.
     * 
     * La formule appliquee est : Somme par tranche de (Nombre d'enfants * Tarif * Multiplicateur).
     * 
     * @param pole Le nom du pôle.
     * @param multiplicateur Le coefficient annuel (140j, 10 mois, ou 1 pour forfait).
     * @return Le total des recettes calculees.
     */
    public double calculerRecettesAnnuelles(String pole, double multiplicateur) {
        SyntheseGlobale sg = getSynthese();
        double total = 0;
        for (String tranche : sg.effectifs.keySet()) {
            double nb = sg.effectifs.getOrDefault(tranche, 0.0);
            double tarif = sg.tarifs.getOrDefault(tranche, new HashMap<>()).getOrDefault(pole, 0.0);
            total += nb * tarif * multiplicateur;
        }
        return total;
    }

    /**
     * Retourne le detail des depenses ventilees par nature comptable pour un pôle.
     * 
     * @param pole Le nom du pôle.
     * @return Une Map ordonnee (Nature -> Montant).
     */
    public Map<String, Double> getDepensesDetaillees(String pole) {
        return getSynthese().depenses.getOrDefault(pole, new LinkedHashMap<>());
    }

    // --- Methodes de compatibilite pour le menu principal ---

    /** Calcule les depenses de la Restauration. @return Montant en euros. */
    public double calculerDepensesRestauration() { return calculerTotalDepenses("Restauration"); }
    
    /** Calcule les depenses des Loisirs. @return Montant en euros. */
    public double calculerDepensesAccueilLoisirs() { return calculerTotalDepenses("Accueil de Loisirs"); }
    
    /** Calcule les depenses du Periscolaire. @return Montant en euros. */
    public double calculerDepensesAccueilPeriscolaire() { return calculerTotalDepenses("Accueil periscolaire"); }
    
    /** Calcule les depenses des Etudes. @return Montant en euros. */
    public double calculerDepensesEtudesSurveillees() { return calculerTotalDepenses("Etudes surveillees"); }
    
    /** Calcule les depenses des Ados. @return Montant en euros. */
    public double calculerDepensesEspaceAdos() { return calculerTotalDepenses("Espace Ados"); }
    
    /** Calcule les depenses des Sejours. @return Montant en euros. */
    public double calculerDepensesSejours() { return calculerTotalDepenses("Sejours"); }

    /** Calcule les recettes de la Restauration (base 140 jours). @return Montant en euros. */
    public double calculerRecettesRestauration() { return calculerRecettesAnnuelles("Restauration", 140); }
    
    /** Calcule les recettes des Loisirs (base forfaitaire). @return Montant en euros. */
    public double calculerRecettesAccueilLoisirs() { return calculerRecettesAnnuelles("Accueil de Loisirs", 1); }
    
    /** Calcule les recettes du Periscolaire (base 10 mois). @return Montant en euros. */
    public double calculerRecettesAccueilPeriscolaire() { return calculerRecettesAnnuelles("Accueil periscolaire", 10); }
    
    /** Calcule les recettes des Etudes (base 10 mois). @return Montant en euros. */
    public double calculerRecettesEtudesSurveillees() { return calculerRecettesAnnuelles("Etudes surveillees", 10); }
    
    /** Calcule les recettes des Ados (base forfaitaire). @return Montant en euros. */
    public double calculerRecettesEspaceAdos() { return calculerRecettesAnnuelles("Espace Ados", 1); }
    
    /** Calcule les recettes des Sejours (base forfaitaire). @return Montant en euros. */
    public double calculerRecettesSejours() { return calculerRecettesAnnuelles("Sejours", 1); }

    /**
     * Extrait le texte d'une cellule Excel.
     * 
     * @param c La cellule POI.
     * @return Le contenu texte nettoye, ou une chaine vide si nulle.
     */
    private String getValeurTexte(Cell c) {
        if (c == null) {
            return "";
        }
        return c.toString().trim();
    }

    /**
     * Extrait la valeur numerique d'une cellule, gerant les formules et les formats textes.
     * 
     * @param c La cellule POI.
     * @return La valeur double correspondante, ou 0 si invalide.
     */
    private double getValeurNumerique(Cell c) {
        if (c == null) {
            return 0;
        }
        try {
            if (c.getCellType() == CellType.NUMERIC) {
                return c.getNumericCellValue();
            }
            if (c.getCellType() == CellType.FORMULA) {
                return c.getNumericCellValue();
            }
            String s = c.toString().trim().replace(",", ".");
            if (s.isEmpty()) {
                return 0;
            }
            return Double.parseDouble(s);
        } catch (Exception e) { 
            return 0; 
        }
    }
}
