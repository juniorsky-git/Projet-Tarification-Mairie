package fr.mairie.tarification_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.*;

/**
 * Moteur de calcul financier pour la tarification municipale.
 * Lit directement synthese.json (généré localement depuis CALC DEP (3).xlsx).
 * Zéro lecture Excel → zéro OutOfMemoryError sur Railway.
 */
public class Calculateur {

    public static class SyntheseGlobale {
        public Map<String, Map<String, Double>> depenses      = new LinkedHashMap<>();
        public Map<String, Map<String, Double>> tarifs        = new LinkedHashMap<>();
        public Map<String, Double>              effectifs     = new LinkedHashMap<>();
        public Map<String, Double>              totauxDepenses = new HashMap<>();
    }

    private SyntheseGlobale syntheseCachee = null;

    public SyntheseGlobale getSynthese() {
        if (syntheseCachee != null) return syntheseCachee;

        SyntheseGlobale sg = new SyntheseGlobale();
        try {
            ClassPathResource res = new ClassPathResource("synthese.json");
            try (InputStream is = res.getInputStream()) {
                JsonNode root = new ObjectMapper().readTree(is);

                // depenses : { "Restauration": { "Personnel": 12345.0, ... }, ... }
                root.path("depenses").fields().forEachRemaining(e -> {
                    Map<String, Double> charges = new LinkedHashMap<>();
                    e.getValue().fields().forEachRemaining(c -> charges.put(c.getKey(), c.getValue().asDouble()));
                    sg.depenses.put(e.getKey(), charges);
                });

                // totauxDepenses
                root.path("totauxDepenses").fields()
                        .forEachRemaining(e -> sg.totauxDepenses.put(e.getKey(), e.getValue().asDouble()));

                // effectifs
                root.path("effectifs").fields()
                        .forEachRemaining(e -> sg.effectifs.put(e.getKey(), e.getValue().asDouble()));

                // tarifs : { "Tranche A": { "Restauration": 4.5, ... }, ... }
                root.path("tarifs").fields().forEachRemaining(e -> {
                    Map<String, Double> t = new HashMap<>();
                    e.getValue().fields().forEachRemaining(c -> t.put(c.getKey(), c.getValue().asDouble()));
                    sg.tarifs.put(e.getKey(), t);
                });
            }
            syntheseCachee = sg;
        } catch (Exception e) {
            System.err.println("[Calculateur] Erreur lecture synthese.json: " + e.getMessage());
            e.printStackTrace();
        }
        return sg;
    }
}
