package fr.mairie.tarification_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;

/**
 * Contrôleur REST du tableau de bord financier.
 *
 * Expose GET /api/dashboard?pole={nom} pour les 6 pôles du fichier CALC DEP(4).csv :
 * Restauration, Accueil de Loisirs, Espace Ados, Séjours,
 * Études surveillées, Accueil périscolaire.
 *
 * Seules les dépenses réelles sont calculées pour l'instant.
 * Les recettes seront ajoutées dans une issue future.
 *
 * @author Séri-khane YOLOU
 * @version 1.2
 */
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final SimulationCalculateur simulationCalculateur;

    public DashboardController(
            @Value("${simulation.fichier.csv}") String cheminFichierCsv) {
        this.simulationCalculateur = new SimulationCalculateur(cheminFichierCsv);
    }

    /**
     * Retourne les dépenses réelles ventilées par nature pour un pôle donné.
     *
     * Le paramètre {@code pole} correspond aux valeurs du dropdown HTML :
     * "Restauration", "Accueil de Loisirs", "Espace Ados",
     * "Sejours", "Etudes surveillees", "Accueil periscolaire"
     *
     * @param pole Nom du pôle (insensible à la casse et aux accents).
     * @return 200 OK avec un {@link DashboardResponse} JSON.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam String pole) {

        String cle = normaliserPole(pole);
        DashboardResponse response;

        switch (cle) {

            case "restauration":
                response = construireDashboard(
                        "Restauration",
                        simulationCalculateur.lireDepensesReellesRestauration());
                // Données supplémentaires spécifiques à la restauration
                response.nombreEnfants = simulationCalculateur.lireNombreEnfantsTotal();
                response.coutMoyenReel = 11.39; // "Un repas coûte en moyenne 11,39€" (CSV ligne 25)
                break;

            case "accueil de loisirs":
                response = construireDashboard(
                        "Accueil de Loisirs",
                        simulationCalculateur.lireDepensesAccueilLoisirs());
                break;

            case "espace ados":
                response = construireDashboard(
                        "Espace Ados",
                        simulationCalculateur.lireDepensesEspaceAdos());
                break;

            case "sejours":
                response = construireDashboard(
                        "Séjours",
                        simulationCalculateur.lireDepensesSejours());
                break;

            case "etudes surveillees":
                response = construireDashboard(
                        "Études surveillées",
                        simulationCalculateur.lireDepensesEtudesSurveillees());
                break;

            case "accueil periscolaire":
                response = construireDashboard(
                        "Accueil périscolaire",
                        simulationCalculateur.lireDepensesAccueilPeriscolaire());
                break;

            default:
                // Pôle inconnu — réponse vide
                response = new DashboardResponse();
                response.pole = pole;
                response.depensesTotales = 0;
                response.recettesTotales = 0;
                response.tauxCouverture  = 0;
                response.multiplicateur  = "N/A";
                response.detailsCharges  = new LinkedHashMap<>();
                break;
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Construit un {@link DashboardResponse} à partir d'une map de dépenses réelles.
     *
     * La clé "TOTAL" de la map est extraite pour renseigner {@code depensesTotales}.
     * Les recettes ne sont pas encore calculées (valeur 0).
     *
     * @param nomPole  Libellé affiché du pôle.
     * @param depenses Map {nature → montant} + clé "TOTAL" = total général.
     * @return DashboardResponse prêt à être sérialisé en JSON.
     */
    private DashboardResponse construireDashboard(
            String nomPole, java.util.Map<String, Double> depenses) {

        DashboardResponse r = new DashboardResponse();
        r.pole            = nomPole;
        r.depensesTotales = depenses.getOrDefault("TOTAL", 0.0);
        r.recettesTotales = 0;   // à implémenter dans une issue future
        r.tauxCouverture  = 0;
        r.multiplicateur  = "N/A";
        r.nombreEnfants   = 0;
        r.coutMoyenReel   = 0;

        // Détail exposé sans la clé interne "TOTAL"
        r.detailsCharges = new LinkedHashMap<>(depenses);
        r.detailsCharges.remove("TOTAL");

        return r;
    }

    /**
     * Normalise le nom d'un pôle pour la comparaison : minuscules + suppression accents.
     *
     * Permet de faire correspondre "Accueil périscolaire", "accueil periscolaire",
     * "Accueil Périscolaire" à la même clé.
     *
     * @param pole Nom brut du pôle (depuis l'URL ou le dropdown).
     * @return Clé normalisée pour le switch.
     */
    private String normaliserPole(String pole) {
        return pole.toLowerCase()
                .replace("é", "e").replace("è", "e").replace("ê", "e")
                .replace("à", "a").replace("â", "a")
                .replace("î", "i").replace("ï", "i")
                .replace("ô", "o").replace("û", "u").replace("ù", "u")
                .trim();
    }
}
