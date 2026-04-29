package fr.mairie.tarification_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Contrôleur principal de l'API de Tarification.
 * 
 * Fournit les données consolidées pour le Dashboard :
 * - Indicateurs budgétaires (Dépenses, Recettes).
 * - Calcul dynamique du taux de couverture.
 * - Intégration du diagnostic analytique des fluides.
 * 
 * @author Stagiaire DG 2
 */
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final AnalytiqueFluideService analytiqueFluideService;
    private final DonneesBudgetaires budgetService;

    public DashboardController(AnalytiqueFluideService analytiqueFluideService, DonneesBudgetaires budgetService) {
        this.analytiqueFluideService = analytiqueFluideService;
        this.budgetService = budgetService;
    }

    /**
     * Endpoint principal retournant l'état financier complet d'un pôle tarifaire.
     * 
     * @param pole Le nom du pôle (ex: Restauration, Accueil de Loisirs).
     * @return Une réponse JSON contenant tous les indicateurs et le détail des fluides.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam String pole) {
        try {
            // Décodage sécurisé du paramètre URL
            String decodedPole = URLDecoder.decode(pole, StandardCharsets.UTF_8).replace("+", " ").trim();
            
            // Chargement dynamique des données (Lecture Excel fraîche)
            List<DepensePole> tousLesPoles = budgetService.chargerPolesDynamiques();

            return tousLesPoles.stream()
                    .filter(p -> {
                        return p.nom().equalsIgnoreCase(decodedPole);
                    })
                    .findFirst()
                    .map(p -> {
                        DashboardResponse r = new DashboardResponse();
                        r.pole = p.nom();
                        r.depensesTotales = p.depensesTotales();
                        r.coutUnitaire = p.coutUnitaire();
                        r.nombreEnfants = p.nombreEnfants();
                        r.unitesAnnuelles = p.unitesAnnuelles();
                        r.detailsCharges = p.chargesDetaillees();
                        
                        // --- LOGIQUE DE CALCUL DES RECETTES DYNAMIQUE ---
                        double recettes = 0;
                        List<Tarif> tarifs = DonneesTarifs.chargerTarifsReference();
                        String serviceKey = getServiceKeyForPole(p.nom());
                        
                        if (p.distributionTranches() != null && serviceKey != null) {
                            double volumeMoyen = 1.0; 
                            Integer ne = p.nombreEnfants();
                            Integer ua = p.unitesAnnuelles();
                            
                            if (ne != null && ne > 0 && ua != null) {
                                volumeMoyen = (double) ua / ne;
                            }

                            // Agrégation des recettes par tranche de Quotient Familial (QF)
                            for (Map.Entry<String, Integer> entry : p.distributionTranches().entrySet()) {
                                String tranche = entry.getKey();
                                Integer count = entry.getValue();
                                
                                double prixTranche = tarifs.stream()
                                    .filter(t -> {
                                        return t.getTranche().equalsIgnoreCase(tranche);
                                    })
                                    .findFirst()
                                    .map(t -> {
                                        return t.getPrix(serviceKey);
                                    })
                                    .orElse(0.0);
                                
                                recettes += (count * prixTranche * volumeMoyen);
                            }
                        }
                        
                        // Finalisation des indicateurs financiers
                        r.recettesTotales = recettes;
                        r.tauxCouverture = (p.depensesTotales() > 0) ? (recettes / p.depensesTotales()) : 0;
                        r.ecart = recettes - p.depensesTotales();
                        r.distributionTranches = p.distributionTranches();
                        
                        // Intégration du diagnostic fluides filtré par pôle
                        r.detailsFluides = analytiqueFluideService.analyserParPole(p.nom());
                        
                        return ResponseEntity.ok(r);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur interne du contrôleur : " + e.getMessage());
        }
    }

    /**
     * Mappe le nom d'un pôle utilisateur vers la clé technique de la grille tarifaire.
     * @param pole Nom du pôle en entrée.
     * @return Clé tarifaire correspondante ou null.
     */
    private String getServiceKeyForPole(String pole) {
        switch (pole) {
            case "Restauration": 
                return DonneesTarifs.REPAS;
            case "Accueil de Loisirs": 
                return DonneesTarifs.ACCUEIL_JOURNEE;
            case "Accueil periscolaire": 
                return DonneesTarifs.PERISCOLAIRE_MATIN_SOIR;
            case "Etudes surveillees": 
                return DonneesTarifs.ETUDES_FORFAIT_MENSUEL;
            case "Espace Ados": 
                return DonneesTarifs.ADOS_VAC_JOURNEE_REPAS;
            case "Sejours": 
                return DonneesTarifs.SEJOUR_5_JOURS;
            default: 
                return null;
        }
    }

    /**
     * Nouvel endpoint pour l'audit complet Réel vs Théorique.
     */
    @GetMapping("/analytique/fluides/audit")
    public ResponseEntity<List<AnalytiqueFluide>> getAuditComplet() {
        return ResponseEntity.ok(analytiqueFluideService.analyserTout());
    }

    /**
     * Nouvel endpoint pour l'Issue #24 : Audit bi-semestriel des consommations
     */
    @GetMapping("/analytique/fluides/bi-semestriel")
    public ResponseEntity<List<RapportSemestrielFluide>> getRapportBiSemestriel() {
        return ResponseEntity.ok(analytiqueFluideService.analyserBiSemestriel());
    }
}