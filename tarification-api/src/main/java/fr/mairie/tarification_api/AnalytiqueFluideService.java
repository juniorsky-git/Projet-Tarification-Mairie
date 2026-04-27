package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;

/**
 * Service d'Analyse et d'Audit des Fluides Municipaux.
 * 
 * Ce service est le moteur principal du diagnostic budgétaire. Il est capable de :
 * - Parcourir des fichiers Excel complexes (multi-lignes, multi-onglets).
 * - Identifier les consommations réelles (m3, kWh) et les montants facturés.
 * - Calculer les écarts par rapport à un coût théorique basé sur les tarifs 2025.
 * - Générer des logs détaillés pour la traçabilité des calculs.
 * 
 * @author Stagiaire DG 2
 * @version 2.0 (Audit Fiabilisé)
 */
@Service
public class AnalytiqueFluideService {

    /** Chemin vers le fichier source Excel principal */
    private static final String FICHIER = new File("Donnees/Autres/CALC DEP(4).xlsx").exists() 
        ? "Donnees/Autres/CALC DEP(4).xlsx" 
        : "../Donnees/Autres/CALC DEP(4).xlsx";

    /** Tarifs de référence 2025 pour l'Eau (incluant taxes et assainissement) */
    private static final double PRIX_EAU_M3 = 4.50; 
    private static final double ABO_EAU_SEMESTRE = 10.67;

    /** Tarifs de référence 2025 pour le Gaz (calculé sur le PCI/PCS moyen) */
    private static final double PRIX_GAZ_M3 = 1.21; 

    /** Tarifs de référence 2025 pour l'Électricité (tarif TRV Mairie) */
    private static final double PRIX_ELEC_KWH = 0.31; 

    @org.springframework.beans.factory.annotation.Autowired
    private LogService logService;

    /**
     * Point d'entrée principal de l'analyse utilisant le fichier par défaut.
     * @return Liste d'objets AnalytiqueFluide consolidés pour le Dashboard.
     */
    public List<AnalytiqueFluide> analyserTout() {
        return analyserTout(FICHIER);
    }

    /**
     * Effectue l'analyse complète de tous les fluides à partir d'un fichier Excel.
     * @param cheminExcel Le chemin absolu ou relatif vers le fichier .xlsx.
     * @return Une liste triée par montant réel décroissant.
     */
    public List<AnalytiqueFluide> analyserTout(String cheminExcel) {
        List<AnalytiqueFluide> resultats = new ArrayList<>();
        logService.reinitialiser();

        try (FileInputStream fis = new FileInputStream(new File(cheminExcel))) {
            Workbook wb = WorkbookFactory.create(fis);
            
            // Lancement séquentiel des analyses par type de fluide
            analyserGaz(wb, resultats);
            analyserElec(wb, resultats);
            analyserEau(wb, resultats);

            // Tri pour mettre les bâtiments les plus "coûteux" en haut de liste
            resultats.sort((a, b) -> {
                return Double.compare(b.montantReel(), a.montantReel());
            });

            // Finalisation de l'audit
            logService.sauvegarderFichiers();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultats;
    }

    /**
     * Analyse l'onglet Eau. Structure relativement stable mais nécessite un agrégat S1+S2.
     * @param wb Workbook Excel
     * @param list Liste à enrichir
     */
    private void analyserEau(Workbook wb, List<AnalytiqueFluide> list) {
        Sheet s = wb.getSheet("Conso eau");
        if (s == null) {
            return;
        }

        for (int i = 5; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) {
                continue;
            }

            // Identification du site par concaténation des colonnes de localisation
            String site = getStr(r, 1) + " " + getStr(r, 2) + " " + getStr(r, 3);
            if (site.trim().isEmpty()) {
                continue;
            }

            // Addition des deux semestres d'eau
            double conso = getVal(r, 7) + getVal(r, 17);
            double reel  = getVal(r, 8) + getVal(r, 18);

            if (conso > 0 || reel > 0) {
                list.add(calculer(site, "Eau", conso, reel, "m3", PRIX_EAU_M3, ABO_EAU_SEMESTRE * 2, "Année 2025"));
            }
        }
    }

    /**
     * Analyse comparative bi-semestrielle pour l'Issue #24.
     * Restreint à l'Eau pour le moment.
     */
    public List<RapportSemestrielFluide> analyserBiSemestriel() {
        return analyserBiSemestriel(FICHIER);
    }

    public List<RapportSemestrielFluide> analyserBiSemestriel(String cheminExcel) {
        List<RapportSemestrielFluide> resultats = new ArrayList<>();
        File fichier = new File(cheminExcel);
        
        if (!fichier.exists()) {
            System.err.println("ERREUR : Fichier Excel introuvable à : " + cheminExcel);
            return resultats;
        }

        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook wb = WorkbookFactory.create(fis)) {
            
            analyserBiSemestrielEau(wb, resultats);
            analyserBiSemestrielGaz(wb, resultats);
            analyserBiSemestrielElec(wb, resultats);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultats;
    }

    private void analyserBiSemestrielEau(Workbook wb, List<RapportSemestrielFluide> resultats) {
        Sheet s = wb.getSheet("Conso eau");
        if (s == null) return;

        for (int i = 5; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;

            String site = getStr(r, 1) + " " + getStr(r, 2) + " " + getStr(r, 3);
            site = site.trim();
            if (site.isEmpty()) continue;

            double m3_S1 = getVal(r, 7);
            double reel_S1 = getVal(r, 8);
            double m3_S2 = getVal(r, 17);
            double reel_S2 = getVal(r, 18);

            resultats.add(creerRapport(site, "Eau", "m3", m3_S1, m3_S2, reel_S1, reel_S2));
        }
    }

    private void analyserBiSemestrielGaz(Workbook wb, List<RapportSemestrielFluide> resultats) {
        Sheet s = wb.getSheet("CONSO GAZ");
        if (s == null) return;

        Map<String, Accumulateur> stats = agregerParSemestre(s, 2); // Col 2 pour le nom du site
        stats.forEach((site, acc) -> {
            resultats.add(creerRapport(site, "Gaz", "m3", acc.consoS1, acc.consoS2, acc.reelS1, acc.reelS2));
        });
    }

    private void analyserBiSemestrielElec(Workbook wb, List<RapportSemestrielFluide> resultats) {
        Sheet s = wb.getSheet("CONSO ELEC");
        if (s == null) return;

        Map<String, Accumulateur> stats = agregerParSemestre(s, 5); // Col 5 pour l'élec
        stats.forEach((site, acc) -> {
            resultats.add(creerRapport(site, "Electricité", "kWh", acc.consoS1, acc.consoS2, acc.reelS1, acc.reelS2));
        });
    }

    private Map<String, Accumulateur> agregerParSemestre(Sheet s, int colSite) {
        Map<String, Accumulateur> stats = new LinkedHashMap<>();
        String siteCourant = "";

        for (int i = 9; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;

            String siteNom = getStr(r, colSite).trim();
            if (!siteNom.isEmpty()) siteCourant = siteNom;
            if (siteCourant.isEmpty()) continue;

            Accumulateur acc = stats.computeIfAbsent(siteCourant, k -> new Accumulateur());

            for (int col = 2; col < Math.min(r.getLastCellNum(), 200); col++) {
                String valRaw = getStrRaw(r, col).toLowerCase();
                if (valRaw.contains("au") && valRaw.contains("/") && (valRaw.contains("25") || valRaw.contains("2025"))) {
                    double m = getVal(r, col + 3);
                    String cle = valRaw + "_" + m;
                    if (acc.periodes.contains(cle)) { col += 3; continue; }
                    
                    double c = getVal(r, col + 2);
                    if (m < 100000 && m > 0) {
                        int semestre = determinerSemestre(valRaw);
                        if (semestre == 1) { acc.consoS1 += c; acc.reelS1 += m; }
                        else { acc.consoS2 += c; acc.reelS2 += m; }
                        acc.periodes.add(cle);
                    }
                    col += 3;
                }
            }
        }
        return stats;
    }

    private int determinerSemestre(String dateStr) {
        try {
            // Format attendu "au 15/04/25" -> on cherche le 04
            String[] parts = dateStr.split("/");
            if (parts.length >= 2) {
                String moisStr = parts[1].replaceAll("[^0-9]", "");
                int mois = Integer.parseInt(moisStr);
                return (mois <= 6) ? 1 : 2;
            }
        } catch (Exception e) {}
        return 1;
    }

    private RapportSemestrielFluide creerRapport(String site, String fluide, String unite, double s1_vol, double s2_vol, double s1_eur, double s2_eur) {
        double total_vol = s1_vol + s2_vol;
        double total_eur = s1_eur + s2_eur;
        double delta = s1_vol > 0 ? ((s2_vol - s1_vol) / s1_vol) * 100 : 0;
        boolean alerte = Math.abs(delta) > 20;

        String remarque = "";
        if (s1_vol == 0 && s1_eur > 0) remarque = "S1 : Abonnement uniquement (0 " + unite + ")";
        else if (total_vol == 0 && total_eur > 0) remarque = "Abonnements uniquement";

        return new RapportSemestrielFluide(site, fluide, s1_vol, s2_vol, total_vol, s1_eur, s2_eur, total_eur, delta, alerte, remarque, unite);
    }

    /**
     * Analyse l'onglet Gaz. Gestion complexe des sites multi-lignes et multi-compteurs.
     * @param wb Workbook Excel
     * @param list Liste à enrichir
     */
    private void analyserGaz(Workbook wb, List<AnalytiqueFluide> list) {
        Sheet s = wb.getSheet("CONSO GAZ");
        if (s == null) {
            return;
        }

        Map<String, Accumulateur> stats = new LinkedHashMap<>();
        String siteCourant = "";

        for (int i = 9; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) {
                continue;
            }

            // Gestion du site sur plusieurs lignes : on garde le dernier nom rencontré
            String siteNom = getStr(r, 2).trim();
            if (!siteNom.isEmpty()) {
                siteCourant = siteNom;
            }
            if (siteCourant.isEmpty()) {
                continue;
            }

            Accumulateur acc = stats.computeIfAbsent(siteCourant, k -> {
                return new Accumulateur();
            });

            // Scanner horizontal de factures (fenêtre glissante par pas de 4)
            for (int col = 2; col < Math.min(r.getLastCellNum(), 200); col++) {
                String valRaw = getStrRaw(r, col).toLowerCase();
                
                // Critères de détection d'un bloc de facture valide (2025 uniquement)
                if (valRaw.contains("au") && valRaw.contains("/") && (valRaw.contains("25") || valRaw.contains("2025"))
                    && !valRaw.contains("total") && !valRaw.contains("cumul") && !valRaw.contains("annuel")) {
                    
                    double m = getVal(r, col + 3);
                    // Dédoublonnage via Date+Montant pour capturer les multi-compteurs d'un même mois
                    String cleUnique = valRaw + "_" + m;

                    if (acc.periodes.contains(cleUnique)) {
                        col += 3;
                        continue;
                    }
                    acc.periodes.add(cleUnique);

                    double c = getVal(r, col + 2);
                    
                    // Filtrage des anomalies techniques (montants aberrants ou vides)
                    if (m < 100000 && m > 0) {
                        acc.totalConso += c;
                        acc.totalReel += m;
                        logService.ajouterLogGaz(siteCourant, col + 3, m, valRaw);
                        if (acc.dateFin == null) {
                            acc.dateFin = valRaw;
                        }
                        acc.dateDebut = valRaw;
                    }
                    col += 3;
                }
            }
        }

        // Transformation des données accumulées en objets de résultat final
        stats.forEach((site, acc) -> {
            if (acc.totalConso > 0 || acc.totalReel > 0) {
                list.add(calculer(site, "Gaz", acc.totalConso, acc.totalReel, "m3", PRIX_GAZ_M3, 0, "Cumul Annuel 2025"));
            }
        });
    }

    /**
     * Analyse l'onglet Électricité. 
     * Utilise le même scanner intelligent que le Gaz pour plus de fiabilité.
     * @param wb Workbook Excel
     * @param list Liste à enrichir
     */
    private void analyserElec(Workbook wb, List<AnalytiqueFluide> list) {
        Sheet s = wb.getSheet("CONSO ELEC");
        if (s == null) {
            return;
        }

        Map<String, Accumulateur> stats = new LinkedHashMap<>();
        String siteCourant = "";

        for (int i = 9; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) {
                continue;
            }

            String siteNom = getStr(r, 5).trim();
            if (!siteNom.isEmpty()) {
                siteCourant = siteNom;
            }
            if (siteCourant.isEmpty()) {
                continue;
            }

            Accumulateur acc = stats.computeIfAbsent(siteCourant, k -> {
                return new Accumulateur();
            });

            for (int col = 6; col < Math.min(r.getLastCellNum(), 200); col++) {
                String valRaw = getStrRaw(r, col).toLowerCase();
                
                // Détecteur de facture Électricité 2025
                if (valRaw.contains("/") && valRaw.contains("au")
                    && !valRaw.contains("total") && !valRaw.contains("cumul") && !valRaw.contains("annuel")
                    && (valRaw.contains("25") || valRaw.contains("2025"))) {
                    
                    double m = getVal(r, col + 3);
                    String cleUnique = valRaw + "_" + m;

                    if (acc.periodes.contains(cleUnique)) {
                        col += 3;
                        continue;
                    }
                    acc.periodes.add(cleUnique);

                    double c = getVal(r, col + 2);
                    
                    if (m < 100000 && m != 0) {
                        acc.totalConso += c;
                        acc.totalReel += m;
                        logService.ajouterLogElec(siteCourant, col + 3, m, valRaw);
                        if (acc.dateFin == null) {
                            acc.dateFin = valRaw;
                        }
                        acc.dateDebut = valRaw;
                    }
                    col += 3;
                }
            }
        }

        stats.forEach((site, acc) -> {
            String periode = (acc.dateDebut != null && acc.dateFin != null) ? "du " + acc.dateDebut + " au " + acc.dateFin : "Année 2025";
            if (acc.totalConso > 0 || acc.totalReel > 0) {
                list.add(calculer(site, "Electricité", acc.totalConso, acc.totalReel, "kWh", PRIX_ELEC_KWH, 0, periode));
            }
        });
    }

    /**
     * Exécute les calculs financiers (théorique, delta, pourcentage, alerte).
     */
    private AnalytiqueFluide calculer(String site, String fluide, double conso, double reel, String unite, double prixUnit, double fixe, String periode) {
        double theorique = (conso * prixUnit) + fixe;
        double delta = reel - theorique;
        double pourcentage = theorique > 0 ? (delta / theorique) * 100 : 0;
        boolean alerte = Math.abs(pourcentage) > 20;

        return new AnalytiqueFluide(site, fluide, conso, unite, reel, theorique, delta, pourcentage, alerte, periode);
    }

    /**
     * Récupère une chaîne nettoyée et filtre les noms de colonnes techniques.
     */
    private String getStr(Row r, int col) {
        String val = getStrRaw(r, col);
        String upper = val.toUpperCase();
        if (upper.contains("TOTAL") || upper.contains("FACTURATION") || upper.contains("BILAN") || upper.startsWith("SUM") || upper.startsWith("=")) {
            return ""; 
        }
        // Si c'est juste un nombre, c'est probablement une donnée technique et pas un nom de site
        if (val.matches("^[0-9\\.\\s,]+$")) {
            return "";
        }
        return val;
    }

    /**
     * Lecture brute d'une cellule Excel.
     */
    private String getStrRaw(Row r, int col) {
        Cell c = r.getCell(col);
        return (c == null) ? "" : c.toString().trim();
    }

    /**
     * Lecture numérique avec conversion forcée si nécessaire.
     */
    private double getVal(Row r, int col) {
        Cell c = r.getCell(col);
        if (c == null) {
            return 0;
        }
        try {
            if (c.getCellType() == CellType.NUMERIC) {
                return c.getNumericCellValue();
            }
            // Gestion des virgules et des caractères parasites (ex: "45,50 €")
            String s = c.toString().replace(",", ".").replaceAll("[^0-9.]", "");
            return Double.parseDouble(s);
        } catch (Exception e) { 
            return 0; 
        }
    }

    /**
     * Structure interne temporaire de stockage pour l'agrégation des données.
     */
    private static class Accumulateur {
        double totalConso = 0;
        double totalReel = 0;
        
        double consoS1 = 0;
        double reelS1 = 0;
        double consoS2 = 0;
        double reelS2 = 0;

        String dateDebut = null;
        String dateFin = null;
        Set<String> periodes = new HashSet<>();
    }
}
