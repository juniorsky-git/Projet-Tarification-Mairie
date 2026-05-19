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
     * @param source "A" (défaut, CALC DEP(4) + Depenses recettes nf.xlsx)
     *               ou "B" (VF_REC_DEP.xlsx + repli A pour pôles manquants)
     */
    @GetMapping
    public ResponseEntity<?> getAllPoles(
            @RequestParam(required = false, defaultValue = "A") String source) {

        List<DepensePole> poles = budgetService.chargerPolesDynamiques(source);
        List<AnalytiqueFluide> fluides = analytiqueFluideService.analyserTout();

        // Chargement des recettes réelles depuis la source active
        Map<String, Double> recettesReelles = budgetService.chargerRecettesReelles(source);

        List<Map<String, Object>> enrichis = poles.stream().map(p -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("nom", p.nom());
            map.put("depensesTotales", p.depensesTotales());
            map.put("nombreEnfants", p.nombreEnfants());

            // Calcul du Taux de Couverture avec les recettes réelles
            double recettes = recettesReelles.getOrDefault(p.nom(), 0.0);
            double couverture = (p.depensesTotales() > 0) ? (recettes / p.depensesTotales()) : 0;

            map.put("tauxCouverture", couverture);
            map.put("recettesTotales", recettes);
            map.put("source", source); // utile pour debug côté frontend

            // Performance Fluides (Score moyen d'écart par pôle)
            double ecartMoyen = fluides.stream()
                .filter(f -> {
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
    public ResponseEntity<?> getDepensesByPole(
            @PathVariable String nom,
            @RequestParam(required = false, defaultValue = "A") String source) {
        String decodedNom = URLDecoder.decode(nom, StandardCharsets.UTF_8).replace("+", " ").trim();
        return budgetService.chargerPolesDynamiques(source).stream()
                .filter(p -> p.nom().equalsIgnoreCase(decodedNom))
                .findFirst()
                .map(p -> ResponseEntity.ok(p.chargesDetaillees()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retourne le coût unitaire par usager pour un pôle spécifique.
     */
    @GetMapping("/{nom}/cout-unitaire")
    public ResponseEntity<?> getCoutUnitaireByPole(
            @PathVariable String nom,
            @RequestParam(required = false, defaultValue = "A") String source) {
        String decodedNom = URLDecoder.decode(nom, StandardCharsets.UTF_8).replace("+", " ").trim();
        return budgetService.chargerPolesDynamiques(source).stream()
                .filter(p -> p.nom().equalsIgnoreCase(decodedNom))
                .findFirst()
                .map(p -> ResponseEntity.ok(Map.of("pole", p.nom(), "coutUnitaire", p.coutUnitaire())))
                .orElse(ResponseEntity.notFound().build());
    }
}
