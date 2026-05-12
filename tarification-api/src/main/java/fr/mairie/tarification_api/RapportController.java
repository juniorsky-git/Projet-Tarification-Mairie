package fr.mairie.tarification_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/rapport")
public class RapportController {

    @Autowired
    private RapportDecisifService rapportService;

    @Autowired
    private AnalytiqueFluideService fluideService;

    @Autowired
    private DonneesBudgetaires budgetService;

    @GetMapping("/complet")
    public ResponseEntity<byte[]> telechargerRapportComplet() {
        try {
            // 1. Récupération des données budgétaires (Source de base)
            Calculateur calc = new Calculateur();
            Calculateur.SyntheseGlobale synthese = calc.getSynthese();
            
            // 2. Récupération des recettes réelles (Optionnel)
            Map<String, Double> recettes = Map.of();
            try {
                recettes = budgetService.chargerRecettesReelles();
            } catch (Exception e) {
                System.err.println("Note : Recettes réelles non chargées : " + e.getMessage());
            }

            // 3. Récupération des fluides (Optionnel)
            List<RapportSemestrielFluide> fluides = List.of();
            try {
                fluides = fluideService.analyserBiSemestriel();
            } catch (Exception e) {
                System.err.println("Note : Audit fluides non chargé : " + e.getMessage());
            }

            // 4. Génération du PDF
            byte[] pdfBytes = rapportService.genererRapportComplet(synthese, recettes, fluides);

            // 5. Retour du fichier
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Rapport_Decisionnel_Crosne_2025.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(("Erreur interne : " + e.getMessage()).getBytes());
        }
    }
}
