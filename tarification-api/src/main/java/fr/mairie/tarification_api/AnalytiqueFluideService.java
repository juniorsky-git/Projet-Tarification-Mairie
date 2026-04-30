package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;

/**
 * Service d'Analyse et d'Audit des Fluides Municipaux.
 */
@Service
public class AnalytiqueFluideService {

    private static final String FICHIER = new File("Donnees/Autres/CALC DEP(4).xlsx").exists() 
        ? "Donnees/Autres/CALC DEP(4).xlsx" 
        : "../Donnees/Autres/CALC DEP(4).xlsx";

    private static final double PRIX_EAU_M3 = 4.50; 
    private static final double ABO_EAU_SEMESTRE = 10.67;
    private static final double PRIX_GAZ_M3 = 1.21; 
    private static final double PRIX_ELEC_KWH = 0.31; 

    @org.springframework.beans.factory.annotation.Autowired
    private LogService logService;

    /**
     * Point d'entrée principal (compatibilité dashboard).
     */
    public List<AnalytiqueFluide> analyserTout() {
        return analyserTout(FICHIER, true); 
    }

    /**
     * Analyse filtrée par pôle (utilisée par le Dashboard).
     */
    public List<AnalytiqueFluide> analyserParPole(String pole) {
        // Pour le Dashboard, on ne sauvegarde PAS les logs sur le disque à chaque clic
        List<AnalytiqueFluide> tous = analyserTout(FICHIER, false);
        if (pole == null || pole.isEmpty()) return tous;

        return tous.stream().filter(f -> {
            String site = f.site().toUpperCase();
            switch (pole) {
                case "Restauration":
                    return site.contains("RESTAURATION") || site.contains("GROUPE SCOLAIRE") || site.contains("CUISINE");
                case "Accueil de Loisirs":
                    return site.contains("CENTRE DE LOISIRS") || site.contains("ALSH") || site.contains("POULE");
                case "Accueil periscolaire":
                    return site.contains("MATERNELLE") || site.contains("ELEMENTAIRE") || site.contains("ECOLE");
                case "Etudes surveillees":
                    return site.contains("GROUPE SCOLAIRE") || site.contains("ELEMENTAIRE");
                case "Espace Ados":
                    return site.contains("ADJUST") || site.contains("ADOS") || site.contains("JEUNESSE");
                default:
                    return true;
            }
        }).toList();
    }

    /**
     * Moteur principal d'analyse.
     */
    public List<AnalytiqueFluide> analyserTout(String cheminExcel, boolean doitSauvegarderLogs) {
        List<AnalytiqueFluide> resultats = new ArrayList<>();
        logService.reinitialiser();

        try (FileInputStream fis = new FileInputStream(new File(cheminExcel));
             Workbook wb = WorkbookFactory.create(fis)) {
            
            analyserGaz(wb, resultats);
            analyserElec(wb, resultats);
            analyserEau(wb, resultats);

            resultats.sort((a, b) -> Double.compare(b.montantReel(), a.montantReel()));

            if (doitSauvegarderLogs) {
                logService.sauvegarderFichiers();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultats;
    }

    private void analyserEau(Workbook wb, List<AnalytiqueFluide> list) {
        Sheet s = wb.getSheet("Conso eau");
        if (s == null) return;

        for (int i = 5; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;
            String site = getStr(r, 1) + " " + getStr(r, 2) + " " + getStr(r, 3);
            if (site.trim().isEmpty()) continue;
            double conso = getVal(r, 7) + getVal(r, 17);
            double reel  = getVal(r, 8) + getVal(r, 18);
            if (conso > 0 || reel > 0) {
                list.add(calculer(site, "Eau", conso, reel, "m3", PRIX_EAU_M3, ABO_EAU_SEMESTRE * 2, "Année 2025"));
            }
        }
    }

    public List<RapportSemestrielFluide> analyserBiSemestriel() {
        return analyserBiSemestriel(FICHIER);
    }

    public List<RapportSemestrielFluide> analyserBiSemestriel(String cheminExcel) {
        List<RapportSemestrielFluide> resultats = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(cheminExcel));
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
            String site = (getStr(r, 1) + " " + getStr(r, 2) + " " + getStr(r, 3)).trim();
            if (site.isEmpty()) continue;
            resultats.add(creerRapport(site, "Eau", "m3", getVal(r, 7), getVal(r, 17), getVal(r, 8), getVal(r, 18)));
        }
    }

    private void analyserBiSemestrielGaz(Workbook wb, List<RapportSemestrielFluide> resultats) {
        Sheet s = wb.getSheet("CONSO GAZ");
        if (s == null) return;
        agregerParSemestre(s, 2).forEach((site, acc) -> {
            resultats.add(creerRapport(site, "Gaz", "m3", acc.consoS1, acc.consoS2, acc.reelS1, acc.reelS2));
        });
    }

    private void analyserBiSemestrielElec(Workbook wb, List<RapportSemestrielFluide> resultats) {
        Sheet s = wb.getSheet("CONSO ELEC");
        if (s == null) return;
        agregerParSemestre(s, 5).forEach((site, acc) -> {
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
                        int mois = Integer.parseInt(valRaw.split("/")[1].replaceAll("[^0-9]", ""));
                        if (mois <= 6) { acc.consoS1 += c; acc.reelS1 += m; }
                        else { acc.consoS2 += c; acc.reelS2 += m; }
                        acc.periodes.add(cle);
                    }
                    col += 3;
                }
            }
        }
        return stats;
    }

    private RapportSemestrielFluide creerRapport(String site, String fluide, String unite, double s1_vol, double s2_vol, double s1_eur, double s2_eur) {
        double total_vol = s1_vol + s2_vol;
        double total_eur = s1_eur + s2_eur;
        double delta = s1_vol > 0 ? ((s2_vol - s1_vol) / s1_vol) * 100 : 0;
        return new RapportSemestrielFluide(site, fluide, s1_vol, s2_vol, total_vol, s1_eur, s2_eur, total_eur, delta, Math.abs(delta) > 20, "", unite);
    }

    private void analyserGaz(Workbook wb, List<AnalytiqueFluide> list) {
        Sheet s = wb.getSheet("CONSO GAZ");
        if (s == null) return;
        Map<String, Accumulateur> stats = new LinkedHashMap<>();
        String siteCourant = "";
        for (int i = 9; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;
            String siteNom = getStr(r, 2).trim();
            if (!siteNom.isEmpty()) siteCourant = siteNom;
            if (siteCourant.isEmpty()) continue;
            Accumulateur acc = stats.computeIfAbsent(siteCourant, k -> new Accumulateur());
            for (int col = 2; col < Math.min(r.getLastCellNum(), 200); col++) {
                String valRaw = getStrRaw(r, col).toLowerCase();
                if (valRaw.contains("au") && valRaw.contains("/") && (valRaw.contains("25") || valRaw.contains("2025")) && !valRaw.contains("total")) {
                    double m = getVal(r, col + 3);
                    String cle = valRaw + "_" + m;
                    if (acc.periodes.contains(cle)) { col += 3; continue; }
                    acc.periodes.add(cle);
                    double c = getVal(r, col + 2);
                    if (m < 100000 && m > 0) {
                        acc.totalConso += c; acc.totalReel += m;
                        logService.ajouterLogGaz(siteCourant, col + 3, m, valRaw);
                    }
                    col += 3;
                }
            }
        }
        stats.forEach((site, acc) -> {
            if (acc.totalConso > 0 || acc.totalReel > 0) list.add(calculer(site, "Gaz", acc.totalConso, acc.totalReel, "m3", PRIX_GAZ_M3, 0, "Année 2025"));
        });
    }

    private void analyserElec(Workbook wb, List<AnalytiqueFluide> list) {
        Sheet s = wb.getSheet("CONSO ELEC");
        if (s == null) return;
        Map<String, Accumulateur> stats = new LinkedHashMap<>();
        String siteCourant = "";
        for (int i = 9; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;
            String siteNom = getStr(r, 5).trim();
            if (!siteNom.isEmpty()) siteCourant = siteNom;
            if (siteCourant.isEmpty()) continue;
            Accumulateur acc = stats.computeIfAbsent(siteCourant, k -> new Accumulateur());
            for (int col = 6; col < Math.min(r.getLastCellNum(), 200); col++) {
                String valRaw = getStrRaw(r, col).toLowerCase();
                if (valRaw.contains("/") && valRaw.contains("au") && (valRaw.contains("25") || valRaw.contains("2025")) && !valRaw.contains("total")) {
                    double m = getVal(r, col + 3);
                    String cle = valRaw + "_" + m;
                    if (acc.periodes.contains(cle)) { col += 3; continue; }
                    acc.periodes.add(cle);
                    double c = getVal(r, col + 2);
                    if (m < 100000 && m != 0) {
                        acc.totalConso += c; acc.totalReel += m;
                        logService.ajouterLogElec(siteCourant, col + 3, m, valRaw);
                    }
                    col += 3;
                }
            }
        }
        stats.forEach((site, acc) -> {
            if (acc.totalConso > 0 || acc.totalReel > 0) list.add(calculer(site, "Electricité", acc.totalConso, acc.totalReel, "kWh", PRIX_ELEC_KWH, 0, "Année 2025"));
        });
    }

    private AnalytiqueFluide calculer(String site, String fluide, double conso, double reel, String unite, double prixUnit, double fixe, String periode) {
        double theorique = (conso * prixUnit) + fixe;
        double delta = reel - theorique;
        double pourcentage = theorique > 0 ? (delta / theorique) * 100 : 0;
        return new AnalytiqueFluide(site, fluide, conso, unite, reel, theorique, delta, pourcentage, Math.abs(pourcentage) > 20, periode);
    }

    private String getStr(Row r, int col) {
        String val = getStrRaw(r, col);
        String upper = val.toUpperCase();
        if (upper.contains("TOTAL") || upper.contains("FACTURATION") || val.matches("^[0-9\\.\\s,]+$")) return "";
        return val;
    }

    private String getStrRaw(Row r, int col) {
        Cell c = r.getCell(col);
        return (c == null) ? "" : c.toString().trim();
    }

    private double getVal(Row r, int col) {
        Cell c = r.getCell(col);
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            String s = c.toString().replace(",", ".").replaceAll("[^0-9.]", "");
            return Double.parseDouble(s);
        } catch (Exception e) { return 0; }
    }

    private static class Accumulateur {
        double totalConso = 0; double totalReel = 0;
        double consoS1 = 0; double reelS1 = 0;
        double consoS2 = 0; double reelS2 = 0;
        Set<String> periodes = new HashSet<>();
    }
}
