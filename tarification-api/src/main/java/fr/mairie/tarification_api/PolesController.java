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
    private final SaisieComptableService saisieService;

    public PolesController(DonneesBudgetaires budgetService,
                           AnalytiqueFluideService analytiqueFluideService,
                           SaisieComptableService saisieService) {
        this.budgetService = budgetService;
        this.analytiqueFluideService = analytiqueFluideService;
        this.saisieService = saisieService;
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
     * Si `annee` est fourni et qu'il existe des données saisies pour cette année,
     * les totaux de la saisie sont utilisés à la place des fichiers Excel.
     *
     * @param source "A" (défaut) ou "B"
     * @param annee  Année de saisie (optionnel, ex: 2026)
     */
    @GetMapping
    public ResponseEntity<?> getAllPoles(
            @RequestParam(required = false, defaultValue = "A") String source,
            @RequestParam(required = false) Integer annee) {

        List<DepensePole> poles = budgetService.chargerPolesDynamiques(source);
        List<AnalytiqueFluide> fluides = analytiqueFluideService.analyserTout();

        // Données de recettes : saisie si disponible, sinon Excel
        boolean utiliseSaisie = (annee != null) && saisieService.getAnneesDisponibles().contains(annee);
        Map<String, Double> recettesReelles = budgetService.chargerRecettesReelles(source);
        Map<String, Map<String, Double>> totauxSaisie = utiliseSaisie
            ? saisieService.getTotauxParPole(annee) : null;

        List<Map<String, Object>> enrichis = poles.stream().map(p -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("nom", p.nom());

            double depenses;
            double recettes;
            int nombreEnfants;

            if (utiliseSaisie && totauxSaisie != null && totauxSaisie.containsKey(p.nom())) {
                // Priorité : données saisies
                Map<String, Double> t = totauxSaisie.get(p.nom());
                depenses = t.getOrDefault("depenses", p.depensesTotales());
                recettes = t.getOrDefault("recettes", 0.0);
                nombreEnfants = saisieService.getLignesBrutes(annee).stream()
                    .filter(l -> l.getPole().equalsIgnoreCase(p.nom()) && "STAT".equals(l.getTypeLigne()) && "Nombre d'enfants".equals(l.getLibelle()))
                    .mapToInt(l -> (int) Math.round(l.getMontant()))
                    .findFirst()
                    .orElse(p.nombreEnfants());
            } else {
                // Fallback : Excel 2025
                depenses = p.depensesTotales();
                recettes = recettesReelles.getOrDefault(p.nom(), 0.0);
                nombreEnfants = p.nombreEnfants();
            }

            double couverture = (depenses > 0) ? (recettes / depenses) : 0;
            map.put("depensesTotales", depenses);
            map.put("nombreEnfants", nombreEnfants);
            map.put("tauxCouverture", couverture);
            map.put("recettesTotales", recettes);
            map.put("source", utiliseSaisie ? "SAISIE_" + annee : source);

            // Performance Fluides
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
