package fr.mairie.tarification_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les données de simulation budgétaire.
 *
 * Lit l'onglet "Simulation" du fichier CALC DEP(4).xlsx et expose
 * les résultats en JSON via un endpoint GET.
 *
 * Le chemin du fichier Excel est configuré dans application.properties
 * (propriété : simulation.fichier.excel).
 *
 * @author Séri-khane YOLOU
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
public class SimulationController {

    /**
     * Calculateur pointant vers le fichier de données de simulation.
     * Le chemin est injecté depuis application.properties pour éviter
     * tout chemin codé en dur.
     */
    private final SimulationCalculateur simulationCalculateur;

    public SimulationController(
            @Value("${simulation.fichier.excel}") String cheminFichierExcel) {
        this.simulationCalculateur = new SimulationCalculateur(cheminFichierExcel);
    }

    /**
     * Retourne la liste des lignes de simulation pour la restauration scolaire.
     *
     * Chaque ligne contient : tranche, code tranche, prix facturé,
     * nombre d'enfants, coût moyen, dépense annuelle, recette annuelle,
     * écart et taux de couverture.
     *
     * @return 200 OK avec la liste JSON des {@link SimulationLigne}.
     */
    @GetMapping("/simulation/restauration")
    public ResponseEntity<?> getSimulationRestauration() {
        List<SimulationLigne> lignes = simulationCalculateur.lireSimulationRestauration();
        return ResponseEntity.ok(lignes);
    }
}
