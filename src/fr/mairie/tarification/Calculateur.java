package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Moteur de calcul financier.
 *
 * Source principale des depenses :
 * Fichier CALC DEP (3).csv (synthetise).
 * Source secondaire pour details :
 * Onglet "Simulation" du fichier CALC DEP (1).xlsx (index 8).
 * Ce fichier contient par tranche (A a G + EXT) :
 * - Colonne B : Code de la tranche.
 * - Colonne C : Prix reel facture par repas pour cette tranche.
 * - Colonne D : Nombre d enfants inscrits dans cette tranche.
 * - Colonne E : Cout moyen de reference de la mairie (4.42 euros, fixe).
 *
 * Le fichier Feuille_dataviz.xlsx n est plus utilise pour les effectifs.
 */
public class Calculateur {

    private static final String FICHIER_DEPENSES_CSV = "Donnees/Autres/CALC DEP (3).csv";
    private static final String FICHIER_DEPENSES = "Donnees/Autres/CALC DEP (1).xlsx";

    // Index de l onglet Simulation dans CALC DEP.xlsx
    private static final int ONGLET_SIMULATION = 8;

    // Colonnes dans l onglet Simulation (index 0-based)
    private static final int COL_SIMU_CODE_TRANCHE = 1; // Colonne B
    private static final int COL_SIMU_PRIX_REEL = 2; // Colonne C : Prix reel facture
    private static final int COL_SIMU_NB_ENFANTS = 3; // Colonne D : Nombre d enfants
    private static final int COL_SIMU_COUT_REF = 4; // Colonne E : Cout de reference (4.42)
    private static final int COL_SIMU_DEPENSES_REELLES = 14; // Colonne O : depenses reelles accueil loisirs

    // Colonnes dans l onglet Depenses (index 0-based)
    private static final int COL_DEP_MONTANT_TTC = 7; // Colonne H
    private static final int COL_DEP_SERVICE = 18; // Colonne S
    private static final int COL_DEP_ANTENNE = 19; // Colonne T
    private static final int COL_DEP_LIBELLE = 3; // Colonne D

    /**
     * Structure de donnees regroupant les resultats de l onglet Simulation.
     */
    public static class SimulationData {
        private final Map<String, Double> effectifs = new HashMap<>();
        private double recettesTheoriques = 0;
        private double coutMoyenReference = 4.42;

        public Map<String, Double> getEffectifs() {
            return effectifs;
        }

        public double getRecettesTheoriques() {
            return recettesTheoriques;
        }

        public double getCoutMoyenReference() {
            return coutMoyenReference;
        }
    }

    /**
     * Lit les totaux des depenses depuis le fichier CSV synthetise.
     * Retourne une Map avec les services comme cles et les montants comme valeurs.
     */
    public Map<String, Double> lireTotauxDepensesDepuisCSV() {
        Map<String, Double> totaux = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(FICHIER_DEPENSES_CSV), 
                java.nio.charset.Charset.forName("ISO-8859-1"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Total;;;")) {
                    // Ligne des totaux : Total;;;montant1;montant2;...;total
                    String[] parts = line.split(";");
                    if (parts.length >= 10) {
                        // Indices : 3=Restauration, 4=Accueil Loisirs, 5=Accueil periscolaire, 6=Etudes surveillees, 7=Espace Ados, 8=Sejours, 9=Total general
                        try {
                            totaux.put("Restauration", parseMontant(parts[3]));
                            totaux.put("Accueil de Loisirs", parseMontant(parts[4]));
                            totaux.put("Accueil periscolaire", parseMontant(parts[5]));
                            totaux.put("Etudes surveillees", parseMontant(parts[6]));
                            totaux.put("Espace Ados", parseMontant(parts[7]));
                            totaux.put("Sejours", parseMontant(parts[8]));
                            totaux.put("Total", parseMontant(parts[9]));
                        } catch (Exception e) {
                            LogService.error("Erreur parsing totaux CSV", e);
                        }
                    }
                    break; // On a trouve la ligne Total, pas besoin de continuer
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur lecture CSV depenses", e);
        }
        return totaux;
    }

    /**
     * Parse un montant string en double, gerant tous les formats (1 234,56 ou 1.234,56 etc).
     * Robuste aux caracteres speciaux comme €.
     */
    private double parseMontant(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        
        // Enlever tous les caracteres non-numeriques sauf , et .
        String clean = s.replaceAll("[^\\d.,]", "").trim();
        
        if (clean.isEmpty()) return 0.0;
        
        // Detecter le separateur decimal et gerer les separateurs de milliers
        if (clean.contains(",") && clean.contains(".")) {
            // Determiner lequel est le separateur decimal (le dernier)
            int lastComma = clean.lastIndexOf(',');
            int lastDot = clean.lastIndexOf('.');
            
            if (lastComma > lastDot) {
                // Format: 1.234,56 -> enlever les points, remplacer , par .
                clean = clean.replace(".", "").replace(",", ".");
            } else {
                // Format: 1,234.56 -> enlever les virgules
                clean = clean.replace(",", "");
            }
        } else if (clean.contains(",")) {
            // Seule la virgule -> c'est le separateur decimal
            clean = clean.replace(",", ".");
        }
        // Sinon juste des points ou rien de special, garder comme est
        
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            LogService.error("Erreur parsing montant: " + s, e);
            return 0.0;
        }
    }

    /**
     * Lit l'onglet Simulation une seule fois pour extraire toutes les donnees.
     */
    public SimulationData chargerDonneesSimulation() {
        SimulationData data = new SimulationData();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                LogService.log("Onglet 'Simulation' introuvable dans " + FICHIER_DEPENSES);
                return data;
            }

            // 1. Cout de reference (fixe ligne 7, col E)
            Row refRow = s.getRow(7);
            if (refRow != null) {
                data.coutMoyenReference = getValeurNumerique(refRow.getCell(COL_SIMU_COUT_REF));
            }

            // 2. Parours des tranches
            for (int i = 6; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) {
                    continue;
                }

                String col0 = getValeurTexte(row.getCell(0));
                String col1 = getValeurTexte(row.getCell(COL_SIMU_CODE_TRANCHE));

                if (col0.equalsIgnoreCase("Total")) {
                    break;
                }

                String codeTranche = "";
                if (col0.equalsIgnoreCase("EXT")) {
                    codeTranche = "EXT";
                } else if (!col1.isEmpty()) {
                    codeTranche = col1;
                }

                if (codeTranche.isEmpty()) {
                    continue;
                }

                double prixReel = getValeurNumerique(row.getCell(COL_SIMU_PRIX_REEL));
                double nbEnfants = getValeurNumerique(row.getCell(COL_SIMU_NB_ENFANTS));

                if (nbEnfants > 0) {
                    data.effectifs.put(codeTranche, nbEnfants);
                    data.recettesTheoriques += prixReel * nbEnfants * 140;
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur critique lors du chargement de la Simulation", e);
        }
        return data;
    }

    /**
     * Calcule le total des depenses reelles accueil loisirs (somme de tous les
     * segments).
     */
    public double calculerTotalDepensesLoisirs() {
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();
        return totaux.getOrDefault("Accueil de Loisirs", 0.0);
    }

    public Map<String, Double> calculerDepensesReellesAccueilLoisirsParSegment() {
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();
        double totalLoisirs = totaux.getOrDefault("Accueil de Loisirs", 0.0);
        Map<String, Double> result = new HashMap<>();
        result.put("Total", totalLoisirs);
        return result;
    }

    /**
     * Retourne le detail des depenses d Accueil de Loisirs par categorie.
     * Source : Tableau des depenses du CSV CALC DEP (3).csv
     * @return Map ordonnee (Categorie -> Montant)
     */
    public Map<String, Double> getDepensesAccueilLoisirsDetaillees() {
        Map<String, Double> details = new LinkedHashMap<>();
        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(FICHIER_DEPENSES_CSV), 
                java.nio.charset.Charset.forName("ISO-8859-1"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Arreter a la ligne Total
                if (line.startsWith("Total;;;")) {
                    break;
                }
                
                String[] parts = line.split(";", -1);
                
                // Colonnes du CSV : A=Categorie, B=Detail, C=Code, D onwards=Services
                // Accueil de Loisirs est la colonne E (index 4)
                if (parts.length > 4) {
                    String categorie = parts[0].trim();
                    String detail = parts[1].trim();
                    String montantStr = parts[4].trim();
                    
                    // Ignorer les lignes vides ou d'en-tete
                    if (categorie.isEmpty() || montantStr.isEmpty()) {
                        continue;
                    }
                    
                    double montant = parseMontant(montantStr);
                    if (montant > 0) {
                        // Creer un libelle en combinant categorie et detail
                        String label = categorie;
                        if (!detail.isEmpty() && !detail.equals(categorie)) {
                            label = categorie + " - " + detail;
                        }
                        details.put(label, montant);
                    }
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur lecture details depenses Accueil Loisirs", e);
        }
        return details;
    }

    private String determinerSegmentAccueilLoisirs(String ligne) {
        if (ligne.contains("MDJ")) {
            return "MDJ";
        }
        if (ligne.contains("CLGAV")) {
            return "CLGAV";
        }
        if (ligne.contains("CLJP1")) {
            return "CLJP1";
        }
        if (ligne.contains("CLLMICH")) {
            return "CLLMICH";
        }
        if (ligne.contains("P'TIT") || ligne.contains("PTIT") || ligne.contains("PRINCE")) {
            return "P'TIT PRINCE";
        }
        return null;
    }

    /**
     * Retourne le texte complet d une ligne pour recherche de mots-cles.
     */
    private String getRowTexte(Row row) {
        StringBuilder sb = new StringBuilder();
        for (Cell c : row) {
            if (c != null) {
                sb.append(c.toString()).append(' ');
            }
        }
        return sb.toString().trim();
    }

    /**
     * Calcule les depenses reelles pour un pole donne de maniere universelle.
     *
     * @param antenne   Code de l antenne (ex : RESTMICH) ou null.
     * @param service   Code du service (ex : 2-RE) ou null.
     * @param inclusion Mot-cle obligatoire dans le libelle ou null.
     * @param exclusions Mots-cles a exclure du libelle ou null.
     * @return Total des depenses TTC.
     */
    public double calculerDepenses(String antenne, String service, String inclusion, String[] exclusions) {
        double total = 0;
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
                Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0); // Onglet principal des depenses
            if (s == null) {
                LogService.log("Onglet 'Depenses restau 2025' introuvable");
                return 0;
            }

            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null)
                    continue;

                String ant = getValeurTexte(row.getCell(COL_DEP_ANTENNE));
                String ser = getValeurTexte(row.getCell(COL_DEP_SERVICE));
                String lib = getValeurTexte(row.getCell(COL_DEP_LIBELLE)).toUpperCase();

                // Filtrage Antenne/Service
                boolean match = false;
                if (antenne != null && ant.equalsIgnoreCase(antenne)) {
                    match = true;
                }
                if (service != null && ser.contains(service)) {
                    match = true;
                }

                if (!match) {
                    continue;
                }

                // Filtrage Inclusion
                if (inclusion != null && !lib.contains(inclusion.toUpperCase())) {
                    continue;
                }

                // Filtrage Exclusions
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
        } catch (Exception e) {
            LogService.error("Erreur calcul depenses generique", e);
        }
        return total;
    }

    /**
     * Retourne le detail des depenses reelles pour le pole Ados.
     * Source : Onglet Simulation, Ligne 74 (Titres) et 75 (Valeurs).
     * @return Map ordonnee (Libelle -> Montant)
     */
    public Map<String, Double> getDepensesAdosDetaillees() {
        Map<String, Double> details = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
                Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                return details;
            }
            Row rowHeaders = s.getRow(73); // Ligne 74
            Row rowValues = s.getRow(74);  // Ligne 75

            if (rowHeaders == null || rowValues == null) {
                return details;
            }

            int nbCols = Math.min(rowHeaders.getLastCellNum(), rowValues.getLastCellNum());
            for (int j = 2; j < nbCols; j++) {
                Cell cHeader = rowHeaders.getCell(j);
                Cell cValue = rowValues.getCell(j);
                
                String label = (cHeader == null) ? "Inconnu" : cHeader.toString().replace("\n", " ").trim();
                double montant = Math.abs(getValeurNumerique(cValue));

                if (!label.isEmpty() && montant > 0 && !label.equalsIgnoreCase("TOTAL")) {
                    details.put(label, montant);
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur lecture detail depenses Ados", e);
        }
        return details;
    }

    /**
     * Retourne le detail des depenses par destination (Sejours).
     * Source : Onglet Simulation, Lignes 92 a 95.
     * @return Map (Nom du sejour -> Total)
     */
    public Map<String, Double> getDepensesParSejour() {
        Map<String, Double> sejours = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
                Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheet("Simulation");
            if (s == null) return sejours;

            // On parcourt les lignes de sejours : 92 a 95 (index 91 a 94)
            for (int i = 91; i <= 94; i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String nom = getValeurTexte(row.getCell(1)); // Colonne B
                if (nom.isEmpty() || nom.equals("0")) continue;

                // On calcule le total de la ligne en additionnant les colonnes C a K (2 a 10)
                // car la colonne L (Total) contient souvent des formules externes non cachees.
                double totalLigne = 0;
                for (int j = 2; j <= 10; j++) {
                    totalLigne += Math.abs(getValeurNumerique(row.getCell(j)));
                }

                if (totalLigne > 0) {
                    sejours.put(nom, totalLigne);
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur lecture detail sejours", e);
        }
        return sejours;
    }
    /**
     * Retourne le detail des depenses pour le pole Etudes Surveillees.
     * Source : Onglet Simulation, Ligne 59 (Personnel).
     * @return Map (Libelle -> Montant)
     */
    public Map<String, Double> getDepensesEtudesDetaillees() {
        Map<String, Double> details = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(FICHIER_DEPENSES);
                Workbook wb = WorkbookFactory.create(fis)) {
            Sheet s = wb.getSheet("Simulation");
            if (s == null) {
                return details;
            }
            
            // Ligne 59 (index 58) : Charges de personnel
            Row rowPerso = s.getRow(58);
            if (rowPerso != null) {
                double montant = Math.abs(getValeurNumerique(rowPerso.getCell(2))); // Colonne C
                if (montant > 0) {
                    details.put("Charges de Personnel", montant);
                }
            }
            
            // On peut aussi chercher d'autres charges (Fournitures L60)
            Row rowFourn = s.getRow(59);
            if (rowFourn != null) {
                double montant = Math.abs(getValeurNumerique(rowFourn.getCell(3))); // Colonne D
                if (montant > 0) {
                    details.put("Fournitures scolaires", montant);
                }
            }

        } catch (Exception e) {
            LogService.error("Erreur lecture detail depenses Etudes", e);
        }
        return details;
    }

    // =====================================================================================
    // CALCULS PAR POLE BASES SUR CALC DEP (3).CSV
    // =====================================================================================

    /**
     * Calcule les depenses totales pour la Restauration scolaire.
     * @return Montant total des depenses
     */
    public double calculerDepensesRestauration() {
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();
        return totaux.getOrDefault("Restauration", 0.0);
    }

    /**
     * Calcule les depenses totales pour l'Accueil de Loisirs.
     * @return Montant total des depenses
     */
    public double calculerDepensesAccueilLoisirs() {
        return calculerTotalDepensesLoisirs(); // Utilise la methode existante
    }

    /**
     * Calcule les depenses totales pour l'Accueil Periscolaire.
     * @return Montant total des depenses
     */
    public double calculerDepensesAccueilPeriscolaire() {
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();
        return totaux.getOrDefault("Accueil periscolaire", 0.0);
    }

    /**
     * Calcule les depenses totales pour les Etudes Surveillees.
     * @return Montant total des depenses
     */
    public double calculerDepensesEtudesSurveillees() {
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();
        return totaux.getOrDefault("Etudes surveillees", 0.0);
    }

    /**
     * Calcule les depenses totales pour l'Espace Ados.
     * @return Montant total des depenses
     */
    public double calculerDepensesEspaceAdos() {
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();
        return totaux.getOrDefault("Espace Ados", 0.0);
    }

    /**
     * Calcule les depenses totales pour les Sejours.
     * @return Montant total des depenses
     */
    public double calculerDepensesSejours() {
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();
        return totaux.getOrDefault("Sejours", 0.0);
    }

    /**
     * Calcule le total general de toutes les depenses.
     * @return Montant total de toutes les depenses
     */
    public double calculerTotalDepensesGenerales() {
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();
        return totaux.getOrDefault("Total", 0.0);
    }

    /**
     * Calcule les recettes theoriques pour la Restauration scolaire.
     * Base sur les tarifs et effectifs depuis l'onglet Simulation.
     * @return Montant total des recettes theoriques
     */
    public double calculerRecettesRestauration() {
        SimulationData data = chargerDonneesSimulation();
        return data.getRecettesTheoriques();
    }

    /**
     * Calcule les recettes theoriques pour l'Accueil de Loisirs.
     * Base sur les tarifs moyens et estimation d'effectifs.
     * @return Montant total des recettes theoriques
     */
    public double calculerRecettesAccueilLoisirs() {
        // Estimation basee sur les tarifs moyens du CSV
        // EXT: 14.96€, A: 13.86€, B: 12.26€, etc.
        // Pour une estimation simple, utiliser un tarif moyen pondere
        return 0.0; // A implementer selon les donnees disponibles
    }

    /**
     * Calcule les recettes theoriques pour l'Accueil Periscolaire.
     * Base sur les tarifs du CSV.
     * @return Montant total des recettes theoriques
     */
    public double calculerRecettesAccueilPeriscolaire() {
        // Tarifs depuis le CSV: EXT: 4.48€, A: 4.17€, B: 3.66€, etc.
        return 0.0; // A implementer selon les donnees disponibles
    }

    /**
     * Calcule les recettes theoriques pour les Etudes Surveillees.
     * Base sur les tarifs du CSV.
     * @return Montant total des recettes theoriques
     */
    public double calculerRecettesEtudesSurveillees() {
        // Tarifs depuis le CSV: EXT: 16.12€, A: 14.49€, B: 13.02€, etc.
        return 0.0; // A implementer selon les donnees disponibles
    }

    /**
     * Calcule les recettes theoriques pour l'Espace Ados.
     * Base sur les tarifs du CSV.
     * @return Montant total des recettes theoriques
     */
    public double calculerRecettesEspaceAdos() {
        // Tarifs depuis le CSV: EXT: 8.19€, A: 6.94€, B: 7.01€, etc.
        return 0.0; // A implementer selon les donnees disponibles
    }

    /**
     * Calcule les recettes theoriques pour les Sejours.
     * Base sur les tarifs du CSV.
     * @return Montant total des recettes theoriques
     */
    public double calculerRecettesSejours() {
        // Tarif fixe: 70.00€ pour EXT, 56.00€ pour A, etc.
        return 0.0; // A implementer selon les donnees disponibles
    }

    /**
     * Calcule le resultat financier (Recettes - Depenses) pour chaque pole.
     * @return Map avec les resultats par pole
     */
    public Map<String, Double> calculerResultatsFinanciersParPole() {
        Map<String, Double> resultats = new LinkedHashMap<>();

        // Restauration
        double depRestau = calculerDepensesRestauration();
        double recRestau = calculerRecettesRestauration();
        resultats.put("Restauration", recRestau - depRestau);

        // Accueil de Loisirs
        double depLoisirs = calculerDepensesAccueilLoisirs();
        double recLoisirs = calculerRecettesAccueilLoisirs();
        resultats.put("Accueil de Loisirs", recLoisirs - depLoisirs);

        // Accueil Periscolaire
        double depPeriscolaire = calculerDepensesAccueilPeriscolaire();
        double recPeriscolaire = calculerRecettesAccueilPeriscolaire();
        resultats.put("Accueil Periscolaire", recPeriscolaire - depPeriscolaire);

        // Etudes Surveillees
        double depEtudes = calculerDepensesEtudesSurveillees();
        double recEtudes = calculerRecettesEtudesSurveillees();
        resultats.put("Etudes Surveillees", recEtudes - depEtudes);

        // Espace Ados
        double depAdos = calculerDepensesEspaceAdos();
        double recAdos = calculerRecettesEspaceAdos();
        resultats.put("Espace Ados", recAdos - depAdos);

        // Sejours
        double depSejours = calculerDepensesSejours();
        double recSejours = calculerRecettesSejours();
        resultats.put("Sejours", recSejours - depSejours);

        return resultats;
    }

    /**
     * Calcule les couts moyens par enfant pour chaque pole.
     * Base sur les depenses totales et les nombres d'enfants du CSV.
     * @return Map avec les couts moyens par pole
     */
    public Map<String, Double> calculerCoutsMoyensParPole() {
        Map<String, Double> coutsMoyens = new LinkedHashMap<>();
        Map<String, Double> totaux = lireTotauxDepensesDepuisCSV();

        // Lire les nombres d'enfants depuis le CSV
        Map<String, Integer> effectifs = lireEffectifsDepuisCSV();

        // Calculer les couts moyens
        for (Map.Entry<String, Double> entry : totaux.entrySet()) {
            String pole = entry.getKey();
            if (!pole.equals("Total")) {
                double depenses = entry.getValue();
                int nbEnfants = effectifs.getOrDefault(pole, 0);
                if (nbEnfants > 0) {
                    coutsMoyens.put(pole, depenses / nbEnfants);
                } else {
                    coutsMoyens.put(pole, 0.0);
                }
            }
        }

        return coutsMoyens;
    }

    /**
     * Lit les effectifs (nombres d'enfants) depuis le CSV.
     * @return Map avec les effectifs par pole
     */
    private Map<String, Integer> lireEffectifsDepuisCSV() {
        Map<String, Integer> effectifs = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(FICHIER_DEPENSES_CSV), 
                java.nio.charset.Charset.forName("ISO-8859-1"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Nombre d'enfants;;;")) {
                    // Ligne suivante contient les nombres d'enfants
                    String nextLine = br.readLine();
                    if (nextLine != null) {
                        String[] parts = nextLine.split(";");
                        if (parts.length >= 6) {
                            try {
                                effectifs.put("Restauration", (int) parseMontant(parts[3]));
                                effectifs.put("Accueil de Loisirs", (int) parseMontant(parts[4]));
                                effectifs.put("Accueil periscolaire", (int) parseMontant(parts[5]));
                                effectifs.put("Etudes surveillees", (int) parseMontant(parts[6]));
                                effectifs.put("Espace Ados", (int) parseMontant(parts[7]));
                                effectifs.put("Sejours", (int) parseMontant(parts[8]));
                            } catch (Exception e) {
                                LogService.error("Erreur parsing effectifs CSV", e);
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LogService.error("Erreur lecture effectifs CSV", e);
        }
        return effectifs;
    }

    /**
     * Calcule les ratios depenses/recettes pour chaque pole.
     * @return Map avec les ratios par pole (valeur > 1 = deficit)
     */
    public Map<String, Double> calculerRatiosDepensesRecettes() {
        Map<String, Double> ratios = new LinkedHashMap<>();
        Map<String, Double> resultats = calculerResultatsFinanciersParPole();

        for (Map.Entry<String, Double> entry : resultats.entrySet()) {
            String pole = entry.getKey();
            double resultat = entry.getValue();

            // Calculer les depenses pour ce pole
            double depenses = 0.0;
            switch (pole) {
                case "Restauration":
                    depenses = calculerDepensesRestauration();
                    break;
                case "Accueil de Loisirs":
                    depenses = calculerDepensesAccueilLoisirs();
                    break;
                case "Accueil Periscolaire":
                    depenses = calculerDepensesAccueilPeriscolaire();
                    break;
                case "Etudes Surveillees":
                    depenses = calculerDepensesEtudesSurveillees();
                    break;
                case "Espace Ados":
                    depenses = calculerDepensesEspaceAdos();
                    break;
                case "Sejours":
                    depenses = calculerDepensesSejours();
                    break;
            }

            if (depenses > 0) {
                // Ratio depenses/recettes = depenses / (depenses - resultat)
                double recettes = depenses - resultat;
                ratios.put(pole, recettes > 0 ? depenses / recettes : Double.POSITIVE_INFINITY);
            } else {
                ratios.put(pole, 0.0);
            }
        }

        return ratios;
    }

    /**
     * Retourne un rapport complet de tous les calculs par pole.
     * @return String formate avec tous les indicateurs
     */
    public String genererRapportCompletParPole() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("================================================================================\n");
        rapport.append("RAPPORT COMPLET PAR POLE - CALC DEP (3)\n");
        rapport.append("================================================================================\n\n");

        Map<String, Double> depenses = lireTotauxDepensesDepuisCSV();
        Map<String, Double> resultats = calculerResultatsFinanciersParPole();
        Map<String, Double> coutsMoyens = calculerCoutsMoyensParPole();
        Map<String, Double> ratios = calculerRatiosDepensesRecettes();
        Map<String, Integer> effectifs = lireEffectifsDepuisCSV();

        String[] poles = {"Restauration", "Accueil de Loisirs", "Accueil Periscolaire", "Etudes Surveillees", "Espace Ados", "Sejours"};

        for (String pole : poles) {
            rapport.append(String.format("POLE: %s\n", pole.toUpperCase()));
            rapport.append("-".repeat(50) + "\n");

            double dep = depenses.getOrDefault(pole, 0.0);
            double res = resultats.getOrDefault(pole, 0.0);
            double coutMoyen = coutsMoyens.getOrDefault(pole, 0.0);
            double ratio = ratios.getOrDefault(pole, 0.0);
            int effectif = effectifs.getOrDefault(pole, 0);

            rapport.append(String.format("  Effectif: %d enfants\n", effectif));
            rapport.append(String.format("  Depenses totales: %.2f €\n", dep));
            rapport.append(String.format("  Cout moyen/enfant: %.2f €\n", coutMoyen));
            rapport.append(String.format("  Resultat financier: %.2f € %s\n",
                Math.abs(res), res >= 0 ? "(benefice)" : "(deficit)"));
            rapport.append(String.format("  Ratio depenses/recettes: %.2f\n",
                Double.isInfinite(ratio) ? 0.0 : ratio));

            if (ratio > 1.0 && !Double.isInfinite(ratio)) {
                rapport.append("  Status: DEFICITAIRE\n");
            } else if (ratio == 1.0) {
                rapport.append("  Status: EQUILIBRE\n");
            } else {
                rapport.append("  Status: BENEFICIAIRE\n");
            }

            rapport.append("\n");
        }

        double totalDepenses = depenses.getOrDefault("Total", 0.0);
        rapport.append("SYNTHASE GENERALE\n");
        rapport.append("-".repeat(50) + "\n");
        rapport.append(String.format("Total depenses: %.2f €\n", totalDepenses));

        return rapport.toString();
    }

    /**
     * Retourne la valeur texte d une cellule, ou une chaine vide si la cellule est
     * nulle.
     */
    private String getValeurTexte(Cell c) {
        if (c == null)
            return "";
        return c.toString().trim();
    }

    /**
     * Retourne la valeur numerique d une cellule, ou 0 si la cellule est nulle ou
     * non numerique.
     */
    private double getValeurNumerique(Cell c) {
        if (c == null)
            return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC)
                return c.getNumericCellValue();
            if (c.getCellType() == CellType.FORMULA) {
                try {
                    return c.getNumericCellValue(); // Rcupre le rsultat calcul mis en cache
                } catch (Exception e) {
                    // Si pas numérique, on tombe dans le toString
                }
            }
            String s = c.toString().trim().replace(",", ".");
            if (s.isEmpty())
                return 0;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
