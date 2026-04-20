package fr.mairie.tarification_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;

/**
 * Contrôleur REST du tableau de bord financier.
 *
 * Expose l'endpoint GET /api/dashboard?pole={nom} qui retourne les dépenses
 * réelles ventilées par nature pour un pôle de service donné.
 *
 * Actuellement seul le pôle "Restauration" est implémenté (données CALC DEP(4).csv).
 * Les recettes ne sont pas encore calculées (valeur 0).
 *
 * @author Séri-khane YOLOU
 * @version 1.1
 */
@RestController
@RequestMapping("/api")
public class DashboardController {

    /** Calculateur de simulation, pointant vers CALC DEP(4).xlsx. */
    private final SimulationCalculateur simulationCalculateur;

    public DashboardController(
            @Value("${simulation.fichier.csv}") String cheminFichierCsv) {
        this.simulationCalculateur = new SimulationCalculateur(cheminFichierCsv);
    }

    /**
     * Retourne les indicateurs financiers agrégés pour un pôle de service.
     *
     * Pour le pôle "Restauration", les données proviennent de l'onglet
     * Simulation de CALC DEP(4).xlsx. Pour les autres pôles, une réponse
     * vide est retournée (à compléter dans les issues suivantes).
     *
     * @param pole Nom du pôle (ex: "Restauration", "Accueil de Loisirs"...).
     * @return 200 OK avec un {@link DashboardResponse} sérialisé en JSON.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam String pole) {

        DashboardResponse response = new DashboardResponse();
        response.pole = pole;

        if ("Restauration".equalsIgnoreCase(pole)) {
            response = calculerDashboardRestauration();
        } else {
            // Pôles à implémenter dans les issues suivantes
            response.depensesTotales = 0;
            response.recettesTotales = 0;
            response.tauxCouverture = 0;
            response.multiplicateur = "N/A";
            response.detailsCharges = new LinkedHashMap<>();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Calcule le tableau de bord financier de la Restauration.
     *
     * Données extraites du CSV CALC DEP(4) :
     * - Nombre d'enfants total : ligne 16, col 3 (ex: 1 128)
     * - Coût moyen réel d'un repas : 11,39 € (noté dans le fichier, ligne 25)
     * - Dépenses réelles ventilées : ligne 33 "Total général", section 2
     *   · Scolarest (prestations) : 713 752,47 €
     *   · Personnel               : 1 051 019,64 €
     *   · Alimentation            : 1 451,91 €
     *   · Eau                     : 8 159,00 €
     *   · Électricité             : 30 563,49 €
     *   · Gaz                     : 8 159,00 €
     *   · TOTAL                   : 1 813 105,51 €
     *
     * Les recettes ne sont pas calculées pour l'instant (valeur 0).
     *
     * @return Un {@link DashboardResponse} renseigné avec les dépenses réelles.
     */
    private DashboardResponse calculerDashboardRestauration() {
        DashboardResponse r = new DashboardResponse();
        r.pole = "Restauration";

        // --- Nombre d'enfants total (section 1 - ligne Total) ---
        r.nombreEnfants = simulationCalculateur.lireNombreEnfantsTotal();

        // --- Coût moyen réel d'un repas (noté dans le CSV à la ligne 25) ---
        // "Un repas coûte en moyenne 11,39€"
        r.coutMoyenReel = 11.39;

        // --- Dépenses réelles ventilées (section 2 - ligne Total général) ---
        java.util.Map<String, Double> depenses =
                simulationCalculateur.lireDepensesReellesRestauration();

        // Le total est stocké sous la clé "TOTAL" dans la map
        r.depensesTotales = depenses.getOrDefault("TOTAL", 0.0);

        // Détail des charges exposé dans le dashboard (sans la clé TOTAL)
        r.detailsCharges = new java.util.LinkedHashMap<>(depenses);
        r.detailsCharges.remove("TOTAL");

        // --- Recettes non calculées pour l'instant ---
        r.recettesTotales = 0;
        r.tauxCouverture  = 0;
        r.multiplicateur  = "N/A";

        return r;
    }
}
