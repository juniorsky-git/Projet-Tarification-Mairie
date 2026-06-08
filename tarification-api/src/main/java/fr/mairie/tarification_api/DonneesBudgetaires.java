package fr.mairie.tarification_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

/**
 * Service de gestion des données budgétaires dynamiques.
 *
 * Lit les données depuis des fichiers JSON pré-générés (zéro Excel, zéro OOM sur Railway) :
 *   - synthese.json           (généré depuis CALC DEP (3).xlsx)
 *   - recettes_source_a.json  (généré depuis Depenses recettes nf.xlsx)
 *   - vf_rec_dep.json         (généré depuis VF_REC_DEP.xlsx, peut être vide)
 *
 * SOURCE A (par défaut) :
 *   - Dépenses : synthese.json (via Calculateur)
 *   - Recettes : recettes_source_a.json
 *
 * SOURCE B (VF_REC_DEP) — Approche hybride :
 *   - Dépenses RE/SC/CL : vf_rec_dep.json
 *   - Pôles absents : repli Source A
 */
@Service
public class DonneesBudgetaires {

    private final Calculateur calculateur = new Calculateur();

    private static final String POLE_RESTAURATION = "Restauration";
    private static final String POLE_SCOLAIRE     = "Scolaire";
    private static final String POLE_LOISIRS      = "Accueil de Loisirs";

    private static final String[] POLES_SOURCE_A = {
        "Restauration", "Accueil de Loisirs", "Accueil periscolaire",
        "Etudes surveillees", "Espace Ados", "Sejours"
    };

    // =========================================================================
    // API PUBLIQUE
    // =========================================================================

    public List<DepensePole> chargerPolesDynamiques(String source) {
        if ("B".equalsIgnoreCase(source)) return chargerPolesDynamiquesHybride();
        return chargerPolesDynamiquesSourceA();
    }

    public List<DepensePole> chargerPolesDynamiques() {
        return chargerPolesDynamiquesSourceA();
    }

    public Map<String, Double> chargerRecettesReelles(String source) {
        if ("B".equalsIgnoreCase(source)) return chargerRecettesReellesHybride();
        return chargerRecettesReellesSourceA();
    }

    public Map<String, Double> chargerRecettesReelles() {
        return chargerRecettesReellesSourceA();
    }

    // =========================================================================
    // SOURCE A — JSON synthese.json + recettes_source_a.json
    // =========================================================================

    private List<DepensePole> chargerPolesDynamiquesSourceA() {
        List<DepensePole> poles = new ArrayList<>();
        Calculateur.SyntheseGlobale sg = calculateur.getSynthese();
        Map<String, Integer> effectifsJson = chargerEffectifsJson();

        for (String nom : POLES_SOURCE_A) {
            double total           = sg.totauxDepenses.getOrDefault(nom, 0.0);
            Map<String, Double> charges = sg.depenses.getOrDefault(nom, Map.of());

            double effectifTotal = effectifsJson.containsKey(nom)
                    ? effectifsJson.get(nom)
                    : sg.effectifs.values().stream().mapToDouble(Double::doubleValue).sum();

            double coutUnitaire = (effectifTotal > 0) ? (total / effectifTotal) : 0;

            poles.add(new DepensePole(
                nom, total, coutUnitaire, (int) effectifTotal,
                nom.equals(POLE_RESTAURATION) ? 157920 : null,
                charges,
                Map.copyOf(calculerDistributionTranches(sg))
            ));
        }
        return poles;
    }

    private Map<String, Double> cacheRecettesSourceA = null;

    private Map<String, Double> chargerRecettesReellesSourceA() {
        if (cacheRecettesSourceA != null) return cacheRecettesSourceA;
        Map<String, Double> recettes = new HashMap<>();
        try {
            ClassPathResource res = new ClassPathResource("recettes_source_a.json");
            try (InputStream is = res.getInputStream()) {
                JsonNode root = new ObjectMapper().readTree(is);
                root.fields().forEachRemaining(e -> recettes.put(e.getKey(), e.getValue().asDouble()));
            }
        } catch (Exception e) {
            System.err.println("[DonneesBudgetaires] Erreur lecture recettes_source_a.json: " + e.getMessage());
        }
        cacheRecettesSourceA = recettes;
        return recettes;
    }

    // =========================================================================
    // SOURCE B — vf_rec_dep.json + repli Source A
    // =========================================================================

    private List<DepensePole> cachePolesSourceB = null;

    private List<DepensePole> chargerPolesDynamiquesHybride() {
        List<DepensePole> poles = new ArrayList<>();
        poles.addAll(chargerPolesDepuisVfRecDep());

        List<DepensePole> polesSourceA = chargerPolesDynamiquesSourceA();
        String[] polesRepli = {"Accueil periscolaire", "Etudes surveillees", "Espace Ados", "Sejours"};
        for (String nomRepli : polesRepli) {
            polesSourceA.stream()
                .filter(p -> p.nom().equalsIgnoreCase(nomRepli))
                .findFirst()
                .ifPresent(poles::add);
        }
        return poles;
    }

    private List<DepensePole> chargerPolesDepuisVfRecDep() {
        if (cachePolesSourceB != null) return cachePolesSourceB;
        List<DepensePole> poles = new ArrayList<>();
        try {
            ClassPathResource res = new ClassPathResource("vf_rec_dep.json");
            if (!res.exists()) return poles;

            try (InputStream is = res.getInputStream()) {
                JsonNode root = new ObjectMapper().readTree(is);
                Map<String, Integer> effectifsJson = chargerEffectifsJson();

                for (JsonNode poleNode : root.path("poles")) {
                    String nom     = poleNode.path("nom").asText();
                    double total   = poleNode.path("total").asDouble();
                    int effectif   = effectifsJson.getOrDefault(nom, (int) poleNode.path("effectif").asDouble());
                    double coutU   = effectif > 0 ? total / effectif : 0;

                    Map<String, Double> charges = new HashMap<>();
                    poleNode.path("charges").fields().forEachRemaining(
                        e -> charges.put(e.getKey(), e.getValue().asDouble())
                    );

                    poles.add(new DepensePole(nom, total, coutU, effectif, effectif, charges, Map.of()));
                }
            }
        } catch (Exception e) {
            System.err.println("[DonneesBudgetaires] Erreur lecture vf_rec_dep.json: " + e.getMessage());
        }
        cachePolesSourceB = poles;
        return poles;
    }

    private Map<String, Double> cacheRecettesSourceB = null;

    private Map<String, Double> chargerRecettesReellesHybride() {
        if (cacheRecettesSourceB != null) return cacheRecettesSourceB;
        Map<String, Double> recettes = new HashMap<>(chargerRecettesReellesSourceA());

        try {
            ClassPathResource res = new ClassPathResource("vf_rec_dep.json");
            if (!res.exists()) { cacheRecettesSourceB = recettes; return recettes; }

            try (InputStream is = res.getInputStream()) {
                JsonNode root = new ObjectMapper().readTree(is);
                JsonNode recNode = root.path("recettes");
                recNode.fields().forEachRemaining(e -> {
                    double v = e.getValue().asDouble();
                    if (v > 0) recettes.put(e.getKey(), v);
                });
            }
        } catch (Exception e) {
            System.err.println("[DonneesBudgetaires] Erreur lecture recettes hybrides: " + e.getMessage());
        }
        cacheRecettesSourceB = recettes;
        return recettes;
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    private Map<String, Integer> cacheEffectifsJson = null;

    /**
     * Lit les effectifs annuels depuis consommations_2025.json (si présent).
     */
    private Map<String, Integer> chargerEffectifsJson() {
        if (cacheEffectifsJson != null) return cacheEffectifsJson;
        Map<String, Integer> effectifs = new HashMap<>();
        try {
            ClassPathResource res = new ClassPathResource("Donnees/consommations_2025.json");
            if (!res.exists()) {
                cacheEffectifsJson = effectifs;
                return effectifs;
            }
            try (InputStream is = res.getInputStream()) {
                JsonNode root = new ObjectMapper().readTree(is);
                JsonNode polesNode = root.path("poles");
                if (polesNode.isObject()) {
                    polesNode.fieldNames().forEachRemaining(poleName -> {
                        JsonNode poleInfo = polesNode.get(poleName);
                        if (poleInfo.has("total")) {
                            effectifs.put(poleName, poleInfo.get("total").asInt());
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] consommations_2025.json introuvable ou invalide. Repli sur synthese.json.");
        }
        cacheEffectifsJson = effectifs;
        return effectifs;
    }

    private Map<String, Integer> calculerDistributionTranches(Calculateur.SyntheseGlobale sg) {
        Map<String, Integer> dist = new HashMap<>();
        sg.effectifs.forEach((tranche, nb) -> dist.put(tranche, nb.intValue()));
        return dist;
    }
}
