package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    // 0.31€/kWh : Prix incluant l'abonnement et la hausse du TURPE de juillet 2025
    private static final double PRIX_ELEC_KWH = 0.31; 

    public List<AnalytiqueFluide> genererAnalyseDetailed() {
        List<AnalytiqueFluide> analyses = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            analyserEau(wb, analyses);
            analyserGaz(wb, analyses);
            analyserElec(wb, analyses);

        } catch (Exception e) {
            System.err.println("[AnalytiqueFluideService] Erreur technique : " + e.getMessage());
        }
        
        return analyses;
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

        for (int i = 9; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;

            String site = getStr(r, 2);
            if (site.trim().isEmpty()) continue;

            double totalConso = 0;
            double totalReel = 0;
            String dateDebut = null;
            String dateFin = null;
            
            for (int col = 2; col < Math.min(r.getLastCellNum(), 150); col++) {
                String val = getStrRaw(r, col);
                if (val.toLowerCase().startsWith("du") && (val.contains("25") || val.contains("2025"))) {
                    double c = getVal(r, col + 2);
                    double m = getVal(r, col + 3);
                    if (m < 100000) {
                        totalConso += c;
                        totalReel += m;
                        try {
                            String[] parts = val.toLowerCase().replace("du ", "").split(" au ");
                            if (parts.length == 2) {
                                if (dateFin == null) dateFin = parts[1].trim(); 
                                dateDebut = parts[0].trim(); 
                            }
                        } catch (Exception e) {}
                    }
                    col += 3;
                }
            }

            String periodeReelle = (dateDebut != null && dateFin != null) ? "du " + dateDebut + " au " + dateFin : "Année 2025";
            if (totalConso > 0 || totalReel > 0) {
                list.add(calculer(site, "Gaz", totalConso, totalReel, "m3", PRIX_GAZ_M3, 0, "Cumul Annuel 2025 (" + periodeReelle + ")"));
            }
        }
    }

    private void analyserElec(Workbook wb, List<AnalytiqueFluide> list) {
        Sheet s = wb.getSheet("CONSO ELEC");
        if (s == null) return;

        for (int i = 9; i <= s.getLastRowNum(); i++) {
            Row r = s.getRow(i);
            if (r == null) continue;

            String site = getStr(r, 5);
            if (site.trim().isEmpty()) continue;

            double totalConso = 0;
            double totalReel = 0;
            String dateDebut = null;
            String dateFin = null;
            
            for (int col = 6; col < Math.min(r.getLastCellNum(), 150); col++) {
                String val = getStrRaw(r, col);
                if (val.toLowerCase().startsWith("du") && (val.contains("25") || val.contains("2025"))) {
                    double c = getVal(r, col + 2);
                    double m = getVal(r, col + 3);
                    
                    if (m < 100000) {
                        totalConso += c;
                        totalReel += m;
                        
                        // Extraction intelligente des bornes "du [Début] au [Fin]"
                        try {
                            String[] parts = val.toLowerCase().replace("du ", "").split(" au ");
                            if (parts.length == 2) {
                                if (dateFin == null) dateFin = parts[1].trim(); // La première trouvée (souvent la plus récente à gauche)
                                dateDebut = parts[0].trim(); // On met à jour pour garder la plus ancienne (à droite)
                            }
                        } catch (Exception e) {}
                    }
                    col += 3;
                }
            }

            String periodeReelle = (dateDebut != null && dateFin != null) ? "du " + dateDebut + " au " + dateFin : "Année 2025";
            if (totalConso > 0 || totalReel > 0) {
                list.add(calculer(site, "Electricité", totalConso, totalReel, "kWh", PRIX_ELEC_KWH, 0, periodeReelle));
            }
        }
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
