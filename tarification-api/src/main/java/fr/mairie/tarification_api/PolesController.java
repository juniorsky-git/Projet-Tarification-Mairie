package fr.mairie.tarification_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/poles")
public class PolesController {

    @GetMapping
    public ResponseEntity<List<DepensePole>> getAllPoles() {
        return ResponseEntity.ok(DonneesBudgetaires.POLES);
    }

    @GetMapping("/{nom}/depenses")
    public ResponseEntity<?> getDepensesByPole(@PathVariable String nom) {
        String decodedNom = URLDecoder.decode(nom, StandardCharsets.UTF_8).replace("+", " ").trim();
        return DonneesBudgetaires.POLES.stream()
                .filter(p -> p.nom().equalsIgnoreCase(decodedNom))
                .findFirst()
                .map(p -> ResponseEntity.ok(p.chargesDetaillees()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{nom}/cout-unitaire")
    public ResponseEntity<?> getCoutUnitaireByPole(@PathVariable String nom) {
        String decodedNom = URLDecoder.decode(nom, StandardCharsets.UTF_8).replace("+", " ").trim();
        return DonneesBudgetaires.POLES.stream()
                .filter(p -> p.nom().equalsIgnoreCase(decodedNom))
                .findFirst()
                .map(p -> ResponseEntity.ok(Map.of("pole", p.nom(), "coutUnitaire", p.coutUnitaire())))
                .orElse(ResponseEntity.notFound().build());
    }
}
