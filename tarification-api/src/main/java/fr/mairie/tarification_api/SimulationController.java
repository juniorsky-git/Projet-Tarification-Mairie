package fr.mairie.tarification_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les données de simulation budgétaire.
 *
 * Lit l'onglet "Simulation" du fichier CALC DEP(4).xlsx et expose
 * les résultats en JSON via un endpoint GET.
 *
 * @author Séri-khane YOLOU
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
public class SimulationController {

    /**
     * Calculateur pointant vers le fichier de données de simulation.
     * Le chemin est relatif au répertoire de lancement de l'application.
     */
    private final SimulationCalculateur simulationCalculateur =
            new SimulationCalculateur("Donnees/Autres/CALC DEP(4).xlsx");

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
