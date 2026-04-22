package fr.mairie.tarification_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final AnalytiqueFluideService analytiqueFluideService;

    public DashboardController(AnalytiqueFluideService analytiqueFluideService) {
        this.analytiqueFluideService = analytiqueFluideService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam String pole) {
        try {
            String decodedPole = URLDecoder.decode(pole, StandardCharsets.UTF_8).replace("+", " ").trim();
            
            return DonneesBudgetaires.POLES.stream()
                    .filter(p -> p.nom().equalsIgnoreCase(decodedPole))
                    .findFirst()
                    .map(p -> {
                        DashboardResponse r = new DashboardResponse();
                        r.pole = p.nom();
                        r.depensesTotales = p.depensesTotales();
                        r.coutUnitaire = p.coutUnitaire();
                        r.nombreEnfants = p.nombreEnfants();
                        r.unitesAnnuelles = p.unitesAnnuelles();
                        r.detailsCharges = p.chargesDetaillees();
                        
                        // --- CALCUL DES RECETTES ---
                        double recettes = 0;
                        List<Tarif> tarifs = DonneesTarifs.chargerTarifsReference();
                        String serviceKey = getServiceKeyForPole(p.nom());
                        
                        if (p.distributionTranches() != null && serviceKey != null) {
                            double volumeMoyen = 1.0; 
                            if (p.unitesAnnuelles() != null && p.nombreEnfants() != null && p.nombreEnfants() > 0) {
                                volumeMoyen = (double) p.unitesAnnuelles() / p.nombreEnfants();
                            }

                            for (Map.Entry<String, Integer> entry : p.distributionTranches().entrySet()) {
                                String tranche = entry.getKey();
                                Integer count = entry.getValue();
                                
                                double prixTranche = tarifs.stream()
                                    .filter(t -> t.getTranche().equalsIgnoreCase(tranche))
                                    .findFirst()
                                    .map(t -> t.getPrix(serviceKey))
                                    .orElse(0.0);
                                
                                recettes += (count * prixTranche * volumeMoyen);
                            }
                        }
                        
                        r.recettesTotales = recettes;
                        r.tauxCouverture = (p.depensesTotales() > 0) ? (recettes / p.depensesTotales()) : 0;
                        r.ecart = recettes - p.depensesTotales();
                        r.distributionTranches = p.distributionTranches();
                        r.detailsFluides = analytiqueFluideService.genererAnalyseDetailed();
                        
                        return ResponseEntity.ok(r);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    private String getServiceKeyForPole(String pole) {
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
}