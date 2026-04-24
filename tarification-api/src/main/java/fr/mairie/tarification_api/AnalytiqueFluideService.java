package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Service d'analyse universel des fluides.
 * Lit directement le fichier source CALC DEP(4).xlsx pour un diagnostic exhaustif.
 */
@Service
public class AnalytiqueFluideService {

    private static final String FICHIER = new File("Donnees/Autres/CALC DEP(4).xlsx").exists() 
        ? "Donnees/Autres/CALC DEP(4).xlsx" 
        : "../Donnees/Autres/CALC DEP(4).xlsx";

    // Tarifs de référence 2025
    // 4.50€/m3 : Moyenne incluant la redevance pollution et le traitement des eaux usées
    private static final double PRIX_EAU_M3 = 4.50; 
    private static final double ABO_EAU_SEMESTRE = 10.67;
    // 1.21€/m3 : Conversion du prix de marché (0.11€/kWh) vers le volume (1m3 ≈ 11kWh)
    private static final double PRIX_GAZ_M3 = 1.21; 
    private static final double PRIX_ELEC_KWH = 0.31; 

    @org.springframework.beans.factory.annotation.Autowired
    private LogService logService;

    /**
     * Version par défaut utilisée par le Dashboard
     */
    public List<AnalytiqueFluide> analyserTout() {
        return analyserTout(FICHIER);
    }

    public List<AnalytiqueFluide> analyserTout(String cheminExcel) {
        List<AnalytiqueFluide> resultats = new ArrayList<>();
        logService.reinitialiser();

        try (FileInputStream fis = new FileInputStream(new File(cheminExcel))) {
            Workbook wb = WorkbookFactory.create(fis);
            
            analyserGaz(wb, resultats);
            analyserElec(wb, resultats);
            analyserEau(wb, resultats);

            resultats.sort((a, b) -> Double.compare(b.montantReel(), a.montantReel()));
            logService.sauvegarderFichiers();

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

    private void analyserGaz(Workbook wb, List<AnalytiqueFluide> list) {
        Sheet s = wb.getSheet("CONSO GAZ");
        if (s == null) return;

        Map<String, Accumulateur> stats = new LinkedHashMap<>();
        String siteCourant = "";

        for (int i = 9; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;

            String siteNom = getStr(r, 2).trim();
            if (!siteNom.isEmpty()) {
                siteCourant = siteNom;
            }
            if (siteCourant.isEmpty()) continue;

            Accumulateur acc = stats.computeIfAbsent(siteCourant, k -> new Accumulateur());

            for (int col = 2; col < Math.min(r.getLastCellNum(), 200); col++) {
                String valRaw = getStrRaw(r, col).toLowerCase();
                if (valRaw.contains("au") && valRaw.contains("/") && (valRaw.contains("25") || valRaw.contains("2025"))
                    && !valRaw.contains("total") && !valRaw.contains("cumul") && !valRaw.contains("annuel")) {
                    
                    double m = getVal(r, col + 3);
                    String cleUnique = valRaw + "_" + m;

                    if (acc.periodes.contains(cleUnique)) {
                        col += 3;
                        continue;
                    }
                    acc.periodes.add(cleUnique);

                    double c = getVal(r, col + 2);
                    
                    if (m < 100000 && m > 0) {
                        acc.totalConso += c;
                        acc.totalReel += m;
                        logService.ajouterLogGaz(siteCourant, col + 3, m, valRaw);
                        if (acc.dateFin == null) acc.dateFin = valRaw;
                        acc.dateDebut = valRaw;
                    }
                    col += 3;
                }
            }
        }

        // Conversion des accumulateurs en objets finaux
        stats.forEach((site, acc) -> {
            if (acc.totalConso > 0 || acc.totalReel > 0) {
                list.add(calculer(site, "Gaz", acc.totalConso, acc.totalReel, "m3", PRIX_GAZ_M3, 0, "Cumul Annuel 2025"));
            }
        });
    }

    // Petite classe interne pour accumuler les données multi-lignes
    private static class Accumulateur {
        double totalConso = 0;
        double totalReel = 0;
        String dateDebut = null;
        String dateFin = null;
        Set<String> periodes = new HashSet<>();
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
            if (!siteNom.isEmpty()) {
                siteCourant = siteNom;
            }
            if (siteCourant.isEmpty()) continue;

            Accumulateur acc = stats.computeIfAbsent(siteCourant, k -> new Accumulateur());

            for (int col = 6; col < Math.min(r.getLastCellNum(), 200); col++) {
                String valRaw = getStrRaw(r, col).toLowerCase();
                if (valRaw.contains("/") && valRaw.contains("au")
                    && !valRaw.contains("total") && !valRaw.contains("cumul") && !valRaw.contains("annuel")
                    && (valRaw.contains("25") || valRaw.contains("2025"))) {
                    
                    double m = getVal(r, col + 3);
                    // Dédoublonnage intelligent : Date + Montant
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
                        if (acc.dateFin == null) acc.dateFin = valRaw;
                        acc.dateDebut = valRaw;
                    }
                    col += 3;
                }
            }
        }

        // Conversion des accumulateurs en objets finaux
        stats.forEach((site, acc) -> {
            String periode = (acc.dateDebut != null && acc.dateFin != null) ? "du " + acc.dateDebut + " au " + acc.dateFin : "Année 2025";
            if (acc.totalConso > 0 || acc.totalReel > 0) {
                list.add(calculer(site, "Electricité", acc.totalConso, acc.totalReel, "kWh", PRIX_ELEC_KWH, 0, periode));
            }
        });
    }

    private AnalytiqueFluide calculer(String site, String fluide, double conso, double reel, String unite, double prixUnit, double fixe, String periode) {
        double theorique = (conso * prixUnit) + fixe;
        double delta = reel - theorique;
        double pourcentage = theorique > 0 ? (delta / theorique) * 100 : 0;
        boolean alerte = Math.abs(pourcentage) > 20;

        return new AnalytiqueFluide(site, fluide, conso, unite, reel, theorique, delta, pourcentage, alerte, periode);
    }

    private String getStr(Row r, int col) {
        String val = getStrRaw(r, col);
        if (val.toUpperCase().contains("TOTAL") || val.toUpperCase().contains("FACTURATION") || val.toUpperCase().contains("BILAN")) {
            return ""; 
        }
        return val;
    }

    private String getStrRaw(Row r, int col) {
        Cell c = r.getCell(col);
        return c == null ? "" : c.toString().trim();
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
}
