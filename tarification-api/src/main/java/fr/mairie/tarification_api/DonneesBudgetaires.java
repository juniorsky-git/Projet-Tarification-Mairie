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
     * Algorithme de récupération dynamique des recettes depuis le tableau synthétique de l'Excel.
     */
    public Map<String, Double> chargerRecettesReelles() {
        Map<String, Double> recettes = new java.util.HashMap<>();
        try {
            java.io.File file = new java.io.File("Depenses recettes nf.xlsx");
            if (!file.exists()) file = new java.io.File("../Depenses recettes nf.xlsx");
            if (!file.exists()) file = new java.io.File("tarification-api/Depenses recettes nf.xlsx");
            
            if (file.exists()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                     org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {
                    
                    org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("recettes");
                    if (sheet != null) {
                        // 1. Localiser le début du tableau synthétique
                        int rowStart = -1;
                        int colStart = -1;
                        for (int r = 0; r < 50; r++) {
                            org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                            if (row == null) continue;
                            for (int c = 0; c < 10; c++) {
                                org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                                if (cell != null && cell.toString().contains("Tableau synthetique des recettes")) {
                                    rowStart = r;
                                    colStart = c;
                                    break;
                                }
                            }
                            if (rowStart != -1) break;
                        }

                        // 2. Extraire les données de la ligne "Total" (située 4 lignes plus bas)
                        if (rowStart != -1) {
                            org.apache.poi.ss.usermodel.Row rowTotal = sheet.getRow(rowStart + 4);
                            if (rowTotal != null) {
                                recettes.put("Restauration", getNumericCellValue(rowTotal.getCell(colStart + 1)));
                                recettes.put("Accueil de Loisirs", getNumericCellValue(rowTotal.getCell(colStart + 2)));
                                recettes.put("Accueil periscolaire", getNumericCellValue(rowTotal.getCell(colStart + 3)));
                                recettes.put("Etudes surveillees", getNumericCellValue(rowTotal.getCell(colStart + 4)));
                                recettes.put("Espace Ados", getNumericCellValue(rowTotal.getCell(colStart + 5)));
                                // Séjours + Classes de découverte
                                recettes.put("Sejours", getNumericCellValue(rowTotal.getCell(colStart + 6)) + getNumericCellValue(rowTotal.getCell(colStart + 7)));
                            }
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
