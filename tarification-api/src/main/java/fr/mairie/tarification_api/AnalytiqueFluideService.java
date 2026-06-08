package fr.mairie.tarification_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

/**
 * Service d'Analyse et d'Audit des Fluides Municipaux.
 * Lit les données depuis fluides.json (généré localement depuis Excel).
 * Zéro lecture Excel → zéro OutOfMemoryError sur Railway.
 */
@Service
public class AnalytiqueFluideService {

    private static final double PRIX_EAU_M3      = 4.50;
    private static final double ABO_EAU_SEMESTRE  = 10.67;
    private static final double PRIX_GAZ_M3       = 1.21;
    private static final double PRIX_ELEC_KWH     = 0.31;

    @org.springframework.beans.factory.annotation.Autowired
    private LogService logService;

    // Cache chargé une seule fois au démarrage
    private List<AnalytiqueFluide>       cacheTous = null;
    private List<RapportSemestrielFluide> cacheBi   = null;

    // -------------------------------------------------------------------------
    // Chargement JSON
    // -------------------------------------------------------------------------

    private JsonNode chargerJson() throws Exception {
        ClassPathResource res = new ClassPathResource("fluides.json");
        try (InputStream is = res.getInputStream()) {
            return new ObjectMapper().readTree(is);
        }
    }

    // -------------------------------------------------------------------------
    // API publique
    // -------------------------------------------------------------------------

    public List<AnalytiqueFluide> analyserTout() {
        if (cacheTous != null) return cacheTous;
        cacheTous = construireAnalytique();
        return cacheTous;
    }

    public List<AnalytiqueFluide> analyserParPole(String pole) {
        List<AnalytiqueFluide> tous = analyserTout();
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

    public List<RapportSemestrielFluide> analyserBiSemestriel() {
        if (cacheBi != null) return cacheBi;
        cacheBi = construireBiSemestriel();
        return cacheBi;
    }

    // -------------------------------------------------------------------------
    // Construction depuis JSON
    // -------------------------------------------------------------------------

    private List<AnalytiqueFluide> construireAnalytique() {
        List<AnalytiqueFluide> resultats = new ArrayList<>();
        try {
            JsonNode root = chargerJson();

            // EAU
            for (JsonNode n : root.path("eau")) {
                String site = n.path("site").asText("");
                if (site.toUpperCase().startsWith("TOTAL FACTURATION")) continue;
                double conso = n.path("conso").asDouble();
                double reel  = n.path("reel").asDouble();
                if (conso > 0 || reel > 0) {
                    resultats.add(calculer(site, "Eau", conso, reel,
                            "m3", PRIX_EAU_M3, ABO_EAU_SEMESTRE * 2, "Année 2025"));
                }
            }
            // GAZ
            for (JsonNode n : root.path("gaz")) {
                String site = n.path("site").asText("");
                if (site.toUpperCase().startsWith("TOTAL FACTURATION")) continue;
                double conso = n.path("conso").asDouble();
                double reel  = n.path("reel").asDouble();
                if (conso > 0 || reel > 0) {
                    resultats.add(calculer(site, "Gaz", conso, reel,
                            "m3", PRIX_GAZ_M3, 0, "Année 2025"));
                }
            }
            // ELEC
            for (JsonNode n : root.path("elec")) {
                String site = n.path("site").asText("");
                if (site.toUpperCase().startsWith("TOTAL FACTURATION")) continue;
                double conso = n.path("conso").asDouble();
                double reel  = n.path("reel").asDouble();
                if (conso > 0 || reel > 0) {
                    resultats.add(calculer(site, "Electricité", conso, reel,
                            "kWh", PRIX_ELEC_KWH, 0, "Année 2025"));
                }
            }

            resultats.sort((a, b) -> Double.compare(b.montantReel(), a.montantReel()));
        } catch (Exception e) {
            System.err.println("[AnalytiqueFluideService] Erreur lecture fluides.json: " + e.getMessage());
            e.printStackTrace();
        }
        return resultats;
    }

    private List<RapportSemestrielFluide> construireBiSemestriel() {
        List<RapportSemestrielFluide> resultats = new ArrayList<>();
        try {
            JsonNode root = chargerJson();

            for (JsonNode n : root.path("eau")) {
                String site = n.path("site").asText();
                if (site.isEmpty() || site.toUpperCase().startsWith("TOTAL FACTURATION")) continue;
                resultats.add(creerRapport(site, "Eau", "m3",
                        n.path("consoS1").asDouble(), n.path("consoS2").asDouble(),
                        n.path("reelS1").asDouble(),  n.path("reelS2").asDouble()));
            }
            for (JsonNode n : root.path("gaz")) {
                String site = n.path("site").asText();
                if (site.isEmpty() || site.toUpperCase().startsWith("TOTAL FACTURATION")) continue;
                resultats.add(creerRapport(site, "Gaz", "m3",
                        n.path("consoS1").asDouble(), n.path("consoS2").asDouble(),
                        n.path("reelS1").asDouble(),  n.path("reelS2").asDouble()));
            }
            for (JsonNode n : root.path("elec")) {
                String site = n.path("site").asText();
                if (site.isEmpty() || site.toUpperCase().startsWith("TOTAL FACTURATION")) continue;
                resultats.add(creerRapport(site, "Electricité", "kWh",
                        n.path("consoS1").asDouble(), n.path("consoS2").asDouble(),
                        n.path("reelS1").asDouble(),  n.path("reelS2").asDouble()));
            }
        } catch (Exception e) {
            System.err.println("[AnalytiqueFluideService] Erreur lecture biSemestriel: " + e.getMessage());
        }
        return resultats;
    }

    // -------------------------------------------------------------------------
    // Helpers de calcul
    // -------------------------------------------------------------------------

    private AnalytiqueFluide calculer(String site, String fluide, double conso, double reel,
                                      String unite, double prixUnit, double fixe, String periode) {
        double theorique   = (conso * prixUnit) + fixe;
        double delta       = reel - theorique;
        double pourcentage = theorique > 0 ? (delta / theorique) * 100 : 0;
        return new AnalytiqueFluide(site, fluide, conso, unite, reel, theorique,
                delta, pourcentage, Math.abs(pourcentage) > 20, periode);
    }

    private RapportSemestrielFluide creerRapport(String site, String fluide, String unite,
                                                  double s1Vol, double s2Vol,
                                                  double s1Eur, double s2Eur) {
        double totalVol = s1Vol + s2Vol;
        double totalEur = s1Eur + s2Eur;
        double delta    = s1Vol > 0 ? ((s2Vol - s1Vol) / s1Vol) * 100 : 0;
        return new RapportSemestrielFluide(site, fluide, s1Vol, s2Vol, totalVol,
                s1Eur, s2Eur, totalEur, delta, Math.abs(delta) > 20, "", unite);
    }
}
