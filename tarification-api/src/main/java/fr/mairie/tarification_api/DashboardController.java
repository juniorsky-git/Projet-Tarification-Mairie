package fr.mairie.tarification_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class DashboardController {

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
                        return ResponseEntity.ok(r);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}