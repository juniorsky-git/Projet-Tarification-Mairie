package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service métier pour la saisie comptable multi-années.
 *
 * Responsabilités :
 *   1. Initialiser les données d'une nouvelle année à partir des données 2025 (Excel).
 *   2. Sauvegarder/mettre à jour les lignes saisies par les agents.
 *   3. Fournir les totaux (dépenses/recettes) pour mise à jour du dashboard.
 *   4. Générer un export XLSX au format CALC DEP.
 *   5. Réinitialiser une année vers les données 2025 de référence.
 *
 * @author Stagiaire DG 2
 */
@Service
public class SaisieComptableService {

    private final LigneSaisieRepository repository;
    private final DonneesBudgetaires budgetService;

    /** Pôles reconnus dans l'application */
    private static final List<String> POLES = List.of(
        "Restauration", "Accueil de Loisirs", "Accueil periscolaire",
        "Etudes surveillees", "Espace Ados", "Sejours"
    );

    /** Charges prédéfinies par pôle (libellés standard) */
    private static final Map<String, List<String>> CHARGES_PREDEFINES = Map.of(
        "Restauration",      List.of("Prestataire Scolarest", "Alimentation", "Eau", "Gaz", "Electricité", "Personnel"),
        "Accueil de Loisirs",List.of("Prestataire extérieur", "Alimentation", "Eau", "Gaz", "Electricité", "Personnel"),
        "Accueil periscolaire", List.of("Alimentation", "Eau", "Gaz", "Electricité", "Personnel"),
        "Etudes surveillees",List.of("Personnel", "Fournitures", "Eau", "Electricité"),
        "Espace Ados",       List.of("Prestataire activités", "Transport", "Alimentation", "Personnel"),
        "Sejours",           List.of("Hébergement", "Transport", "Alimentation", "Encadrement", "Assurance")
    );

    /** Recettes prédéfinies standard */
    private static final List<String> RECETTES_PREDEFINES = List.of(
        "Recettes familles (tarification)", "Subvention CAF", "Subvention Ville", "Autres recettes"
    );

    public SaisieComptableService(LigneSaisieRepository repository, DonneesBudgetaires budgetService) {
        this.repository = repository;
        this.budgetService = budgetService;
    }

    // =========================================================================
    // INITIALISATION D'UNE NOUVELLE ANNÉE
    // =========================================================================

    /**
     * Initialise les lignes d'une nouvelle année à partir des données de référence 2025.
     * Si des données existent déjà pour cette année, elles ne sont PAS écrasées.
     *
     * @param annee L'année à initialiser (ex: 2026)
     * @return Le nombre de lignes créées (0 si déjà initialisé)
     */
    @Transactional
    public int initialiserAnnee(Integer annee) {
        if (repository.existsByAnnee(annee)) {
            return 0; // Déjà initialisé, on ne touche à rien
        }

        // Charger les données réelles de 2025 depuis les fichiers Excel
        List<DepensePole> poles2025 = budgetService.chargerPolesDynamiques("A");
        Map<String, Double> recettes2025 = budgetService.chargerRecettesReelles("A");

        List<LigneSaisie> lignesACreer = new ArrayList<>();

        for (DepensePole pole : poles2025) {
            // --- DÉPENSES : lignes prédéfinies avec montants 2025 ---
            Map<String, Double> charges = pole.chargesDetaillees();
            List<String> libellesPredefinis = CHARGES_PREDEFINES.getOrDefault(pole.nom(), List.of());

            double sommeMappee = 0.0;
            for (String libelle : libellesPredefinis) {
                // Cherche le montant correspondant dans les charges 2025 (correspondance souple)
                double montant = charges.entrySet().stream()
                    .filter(e -> e.getKey().toLowerCase().contains(libelle.substring(0, Math.min(6, libelle.length())).toLowerCase()))
                    .mapToDouble(Map.Entry::getValue)
                    .findFirst().orElse(0.0);

                LigneSaisie ligne = new LigneSaisie(annee, pole.nom(), "DEPENSE", libelle, montant, true);
                ligne.setCommentaire("Valeur de référence 2025 — à mettre à jour");
                lignesACreer.add(ligne);
                sommeMappee += montant;
            }

            // --- Ajouter le reliquat pour que le total soit exactement égal à 2025 ---
            double totalReel2025 = pole.depensesTotales();
            double restant = totalReel2025 - sommeMappee;
            if (restant > 0.01 || restant < -0.01) {
                LigneSaisie ligneReliquat = new LigneSaisie(annee, pole.nom(), "DEPENSE", "Autres charges (reliquat 2025)", restant, true);
                ligneReliquat.setCommentaire("Charges de 2025 non classifiées dans les catégories standard");
                lignesACreer.add(ligneReliquat);
            }

            // --- RECETTES : lignes prédéfinies avec montants 2025 ---
            double totalRecettes2025 = recettes2025.getOrDefault(pole.nom(), 0.0);
            lignesACreer.add(new LigneSaisie(annee, pole.nom(), "RECETTE",
                "Recettes familles (tarification)", totalRecettes2025, true));
            lignesACreer.add(new LigneSaisie(annee, pole.nom(), "RECETTE",
                "Subvention CAF", 0.0, true));
            lignesACreer.add(new LigneSaisie(annee, pole.nom(), "RECETTE",
                "Subvention Ville", 0.0, true));
            lignesACreer.add(new LigneSaisie(annee, pole.nom(), "RECETTE",
                "Autres recettes", 0.0, true));
        }

        repository.saveAll(lignesACreer);
        return lignesACreer.size();
    }

    // =========================================================================
    // LECTURE
    // =========================================================================

    /**
     * Retourne toutes les lignes saisies pour une année, groupées par pôle.
     * Structure : { "Restauration": { "DEPENSE": [...], "RECETTE": [...] }, ... }
     */
    public Map<String, Map<String, List<LigneSaisie>>> getLignesParAnnee(Integer annee) {
        List<LigneSaisie> lignes = repository.findByAnneeOrderByPoleAscLibelleAsc(annee);
        Map<String, Map<String, List<LigneSaisie>>> result = new LinkedHashMap<>();

        for (LigneSaisie ligne : lignes) {
            result.computeIfAbsent(ligne.getPole(), k -> new LinkedHashMap<>())
                  .computeIfAbsent(ligne.getTypeLigne(), k -> new ArrayList<>())
                  .add(ligne);
        }
        return result;
    }

    /**
     * Retourne la liste plate de toutes les lignes d'une année.
     */
    public List<LigneSaisie> getLignesBrutes(Integer annee) {
        return repository.findByAnneeOrderByPoleAscLibelleAsc(annee);
    }

    /**
     * Retourne la liste des années disponibles dans la base.
     */
    public List<Integer> getAnneesDisponibles() {
        return repository.findDistinctAnnees();
    }

    /**
     * Calcule les totaux dépenses/recettes par pôle pour une année.
     * Utilisé pour mettre à jour le dashboard.
     */
    public Map<String, Map<String, Double>> getTotauxParPole(Integer annee) {
        Map<String, Map<String, Double>> totaux = new LinkedHashMap<>();
        for (String pole : POLES) {
            Map<String, Double> t = new LinkedHashMap<>();
            t.put("depenses", repository.sumDepensesByAnneeAndPole(annee, pole));
            t.put("recettes", repository.sumRecettesByAnneeAndPole(annee, pole));
            double dep = t.get("depenses");
            t.put("tauxCouverture", dep > 0 ? (t.get("recettes") / dep) : 0.0);
            totaux.put(pole, t);
        }
        return totaux;
    }

    public List<Map<String, Object>> getComparatif(Integer anneeSource, Integer anneeRefForcee) {
        Integer anneeRef = anneeRefForcee != null ? anneeRefForcee : (anneeSource - 1);
        
        // On récupère d d'abord la structure des pôles depuis 2025 pour avoir la liste complète
        List<DepensePole> poles2025 = budgetService.chargerPolesDynamiques("A");
        Map<String, Double> recettes2025 = budgetService.chargerRecettesReelles("A");

        // Totaux de l'année de référence (N-1 si en base, sinon Excel 2025)
        boolean hasRefDB = repository.existsByAnnee(anneeRef);
        Map<String, Map<String, Double>> totauxRef = hasRefDB ? getTotauxParPole(anneeRef) : new HashMap<>();

        // Données de la nouvelle année (base de données)
        Map<String, Map<String, Double>> totauxNouvelleAnnee = getTotauxParPole(anneeSource);

        List<Map<String, Object>> comparatif = new ArrayList<>();
        for (DepensePole pole : poles2025) {
            Map<String, Object> ligne = new LinkedHashMap<>();
            ligne.put("pole", pole.nom());
            
            ligne.put("anneeRefTexte", hasRefDB ? String.valueOf(anneeRef) : "2025 (Référence initiale)");

            // Référence
            double depRef = hasRefDB ? totauxRef.getOrDefault(pole.nom(), Map.of("depenses", 0.0)).getOrDefault("depenses", 0.0) : pole.depensesTotales();
            double recRef = hasRefDB ? totauxRef.getOrDefault(pole.nom(), Map.of("recettes", 0.0)).getOrDefault("recettes", 0.0) : recettes2025.getOrDefault(pole.nom(), 0.0);
            double tcRef = depRef > 0 ? recRef / depRef : 0.0;

            ligne.put("dep2025", depRef);
            ligne.put("rec2025", recRef);
            ligne.put("tc2025", tcRef);

            // Nouvelle année
            Map<String, Double> totaux = totauxNouvelleAnnee.getOrDefault(pole.nom(), Map.of("depenses", 0.0, "recettes", 0.0));
            double depN = totaux.getOrDefault("depenses", 0.0);
            double recN = totaux.getOrDefault("recettes", 0.0);
            double tcN = depN > 0 ? recN / depN : 0.0;

            ligne.put("depN", depN);
            ligne.put("recN", recN);
            ligne.put("tcN", tcN);

            // Écarts
            ligne.put("ecartDep", depN - depRef);
            ligne.put("ecartRec", recN - recRef);
            ligne.put("ecartDepPct", depRef > 0 ? ((depN - depRef) / depRef) * 100 : 0.0);
            ligne.put("ecartRecPct", recRef > 0 ? ((recN - recRef) / recRef) * 100 : 0.0);

            comparatif.add(ligne);
        }
        return comparatif;
    }

    // =========================================================================
    // SAUVEGARDE / MISE À JOUR
    // =========================================================================

    /**
     * Sauvegarde ou met à jour une liste de lignes (upsert par ID).
     * Si l'ID est null, la ligne est créée. Sinon elle est mise à jour.
     */
    @Transactional
    public List<LigneSaisie> sauvegarder(List<LigneSaisie> lignes) {
        return repository.saveAll(lignes);
    }

    /**
     * Supprime une ligne par son ID.
     * Ne peut pas supprimer une ligne prédéfinie (sécurité).
     */
    @Transactional
    public boolean supprimerLigne(Long id) {
        return repository.findById(id).map(ligne -> {
            if (Boolean.TRUE.equals(ligne.getPredefinie())) {
                return false; // Refus : ligne système
            }
            repository.deleteById(id);
            return true;
        }).orElse(false);
    }

    /**
     * Réinitialise une année en supprimant toutes ses données
     * et en réinitialisant à partir de 2025.
     */
    @Transactional
    public int reinitialiserAnnee(Integer annee) {
        List<LigneSaisie> lignes = repository.findByAnneeOrderByPoleAscLibelleAsc(annee);
        repository.deleteAll(lignes);
        return initialiserAnnee(annee);
    }

    // =========================================================================
    // EXPORT XLSX (Format CALC DEP)
    // =========================================================================

    /**
     * Génère un fichier XLSX au format CALC DEP avec les données saisies pour une année.
     * Le fichier produit reprend l'organisation par pôle : Dépenses puis Recettes.
     *
     * @param annee L'année à exporter.
     * @return Le contenu binaire du fichier XLSX.
     */
    public byte[] exporterXlsx(Integer annee) throws IOException {
        Map<String, Map<String, List<LigneSaisie>>> donnees = getLignesParAnnee(annee);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- Styles ---
            CellStyle styleTitre = workbook.createCellStyle();
            Font fontTitre = workbook.createFont();
            fontTitre.setBold(true);
            fontTitre.setFontHeightInPoints((short) 14);
            styleTitre.setFont(fontTitre);
            styleTitre.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            styleTitre.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font fontTitreBlanc = workbook.createFont();
            fontTitreBlanc.setBold(true);
            fontTitreBlanc.setColor(IndexedColors.WHITE.getIndex());
            fontTitreBlanc.setFontHeightInPoints((short) 14);
            styleTitre.setFont(fontTitreBlanc);

            CellStyle styleEntete = workbook.createCellStyle();
            Font fontEntete = workbook.createFont();
            fontEntete.setBold(true);
            styleEntete.setFont(fontEntete);
            styleEntete.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleEntete.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleEntete.setBorderBottom(BorderStyle.THIN);

            CellStyle stylePredefini = workbook.createCellStyle();
            Font fontPredefini = workbook.createFont();
            fontPredefini.setBold(true);
            stylePredefini.setFont(fontPredefini);

            CellStyle styleMontant = workbook.createCellStyle();
            styleMontant.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 €"));

            CellStyle styleTotal = workbook.createCellStyle();
            Font fontTotal = workbook.createFont();
            fontTotal.setBold(true);
            fontTotal.setFontHeightInPoints((short) 11);
            styleTotal.setFont(fontTotal);
            styleTotal.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            styleTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleTotal.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 €"));
            styleTotal.setBorderTop(BorderStyle.MEDIUM);

            // === FEUILLE PRINCIPALE : SYNTHÈSE PAR PÔLE ===
            Sheet sheetSynthese = workbook.createSheet("Synthèse " + annee);
            sheetSynthese.setColumnWidth(0, 8000);  // Libellé
            sheetSynthese.setColumnWidth(1, 5000);  // Montant
            sheetSynthese.setColumnWidth(2, 8000);  // Commentaire
            sheetSynthese.setColumnWidth(3, 4000);  // Type

            // Titre principal
            Row rowTitre = sheetSynthese.createRow(0);
            Cell cellTitre = rowTitre.createCell(0);
            cellTitre.setCellValue("SAISIE COMPTABLE — Mairie de Crosne — Exercice " + annee);
            cellTitre.setCellStyle(styleTitre);

            int rowIdx = 2;

            for (Map.Entry<String, Map<String, List<LigneSaisie>>> poleEntry : donnees.entrySet()) {
                String nomPole = poleEntry.getKey();
                Map<String, List<LigneSaisie>> typeMap = poleEntry.getValue();

                // En-tête pôle
                Row rowPole = sheetSynthese.createRow(rowIdx++);
                Cell cellPole = rowPole.createCell(0);
                cellPole.setCellValue("► " + nomPole.toUpperCase());
                cellPole.setCellStyle(styleEntete);
                rowPole.createCell(1).setCellStyle(styleEntete);
                rowPole.createCell(2).setCellStyle(styleEntete);
                rowPole.createCell(3).setCellStyle(styleEntete);

                // En-têtes colonnes
                Row rowHeader = sheetSynthese.createRow(rowIdx++);
                String[] headers = {"Libellé", "Montant (€)", "Commentaire", "Type"};
                for (int i = 0; i < headers.length; i++) {
                    Cell c = rowHeader.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(styleEntete);
                }

                // Dépenses
                List<LigneSaisie> depenses = typeMap.getOrDefault("DEPENSE", List.of());
                double totalDep = 0;
                for (LigneSaisie dep : depenses) {
                    Row row = sheetSynthese.createRow(rowIdx++);
                    Cell cLib = row.createCell(0);
                    cLib.setCellValue(dep.getLibelle());
                    if (dep.getPredefinie()) cLib.setCellStyle(stylePredefini);

                    Cell cMont = row.createCell(1);
                    cMont.setCellValue(dep.getMontant());
                    cMont.setCellStyle(styleMontant);

                    row.createCell(2).setCellValue(dep.getCommentaire() != null ? dep.getCommentaire() : "");
                    row.createCell(3).setCellValue("Dépense");
                    totalDep += dep.getMontant();
                }

                // Ligne total dépenses
                Row rowTotalDep = sheetSynthese.createRow(rowIdx++);
                Cell cTDLib = rowTotalDep.createCell(0);
                cTDLib.setCellValue("TOTAL DÉPENSES " + nomPole.toUpperCase());
                cTDLib.setCellStyle(styleTotal);
                Cell cTDMont = rowTotalDep.createCell(1);
                cTDMont.setCellValue(totalDep);
                cTDMont.setCellStyle(styleTotal);

                // Recettes
                List<LigneSaisie> recettes = typeMap.getOrDefault("RECETTE", List.of());
                double totalRec = 0;
                for (LigneSaisie rec : recettes) {
                    Row row = sheetSynthese.createRow(rowIdx++);
                    Cell cLib = row.createCell(0);
                    cLib.setCellValue(rec.getLibelle());
                    if (rec.getPredefinie()) cLib.setCellStyle(stylePredefini);

                    Cell cMont = row.createCell(1);
                    cMont.setCellValue(rec.getMontant());
                    cMont.setCellStyle(styleMontant);

                    row.createCell(2).setCellValue(rec.getCommentaire() != null ? rec.getCommentaire() : "");
                    row.createCell(3).setCellValue("Recette");
                    totalRec += rec.getMontant();
                }

                // Ligne total recettes
                Row rowTotalRec = sheetSynthese.createRow(rowIdx++);
                Cell cTRLib = rowTotalRec.createCell(0);
                cTRLib.setCellValue("TOTAL RECETTES " + nomPole.toUpperCase());
                cTRLib.setCellStyle(styleTotal);
                Cell cTRMont = rowTotalRec.createCell(1);
                cTRMont.setCellValue(totalRec);
                cTRMont.setCellStyle(styleTotal);

                // Ligne taux de couverture
                Row rowTC = sheetSynthese.createRow(rowIdx++);
                double tc = totalDep > 0 ? (totalRec / totalDep) * 100 : 0;
                rowTC.createCell(0).setCellValue("Taux de couverture");
                rowTC.createCell(1).setCellValue(String.format("%.1f%%", tc));

                rowIdx++; // Ligne vide de séparation
            }

            // === FEUILLE 2 : COMPARATIF ===
            List<Map<String, Object>> comparatifData = getComparatif(annee, null);
            if (!comparatifData.isEmpty()) {
                Sheet sheetComp = workbook.createSheet("Comparatif " + annee);
                sheetComp.setColumnWidth(0, 8000); // Pôle
                sheetComp.setColumnWidth(1, 4000); // Dép. Réf
                sheetComp.setColumnWidth(2, 4000); // Dép. Année
                sheetComp.setColumnWidth(3, 4000); // Écart Dép.
                sheetComp.setColumnWidth(4, 4000); // Rec. Réf
                sheetComp.setColumnWidth(5, 4000); // Rec. Année
                sheetComp.setColumnWidth(6, 4000); // Écart Rec.
                sheetComp.setColumnWidth(7, 3000); // TC Réf
                sheetComp.setColumnWidth(8, 3000); // TC Année

                // Styles spécifiques Comparatif
                CellStyle styleEcartPositif = workbook.createCellStyle();
                styleEcartPositif.cloneStyleFrom(styleMontant);
                Font fontPositif = workbook.createFont();
                fontPositif.setColor(IndexedColors.RED.getIndex());
                fontPositif.setBold(true);
                styleEcartPositif.setFont(fontPositif);

                CellStyle styleEcartNegatif = workbook.createCellStyle();
                styleEcartNegatif.cloneStyleFrom(styleMontant);
                Font fontNegatif = workbook.createFont();
                fontNegatif.setColor(IndexedColors.GREEN.getIndex());
                fontNegatif.setBold(true);
                styleEcartNegatif.setFont(fontNegatif);

                CellStyle styleEcartNeutre = workbook.createCellStyle();
                styleEcartNeutre.cloneStyleFrom(styleMontant);
                Font fontNeutre = workbook.createFont();
                fontNeutre.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
                styleEcartNeutre.setFont(fontNeutre);

                CellStyle stylePourcent = workbook.createCellStyle();
                stylePourcent.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));

                Row rowTitreComp = sheetComp.createRow(0);
                Cell cellTitreComp = rowTitreComp.createCell(0);
                cellTitreComp.setCellValue("COMPARATIF ANNÉE PRÉCÉDENTE VS SAISIE " + annee);
                cellTitreComp.setCellStyle(styleTitre);

                Row rowHeaderComp = sheetComp.createRow(2);
                String[] headersComp = {
                    "PÔLE", "DÉP. RÉF.", "DÉP. " + annee, "ÉCART DÉP.", 
                    "REC. RÉF.", "REC. " + annee, "ÉCART REC.", 
                    "TC RÉF.", "TC " + annee
                };
                for (int i = 0; i < headersComp.length; i++) {
                    Cell c = rowHeaderComp.createCell(i);
                    c.setCellValue(headersComp[i]);
                    c.setCellStyle(styleEntete);
                }

                int rowIdxComp = 3;
                for (Map<String, Object> ligne : comparatifData) {
                    Row row = sheetComp.createRow(rowIdxComp++);
                    
                    Cell cPole = row.createCell(0);
                    cPole.setCellValue((String) ligne.get("pole"));
                    cPole.setCellStyle(stylePredefini);

                    row.createCell(1).setCellValue((Double) ligne.get("dep2025"));
                    row.getCell(1).setCellStyle(styleMontant);
                    
                    row.createCell(2).setCellValue((Double) ligne.get("depN"));
                    row.getCell(2).setCellStyle(styleMontant);

                    double ecartDep = (Double) ligne.get("ecartDep");
                    Cell cEcartDep = row.createCell(3);
                    cEcartDep.setCellValue(ecartDep);
                    cEcartDep.setCellStyle(ecartDep > 0 ? styleEcartPositif : (ecartDep < 0 ? styleEcartNegatif : styleEcartNeutre));

                    row.createCell(4).setCellValue((Double) ligne.get("rec2025"));
                    row.getCell(4).setCellStyle(styleMontant);
                    
                    row.createCell(5).setCellValue((Double) ligne.get("recN"));
                    row.getCell(5).setCellStyle(styleMontant);

                    double ecartRec = (Double) ligne.get("ecartRec");
                    Cell cEcartRec = row.createCell(6);
                    cEcartRec.setCellValue(ecartRec);
                    // Pour les recettes, un écart positif est "vert", négatif est "rouge"
                    cEcartRec.setCellStyle(ecartRec > 0 ? styleEcartNegatif : (ecartRec < 0 ? styleEcartPositif : styleEcartNeutre));

                    row.createCell(7).setCellValue((Double) ligne.get("tc2025"));
                    row.getCell(7).setCellStyle(stylePourcent);

                    row.createCell(8).setCellValue((Double) ligne.get("tcN"));
                    row.getCell(8).setCellStyle(stylePourcent);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
