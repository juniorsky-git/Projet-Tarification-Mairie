package fr.mairie.tarification_api;

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

    /**
     * Lit les recettes réelles depuis l'onglet 'recettes' du fichier 'Depenses recettes nf.xlsx'.
     */
    public Map<String, Double> chargerRecettesReelles() {
        Map<String, Double> recettes = new java.util.HashMap<>();
        try {
            java.io.File file = new java.io.File("Depenses recettes nf.xlsx");
            if (!file.exists()) {
                file = new java.io.File("tarification-api/Depenses recettes nf.xlsx");
            }
            if (file.exists()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                     org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {
                     
                    org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("recettes");
                    if (sheet != null) {
                        org.apache.poi.ss.usermodel.Row rowTotal = sheet.getRow(10); // Ligne 11 (index 10)
                        if (rowTotal != null) {
                            double restauration = getNumericCellValue(rowTotal.getCell(1)) + getNumericCellValue(rowTotal.getCell(2));
                            double loisirs = getNumericCellValue(rowTotal.getCell(3)) + getNumericCellValue(rowTotal.getCell(4)) + getNumericCellValue(rowTotal.getCell(5));
                            double scolaire = getNumericCellValue(rowTotal.getCell(6));
                            double ados = getNumericCellValue(rowTotal.getCell(7));
                            double sejours = getNumericCellValue(rowTotal.getCell(8));
                            
                            recettes.put("Restauration", restauration);
                            recettes.put("Accueil de Loisirs", loisirs);
                            // On map le scolaire sur l'accueil périscolaire et les études surveillées (50/50 pour l'instant)
                            recettes.put("Accueil periscolaire", scolaire * 0.5);
                            recettes.put("Etudes surveillees", scolaire * 0.5);
                            recettes.put("Espace Ados", ados);
                            recettes.put("Sejours", sejours);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recettes;
    }
    
    private double getNumericCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return 0.0;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
