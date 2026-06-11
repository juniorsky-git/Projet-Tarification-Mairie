package fr.mairie.tarification_api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la saisie comptable multi-années.
 *
 * Endpoints disponibles :
 *   GET  /api/saisie/annees                    — Liste des années disponibles
 *   POST /api/saisie/{annee}/initialiser       — Initialise une nouvelle année depuis 2025
 *   GET  /api/saisie/{annee}                   — Toutes les lignes d'une année
 *   GET  /api/saisie/{annee}/totaux            — Totaux par pôle (pour le dashboard)
 *   GET  /api/saisie/{annee}/comparatif        — Comparatif vs 2025
 *   POST /api/saisie/lignes                    — Sauvegarde une liste de lignes
 *   DELETE /api/saisie/lignes/{id}             — Supprime une ligne (non prédéfinie)
 *   POST /api/saisie/{annee}/reinitialiser     — Réinitialise une année vers 2025
 *   GET  /api/saisie/{annee}/export            — Export XLSX format CALC DEP
 *
 * @author Stagiaire DG 2
 */
@RestController
@RequestMapping("/api/saisie")
public class SaisieComptableController {

    private final SaisieComptableService service;

    public SaisieComptableController(SaisieComptableService service) {
        this.service = service;
    }

    // =========================================================================
    // GESTION DES ANNÉES
    // =========================================================================

    /**
     * Liste toutes les années ayant des données saisies.
     */
    @GetMapping("/annees")
    public ResponseEntity<List<Integer>> getAnnees() {
        return ResponseEntity.ok(service.getAnneesDisponibles());
    }

    /**
     * Initialise une nouvelle année à partir des données de référence 2025.
     * Idempotent : si l'année existe déjà, retourne 0 lignes créées.
     */
    @PostMapping("/{annee}/initialiser")
    public ResponseEntity<Map<String, Object>> initialiserAnnee(@PathVariable Integer annee) {
        int nbLignes = service.initialiserAnnee(annee);
        return ResponseEntity.ok(Map.of(
            "annee", annee,
            "lignesCreees", nbLignes,
            "message", nbLignes > 0
                ? annee + " initialisée avec " + nbLignes + " lignes depuis la référence 2025."
                : "L'année " + annee + " avait déjà des données — aucune modification."
        ));
    }

    // =========================================================================
    // LECTURE
    // =========================================================================

    /**
     * Retourne toutes les lignes d'une année en liste plate (pour le formulaire JS).
     */
    @GetMapping("/{annee}")
    public ResponseEntity<List<LigneSaisie>> getLignes(@PathVariable Integer annee) {
        return ResponseEntity.ok(service.getLignesBrutes(annee));
    }

    /**
     * Retourne les totaux par pôle (dépenses, recettes, taux de couverture).
     * Utilisé par le dashboard pour se rafraîchir.
     */
    @GetMapping("/{annee}/totaux")
    public ResponseEntity<Map<String, Map<String, Double>>> getTotaux(@PathVariable Integer annee) {
        return ResponseEntity.ok(service.getTotauxParPole(annee));
    }

    /**
     * Retourne le comparatif entre l'année de référence et l'année saisie.
     */
    @GetMapping("/{annee}/comparatif")
    public ResponseEntity<List<Map<String, Object>>> getComparatif(
            @PathVariable Integer annee,
            @RequestParam(required = false) Integer anneeRef) {
        return ResponseEntity.ok(service.getComparatif(annee, anneeRef));
    }

    // =========================================================================
    // ÉCRITURE
    // =========================================================================

    /**
     * Sauvegarde une liste de lignes (création ou mise à jour).
     * Les lignes avec ID null sont créées, celles avec ID sont mises à jour.
     */
    @PostMapping("/lignes")
    public ResponseEntity<List<LigneSaisie>> sauvegarder(@RequestBody List<LigneSaisie> lignes) {
        List<LigneSaisie> sauvegardees = service.sauvegarder(lignes);
        return ResponseEntity.ok(sauvegardees);
    }

    /**
     * Supprime une ligne par son ID.
     * Refuse la suppression si la ligne est prédéfinie (code 403).
     */
    @DeleteMapping("/lignes/{id}")
    public ResponseEntity<Map<String, Object>> supprimerLigne(@PathVariable Long id) {
        boolean supprime = service.supprimerLigne(id);
        if (supprime) {
            return ResponseEntity.ok(Map.of("succes", true, "message", "Ligne supprimée."));
        } else {
            return ResponseEntity.status(403).body(Map.of(
                "succes", false,
                "message", "Impossible de supprimer une ligne système prédéfinie."
            ));
        }
    }

    /**
     * Réinitialise une année entière en effaçant toutes ses données
     * et en recréant les lignes de référence depuis 2025.
     */
    @PostMapping("/{annee}/reinitialiser")
    public ResponseEntity<Map<String, Object>> reinitialiserAnnee(@PathVariable Integer annee) {
        int nbLignes = service.reinitialiserAnnee(annee);
        return ResponseEntity.ok(Map.of(
            "annee", annee,
            "lignesRecreees", nbLignes,
            "message", "Données de " + annee + " réinitialisées depuis la référence 2025 (" + nbLignes + " lignes)."
        ));
    }

    // =========================================================================
    // EXPORT XLSX
    // =========================================================================

    /**
     * Génère et retourne un fichier XLSX au format CALC DEP
     * avec toutes les données saisies pour l'année donnée.
     */
    @GetMapping("/{annee}/export")
    public ResponseEntity<byte[]> exporterXlsx(
            @PathVariable Integer annee,
            @RequestParam(required = false) Integer anneeRef) {
        try {
            byte[] xlsx = service.exporterXlsx(annee, anneeRef);
            String nomFichier = "SAISIE_COMPTABLE_" + annee + "_" + LocalDate.now() + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
