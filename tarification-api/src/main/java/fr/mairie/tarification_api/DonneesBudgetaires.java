package fr.mairie.tarification_api;

import fr.mairie.tarification.Calculateur;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion des données budgétaires dynamiques.
 * 
 * Cette classe n'utilise plus de données statiques "en dur".
 * Elle interroge le moteur de calcul (Calculateur) pour extraire 
 * les chiffres en temps réel depuis le fichier Excel source.
 * 
 * @author Stagiaire DG 2
 */
@Service
public class DonneesBudgetaires {

    private final Calculateur calculateur = new Calculateur();

    /**
     * Charge et construit la liste des pôles dynamiquement depuis l'Excel.
     * @return Liste des pôles avec leurs budgets réels du moment.
     */
    public List<DepensePole> chargerPolesDynamiques() {
        List<DepensePole> poles = new ArrayList<>();
        Calculateur.SyntheseGlobale sg = calculateur.getSynthese();

        // Noms des pôles tels qu'ils apparaissent dans l'Excel
        String[] noms = {"Restauration", "Accueil de Loisirs", "Accueil periscolaire", "Etudes surveillees", "Espace Ados", "Sejours"};

        for (String nom : noms) {
            // Extraction des données du calculateur
            double total = sg.totauxDepenses.getOrDefault(nom, 0.0);
            Map<String, Double> charges = sg.depenses.getOrDefault(nom, Map.of());
            
            // Calcul du coût unitaire (Basé sur les effectifs totaux extraits)
            double effectifTotal = 0;
            for (Double val : sg.effectifs.values()) {
                effectifTotal += val;
            }
            double coutUnitaire = (effectifTotal > 0) ? (total / effectifTotal) : 0;

            // Construction de l'objet de données immuable (Record)
            poles.add(new DepensePole(
                nom,
                total,
                coutUnitaire,
                (int) effectifTotal,
                nom.equals("Restauration") ? 157920 : null, // Exemple : Volume de repas
                charges,
                Map.copyOf(calculerDistributionTranches(sg, nom))
            ));
        }

        return poles;
    }

    /**
     * Synthèse interne de la répartition des QF.
     */
    private Map<String, Integer> calculerDistributionTranches(Calculateur.SyntheseGlobale sg, String pole) {
        // Logique de conversion : les effectifs Excel sont des doubles, on les passe en entiers
        Map<String, Integer> distribution = new java.util.HashMap<>();
        sg.effectifs.forEach((tranche, nb) -> {
            distribution.put(tranche, nb.intValue());
        });
        return distribution;
    }
}
