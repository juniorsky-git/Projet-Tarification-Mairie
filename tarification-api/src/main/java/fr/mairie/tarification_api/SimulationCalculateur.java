package fr.mairie.tarification_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

/**
 * Lecteur des données Simulation depuis simulation.json.
 * Zéro lecture Excel → zéro OutOfMemoryError sur Railway.
 */
public class SimulationCalculateur {

    private JsonNode simRoot = null;

    private JsonNode getRoot() {
        if (simRoot != null) return simRoot;
        try {
            ClassPathResource res = new ClassPathResource("simulation.json");
            try (InputStream is = res.getInputStream()) {
                simRoot = new ObjectMapper().readTree(is);
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur lecture simulation.json: " + e.getMessage());
            simRoot = new ObjectMapper().createObjectNode();
        }
        return simRoot;
    }

    // Constructeur gardé pour compatibilité (le paramètre fichierExcel n'est plus utilisé)
    public SimulationCalculateur(String fichierExcel) {
        // fichier Excel ignoré, on lit simulation.json
    }

    private List<SimulationLigne> cacheSimulationRestauration = null;

    public List<SimulationLigne> lireSimulationRestauration() {
        if (cacheSimulationRestauration != null) return cacheSimulationRestauration;
        List<SimulationLigne> lignes = new ArrayList<>();
        try {
            for (JsonNode n : getRoot().path("restauration")) {
                SimulationLigne s = new SimulationLigne();
                s.tranche         = n.path("tranche").asText();
                s.codeTranche     = n.path("codeTranche").asText();
                s.prixFacture     = n.path("prixFacture").asDouble();
                s.nombreEnfants   = n.path("nombreEnfants").asDouble();
                s.coutMoyen       = n.path("coutMoyen").asDouble();
                s.depenseAnnuelle = n.path("depenseAnnuelle").asDouble();
                s.recetteAnnuelle = n.path("recetteAnnuelle").asDouble();
                s.ecart           = n.path("ecart").asDouble();
                s.tauxCouverture  = n.path("tauxCouverture").asDouble();
                lignes.add(s);
            }
        } catch (Exception e) {
            System.err.println("[SimulationCalculateur] Erreur restauration: " + e.getMessage());
        }
        cacheSimulationRestauration = lignes;
        return lignes;
    }

    public double lireNombreEnfantsTotal() {
        return getRoot().path("totalEnfants").asDouble(0);
    }

    public Map<String, Double> lireDepensesReellesRestauration() {
        return jsonNodeToMap(getRoot().path("depensesReelles"));
    }

    public Map<String, Double> lireDepensesAccueilLoisirs() {
        return jsonNodeToMap(getRoot().path("depensesAccueilLoisirs"));
    }

    public Map<String, Double> lireDepensesEtudesSurveillees() {
        return jsonNodeToMap(getRoot().path("depensesEtudesSurveillees"));
    }

    public Map<String, Double> lireDepensesEspaceAdos() {
        return jsonNodeToMap(getRoot().path("depensesEspaceAdos"));
    }

    public Map<String, Double> lireDepensesSejours() {
        return jsonNodeToMap(getRoot().path("depensesSejours"));
    }

    public Map<String, Double> lireDepensesAccueilPeriscolaire() {
        return jsonNodeToMap(getRoot().path("depensesPeriscolaire"));
    }

    private Map<String, Double> jsonNodeToMap(JsonNode node) {
        Map<String, Double> map = new LinkedHashMap<>();
        if (node == null || node.isMissingNode()) return map;
        node.fields().forEachRemaining(e -> map.put(e.getKey(), e.getValue().asDouble()));
        return map;
    }
}
