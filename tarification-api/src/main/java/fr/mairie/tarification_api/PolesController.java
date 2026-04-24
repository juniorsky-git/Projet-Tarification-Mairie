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

    public PolesController(DonneesBudgetaires budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * Retourne la liste complète des pôles budgétaires.
     */
    @GetMapping
    public ResponseEntity<List<DepensePole>> getAllPoles() {
        return ResponseEntity.ok(budgetService.chargerPolesDynamiques());
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
