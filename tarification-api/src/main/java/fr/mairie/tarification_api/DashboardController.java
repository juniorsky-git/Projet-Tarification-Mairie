package fr.mairie.tarification_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * Contrôleur principal de l'API de Tarification.
 *
 * Fournit les données consolidées pour le Dashboard :
 * - Indicateurs budgétaires (Dépenses, Recettes, Taux de couverture).
 * - Diagnostic analytique des fluides.
 * - Priorité aux données de Saisie Comptable si disponibles pour l'année demandée.
 *
 * @author Stagiaire DG 2
 */
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final AnalytiqueFluideService analytiqueFluideService;
    private final DonneesBudgetaires budgetService;
    private final LogService logService;
    private final SaisieComptableService saisieService;
    private final DashboardExcelExportService excelExportService;

    public DashboardController(AnalytiqueFluideService analytiqueFluideService,
                               DonneesBudgetaires budgetService,
                               LogService logService,
                               SaisieComptableService saisieService,
                               DashboardExcelExportService excelExportService) {
        this.analytiqueFluideService = analytiqueFluideService;
        this.budgetService = budgetService;
        this.logService = logService;
        this.saisieService = saisieService;
        this.excelExportService = excelExportService;
    }

    /**
     * Endpoint d'export Excel du Dashboard, avec formatage natif (Apache POI).
     */
    @GetMapping(value = "/dashboard/export-excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportDashboardExcel(
            @RequestParam(required = false, defaultValue = "A") String source,
            @RequestParam(required = false) Integer annee) {
        try {
            List<DepensePole> poles = budgetService.chargerPolesDynamiques(source);
            Map<String, Double> recettesReelles = budgetService.chargerRecettesReelles(source);
            Map<String, Map<String, Double>> totauxSaisie = annee != null ? saisieService.getTotauxParPole(annee) : null;
            
            // On reconstruit la liste des réponses Dashboard pour l'export
            List<DashboardResponse> dashboardResponses = poles.stream().map(p -> {
                ResponseEntity<?> responseEntity = getDashboard(p.nom(), source, annee);
                if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() instanceof DashboardResponse) {
                    return (DashboardResponse) responseEntity.getBody();
                }
                return null;
            }).filter(r -> r != null).collect(Collectors.toList());

            byte[] excelData = excelExportService.exporterDashboard(annee, poles, recettesReelles, totauxSaisie, dashboardResponses);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"Tableau_de_Bord_" + (annee != null ? annee : "2025") + ".xlsx\"")
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint principal retournant l'état financier complet d'un pôle tarifaire.
     *
     * Si le paramètre `annee` est fourni et qu'il existe des données saisies pour
     * cette année en base, elles sont utilisées EN PRIORITÉ sur les fichiers Excel.
     *
     * @param pole   Le nom du pôle (ex: Restauration).
     * @param source La source de données Excel (A ou B). Ignoré si saisie disponible.
     * @param annee  L'année (optionnel). Null = données Excel 2025.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestParam String pole,
            @RequestParam(required = false, defaultValue = "A") String source,
            @RequestParam(required = false) Integer annee) {
        try {
            String decodedPole = URLDecoder.decode(pole, StandardCharsets.UTF_8).replace("+", " ").trim();

            // Priorité : données de saisie si l'année est connue en base
            boolean utiliseSaisie = (annee != null) && saisieService.getAnneesDisponibles().contains(annee);

            if (utiliseSaisie) {
                return getDashboardDepuisSaisie(decodedPole, annee, source);
            } else {
                return getDashboardDepuisExcel(decodedPole, source);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur interne du contrôleur : " + e.getMessage());
        }
    }

    /**
     * Construit la réponse dashboard depuis les données de saisie comptable (PostgreSQL).
     */
    private ResponseEntity<?> getDashboardDepuisSaisie(String pole, Integer annee, String source) {
        Map<String, Map<String, Double>> totaux = saisieService.getTotauxParPole(annee);
        Map<String, Double> totauxPole = totaux.get(pole);

        if (totauxPole == null) {
            return ResponseEntity.notFound().build();
        }

        List<DepensePole> poles = budgetService.chargerPolesDynamiques(source);
        int defaultEnfants = poles.stream().filter(p -> p.nom().equalsIgnoreCase(pole)).mapToInt(DepensePole::nombreEnfants).findFirst().orElse(0);

        // Détail des charges depuis la base (lignes de type DEPENSE)
        Map<String, Double> detailCharges = new LinkedHashMap<>();
        saisieService.getLignesBrutes(annee).stream()
            .filter(l -> l.getPole().equalsIgnoreCase(pole) && "DEPENSE".equals(l.getTypeLigne()))
            .forEach(l -> detailCharges.put(l.getLibelle(), l.getMontant()));

        int nombreEnfants = saisieService.getLignesBrutes(annee).stream()
            .filter(l -> l.getPole().equalsIgnoreCase(pole) && "STAT".equals(l.getTypeLigne()) && "Nombre d'enfants".equals(l.getLibelle()))
            .mapToInt(l -> (int) Math.round(l.getMontant()))
            .findFirst()
            .orElse(defaultEnfants);

        DashboardResponse r = new DashboardResponse();
        r.pole = pole;
        r.depensesTotales  = totauxPole.getOrDefault("depenses", 0.0);
        r.recettesTotales  = totauxPole.getOrDefault("recettes", 0.0);
        r.tauxCouverture   = totauxPole.getOrDefault("tauxCouverture", 0.0);
        r.ecart            = r.recettesTotales - r.depensesTotales;
        r.nombreEnfants    = nombreEnfants;
        r.coutUnitaire     = nombreEnfants > 0 ? (r.depensesTotales / nombreEnfants) : 0.0;
        r.unitesAnnuelles  = null;
        r.detailsCharges   = detailCharges;
        r.distributionTranches = Map.of();
        r.detailsFluides   = analytiqueFluideService.analyserParPole(pole);

        return ResponseEntity.ok(r);
    }

    /**
     * Construit la réponse dashboard depuis les fichiers Excel (comportement original 2025).
     */
    private ResponseEntity<?> getDashboardDepuisExcel(String pole, String source) {
        List<DepensePole> tousLesPoles = budgetService.chargerPolesDynamiques(source);
        Map<String, Double> recettesReelles = budgetService.chargerRecettesReelles(source);

        return tousLesPoles.stream()
                .filter(p -> p.nom().equalsIgnoreCase(pole))
                .findFirst()
                .map(p -> {
                    DashboardResponse r = new DashboardResponse();
                    r.pole             = p.nom();
                    r.depensesTotales  = p.depensesTotales();
                    r.coutUnitaire     = p.coutUnitaire();
                    r.nombreEnfants    = p.nombreEnfants();
                    r.unitesAnnuelles  = p.unitesAnnuelles();
                    r.detailsCharges   = p.chargesDetaillees();

                    double recettes    = recettesReelles.getOrDefault(p.nom(), 0.0);
                    r.recettesTotales  = recettes;
                    r.tauxCouverture   = (p.depensesTotales() > 0) ? (recettes / p.depensesTotales()) : 0;
                    r.ecart            = recettes - p.depensesTotales();
                    r.distributionTranches = p.distributionTranches();
                    r.detailsFluides   = analytiqueFluideService.analyserParPole(p.nom());

                    return ResponseEntity.ok(r);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Audit complet Réel vs Théorique des fluides. */
    @GetMapping("/analytique/fluides/audit")
    public ResponseEntity<List<AnalytiqueFluide>> getAuditComplet() {
        return ResponseEntity.ok(analytiqueFluideService.analyserTout());
    }

    /** Audit bi-semestriel des consommations (Issue #24). */
    @GetMapping("/analytique/fluides/bi-semestriel")
    public ResponseEntity<List<RapportSemestrielFluide>> getRapportBiSemestriel() {
        return ResponseEntity.ok(analytiqueFluideService.analyserBiSemestriel());
    }

    /** Logs techniques d'audit. */
    @GetMapping("/logs/audit")
    public ResponseEntity<String> getLogsAudit() {
        return ResponseEntity.ok(logService.lireDerniersLogs());
    }
}