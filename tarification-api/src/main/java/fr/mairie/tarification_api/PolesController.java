package fr.mairie.tarification_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST exposant les données budgétaires par pôle.
 * Utilise DonneesBudgetaires en mode dynamique (lecture Excel temps réel).
 */
@RestController
@RequestMapping("/api/poles")
public class PolesController {

    private final DonneesBudgetaires budgetService;
    private final AnalytiqueFluideService analytiqueFluideService;

    public PolesController(DonneesBudgetaires budgetService, AnalytiqueFluideService analytiqueFluideService) {
        this.budgetService = budgetService;
        this.analytiqueFluideService = analytiqueFluideService;
    }

    private String getServiceKey(String pole) {
        switch (pole) {
            case "Restauration": return DonneesTarifs.REPAS;
            case "Accueil de Loisirs": return DonneesTarifs.ACCUEIL_JOURNEE;
            case "Accueil periscolaire": return DonneesTarifs.PERISCOLAIRE_MATIN_SOIR;
            case "Etudes surveillees": return DonneesTarifs.ETUDES_FORFAIT_MENSUEL;
            case "Espace Ados": return DonneesTarifs.ADOS_VAC_JOURNEE_REPAS;
            case "Sejours": return DonneesTarifs.SEJOUR_5_JOURS;
            default: return null;
        }
    }

    /**
     * Retourne la liste complète des pôles budgétaires.
     */
    @GetMapping
    public ResponseEntity<?> getAllPoles() {
        List<DepensePole> poles = budgetService.chargerPolesDynamiques();
        List<Tarif> tarifs = DonneesTarifs.chargerTarifsReference();
        List<AnalytiqueFluide> fluides = analytiqueFluideService.analyserTout();

        List<Map<String, Object>> enrichis = poles.stream().map(p -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("nom", p.nom());
            map.put("depensesTotales", p.depensesTotales());
            map.put("nombreEnfants", p.nombreEnfants());
            
            // Calcul Recettes Simplifié
            double recettes = 0;
            String key = getServiceKey(p.nom());
            if (key != null && p.distributionTranches() != null) {
                double vol = (p.nombreEnfants() != null && p.nombreEnfants() > 0) 
                    ? (double) p.unitesAnnuelles() / p.nombreEnfants() : 1.0;
                for (Map.Entry<String, Integer> entry : p.distributionTranches().entrySet()) {
                    double prix = tarifs.stream()
                        .filter(t -> t.getTranche().equalsIgnoreCase(entry.getKey()))
                        .findFirst().map(t -> t.getPrix(key)).orElse(0.0);
                    recettes += (entry.getValue() * prix * vol);
                }
            }
            double couverture = (p.depensesTotales() > 0) ? (recettes / p.depensesTotales()) : 0;
            map.put("tauxCouverture", couverture);

            // Performance Fluides (Score moyen d'écart)
            double ecartMoyen = fluides.stream()
                .filter(f -> {
                     // Logique simplifiée : les bâtiments de l'éducation pour la restauration, etc.
                     if (p.nom().equals("Restauration")) return f.site().contains("GROUPE SCOLAIRE") || f.site().contains("RESTAURATION");
                     if (p.nom().equals("Accueil de Loisirs")) return f.site().contains("CENTRE DE LOISIRS") || f.site().contains("POULE");
                     return true;
                })
                .mapToDouble(AnalytiqueFluide::pourcentageEcart)
                .average().orElse(0.0);
            
            map.put("performanceEnergie", ecartMoyen);
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(enrichis);
    }

    /**
     * Retourne le détail des charges d'un pôle spécifique.
     */
    @GetMapping("/{nom}/depenses")
    public ResponseEntity<?> getDepensesByPole(@PathVariable String nom) {
        String decodedNom = URLDecoder.decode(nom, StandardCharsets.UTF_8).replace("+", " ").trim();
        return budgetService.chargerPolesDynamiques().stream()
                .filter(p -> p.nom().equalsIgnoreCase(decodedNom))
                .findFirst()
                .map(p -> ResponseEntity.ok(p.chargesDetaillees()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retourne le coût unitaire par usager pour un pôle spécifique.
     */
    @GetMapping("/{nom}/cout-unitaire")
    public ResponseEntity<?> getCoutUnitaireByPole(@PathVariable String nom) {
        String decodedNom = URLDecoder.decode(nom, StandardCharsets.UTF_8).replace("+", " ").trim();
        return budgetService.chargerPolesDynamiques().stream()
                .filter(p -> p.nom().equalsIgnoreCase(decodedNom))
                .findFirst()
                .map(p -> ResponseEntity.ok(Map.of("pole", p.nom(), "coutUnitaire", p.coutUnitaire())))
                .orElse(ResponseEntity.notFound().build());
    }
}
