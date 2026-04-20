package fr.mairie.tarification_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST du tableau de bord financier.
 *
 * Expose l'endpoint GET /api/dashboard?pole={nom} qui agrège les données
 * de simulation budgétaire (dépenses, recettes, taux de couverture, etc.)
 * pour un pôle de service donné.
 *
 * Actuellement seul le pôle "Restauration" est alimenté par les données
 * de l'onglet Simulation de CALC DEP(4).xlsx. Les autres pôles retournent
 * des valeurs vides en attendant leur implémentation.
 *
 * @author Séri-khane YOLOU
 * @version 1.0
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
     * Calcule les indicateurs financiers de la Restauration à partir des
     * lignes de simulation lues dans CALC DEP(4).xlsx.
     *
     * @return Un {@link DashboardResponse} renseigné avec les totaux et détails.
     */
    private DashboardResponse calculerDashboardRestauration() {
        List<SimulationLigne> lignes = simulationCalculateur.lireSimulationRestauration();

        DashboardResponse r = new DashboardResponse();
        r.pole = "Restauration";
        r.detailsCharges = new LinkedHashMap<>();

        double totalDepenses = 0;
        double totalRecettes = 0;
        double sommePrixFacture = 0;
        double sommeCoutMoyen = 0;
        int nbLignes = 0;

        for (SimulationLigne l : lignes) {
            totalDepenses += l.depenseAnnuelle;
            totalRecettes += l.recetteAnnuelle;
            sommePrixFacture += l.prixFacture;
            sommeCoutMoyen += l.coutMoyen;
            nbLignes++;

            // Détail des dépenses par tranche
            String cle = l.codeTranche.isEmpty() ? l.tranche : l.codeTranche;
            r.detailsCharges.put(cle, l.depenseAnnuelle);
        }

        r.depensesTotales = totalDepenses;
        r.recettesTotales = totalRecettes;

        // Taux de couverture : recettes / dépenses × 100
        r.tauxCouverture = (totalDepenses > 0)
                ? Math.round((totalRecettes / totalDepenses) * 10000.0) / 100.0
                : 0;

        // Multiplicateur : coût moyen / prix facturé moyen (sur l'ensemble des tranches)
        if (nbLignes > 0 && sommePrixFacture > 0) {
            double ratio = sommeCoutMoyen / sommePrixFacture;
            r.multiplicateur = String.format("%.2f", ratio);
        } else {
            r.multiplicateur = "N/A";
        }

        return r;
    }
}
