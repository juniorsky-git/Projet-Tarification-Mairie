package fr.mairie.tarification;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service d'export PDF pour le rapport financier municipal.
 * Genere un rapport complet incluant :
 *   - Une page de garde
 *   - Un tableau de bord par pole (6 poles)
 *   - Un tableau recapitulatif global
 *   - La grille tarifaire complete par Quotient Familial (2 pages)
 *
 * Librairie : Apache PDFBox (incluse dans lib/pdfbox.jar)
 */
public class PdfExportService {

    // --- Dimensions page A4 (en points PDF, 1pt = 1/72 pouce) ---
    private static final float PAGE_WIDTH  = PDRectangle.A4.getWidth();   // ~595
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();  // ~842
    private static final float MARGIN = 50f;

    // --- Palette de couleurs ---
    private static final Color BLEU_MAIRIE = new Color(30,  80, 150);
    private static final Color BLEU_ENTETE = new Color(60,  90, 140);
    private static final Color BLEU_TOTAL  = new Color(40,  70, 130);
    private static final Color GRIS_CLAIR  = new Color(245, 245, 245);
    private static final Color VERT        = new Color(0,  150,  80);
    private static final Color ORANGE      = new Color(230, 120,   0);
    private static final Color ROUGE       = new Color(200,  40,  40);

    // --- Configuration des 6 poles (nom Excel, multiplicateur) ---
    private static final Object[][] POLES_CONFIG = {
        {"Restauration",        140.0},
        {"Accueil de Loisirs",   1.0},
        {"Espace Ados",          1.0},
        {"Sejours",              1.0},
        {"Etudes surveillees",  10.0},
        {"Accueil periscolaire",10.0}
    };

    // =========================================================
    // METHODE PRINCIPALE
    // =========================================================

    /**
     * Genere le rapport PDF complet et retourne le chemin absolu du fichier cree.
     *
     * @param calc   Instance du Calculateur (acces aux donnees Excel)
     * @param grille Grille tarifaire chargee depuis DonneesTarifs
     * @return Chemin absolu du fichier PDF genere
     * @throws IOException En cas d'erreur d'ecriture
     */
    public String genererRapport(Calculateur calc, List<Tarif> grille) throws IOException {
        File dossier = new File("rapports");
        if (!dossier.exists()) dossier.mkdirs();

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String chemin = "rapports/rapport_financier_" + date + ".pdf";

        try (PDDocument doc = new PDDocument()) {
            dessinerPageGarde(doc, date);

            for (Object[] poleConf : POLES_CONFIG) {
                dessinerSectionPole(doc, (String) poleConf[0], (double) poleConf[1], calc);
            }

            dessinerTableauRecap(doc, calc);
            dessinerPageGrille1(doc, grille);
            dessinerPageGrille2(doc, grille);

            doc.save(chemin);
        }

        return new File(chemin).getAbsolutePath();
    }

    // =========================================================
    // PAGE DE GARDE (page 1)
    // =========================================================

    private void dessinerPageGarde(PDDocument doc, String dateStr) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            // Bandeau bleu en haut
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.addRect(0, PAGE_HEIGHT - 210, PAGE_WIDTH, 210);
            cs.fill();

            // "VILLE DE CROSNE"
            cs.setNonStrokingColor(Color.WHITE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 28);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 75);
            cs.showText("VILLE DE CROSNE");
            cs.endText();

            // Sous-titre
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 13);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 105);
            cs.showText("Direction des Services aux Familles");
            cs.endText();

            // Titre du rapport
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 150);
            cs.showText("Rapport Financier - Services Municipaux 2025");
            cs.endText();

            // Date de generation
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 185);
            cs.showText("Genere le : " + dateStr);
            cs.endText();

            // Separateur
            cs.setStrokingColor(BLEU_MAIRIE);
            cs.setLineWidth(1.5f);
            cs.moveTo(MARGIN, PAGE_HEIGHT - 240);
            cs.lineTo(PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 240);
            cs.stroke();

            // ---- Section 1 : Tableaux de bord financiers ----
            float y = PAGE_HEIGHT - 270;
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("1. Tableaux de bord financiers (6 poles)");
            cs.endText();
            y -= 22;

            String[] lignesPoles = {
                "  Restauration scolaire         (multiplicateur : 140 jours/an)",
                "  Accueil de loisirs            (forfait annuel)",
                "  Espace Ados                   (forfait annuel)",
                "  Sejours                       (forfait par sejour)",
                "  Etudes surveillees            (x10 mois/an)",
                "  Accueil periscolaire          (x10 mois/an)"
            };
            for (String ligne : lignesPoles) {
                cs.setNonStrokingColor(new Color(55, 55, 55));
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(ligne);
                cs.endText();
                y -= 18;
            }

            // ---- Section 2 : Recapitulatif ----
            y -= 8;
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("2. Tableau recapitulatif global (synthese tous poles)");
            cs.endText();
            y -= 22;

            // ---- Section 3 : Grille tarifaire ----
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("3. Grille tarifaire complete par Quotient Familial (QF)");
            cs.endText();
            y -= 20;

            String[] lignesServices = {
                "  Repas scolaire, Accueil loisirs journee / demi-journee",
                "  Periscolaire matin+soir / matin ou soir",
                "  Etudes surveillees : forfait mensuel / demi-forfait",
                "  Ados vacances : journee avec ou sans repas, demi-journee",
                "  Ados sorties : demi-journee / journee"
            };
            for (String s : lignesServices) {
                cs.setNonStrokingColor(new Color(55, 55, 55));
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(s);
                cs.endText();
                y -= 18;
            }

            // ---- Encadre de legende couleur ----
            y -= 15;
            cs.setNonStrokingColor(new Color(240, 240, 240));
            cs.addRect(MARGIN, y - 65, PAGE_WIDTH - 2 * MARGIN, 75);
            cs.fill();
            cs.setStrokingColor(new Color(200, 200, 200));
            cs.setLineWidth(0.5f);
            cs.addRect(MARGIN, y - 65, PAGE_WIDTH - 2 * MARGIN, 75);
            cs.stroke();

            cs.setNonStrokingColor(new Color(50, 50, 50));
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.newLineAtOffset(MARGIN + 8, y - 12);
            cs.showText("Legende du taux de couverture des charges :");
            cs.endText();

            String[][] legende = {
                {"≥ 80 %", "Satisfaisant"},
                {"≥ 50 %", "Modere"},
                {"< 50 %", "Faible"}
            };
            Color[] legendeColors = {VERT, ORANGE, ROUGE};
            float lx = MARGIN + 8;
            float ly = y - 30;
            for (int i = 0; i < legende.length; i++) {
                cs.setNonStrokingColor(legendeColors[i]);
                cs.addRect(lx, ly - 8, 12, 12);
                cs.fill();
                cs.setNonStrokingColor(new Color(50, 50, 50));
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                cs.newLineAtOffset(lx + 16, ly);
                cs.showText(legende[i][0] + " : " + legende[i][1]);
                cs.endText();
                lx += 145;
            }

            dessinerPiedDePage(cs, 1);
        }
    }

    // =========================================================
    // SECTION PAR POLE (pages 2 a 7)
    // =========================================================

    private void dessinerSectionPole(PDDocument doc, String pole, double multiplicateur,
                                     Calculateur calc) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        double depenses = calc.calculerTotalDepenses(pole);
        double recettes = calc.calculerRecettesAnnuelles(pole, multiplicateur);
        Map<String, Double> details = calc.getDepensesDetaillees(pole);
        double taux = (depenses > 0) ? (recettes / depenses * 100) : 0;
        Color couleurTaux = couleurPourTaux(taux);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            // Bandeau d'en-tete
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.addRect(0, PAGE_HEIGHT - 80, PAGE_WIDTH, 80);
            cs.fill();

            cs.setNonStrokingColor(Color.WHITE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 15);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 33);
            cs.showText("TABLEAU DE BORD : " + traduitNomPole(pole).toUpperCase());
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 57);
            String multLabel = multiplicateur == 140
                ? "Recettes calculees sur 140 jours de cantine / an"
                : multiplicateur > 1
                    ? "Recettes calculees sur " + (int) multiplicateur + " mois / an"
                    : "Recettes calculees sur base forfaitaire annuelle";
            cs.showText(multLabel);
            cs.endText();

            float y = PAGE_HEIGHT - 105;

            // --- Titre section charges ---
            cs.setNonStrokingColor(new Color(40, 40, 40));
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("DETAIL DES CHARGES PAR NATURE COMPTABLE");
            cs.endText();
            y -= 6;

            float col2x = PAGE_WIDTH - MARGIN - 130;
            float rowH = 20f;

            // En-tete du tableau
            cs.setNonStrokingColor(BLEU_ENTETE);
            cs.addRect(MARGIN, y - rowH, PAGE_WIDTH - 2 * MARGIN, rowH);
            cs.fill();
            cs.setNonStrokingColor(Color.WHITE);
            dessinerTexteCellule(cs, PDType1Font.HELVETICA_BOLD, 9, MARGIN + 5, y - 14, "Nature de la depense");
            dessinerTexteCellule(cs, PDType1Font.HELVETICA_BOLD, 9, col2x + 5,   y - 14, "Montant (EUR)");
            y -= rowH;

            // Lignes de donnees
            if (details.isEmpty()) {
                cs.setNonStrokingColor(GRIS_CLAIR);
                cs.addRect(MARGIN, y - rowH, PAGE_WIDTH - 2 * MARGIN, rowH);
                cs.fill();
                cs.setNonStrokingColor(new Color(100, 100, 100));
                dessinerTexteCellule(cs, PDType1Font.HELVETICA, 10, MARGIN + 5, y - 14, "Aucune donnee disponible pour ce pole.");
                y -= rowH;
            } else {
                boolean alt = false;
                for (Map.Entry<String, Double> e : details.entrySet()) {
                    cs.setNonStrokingColor(alt ? GRIS_CLAIR : Color.WHITE);
                    cs.addRect(MARGIN, y - rowH, PAGE_WIDTH - 2 * MARGIN, rowH);
                    cs.fill();
                    cs.setNonStrokingColor(new Color(45, 45, 45));
                    dessinerTexteCellule(cs, PDType1Font.HELVETICA, 9, MARGIN + 5, y - 13, truncate(e.getKey(), 60));
                    dessinerTexteCellule(cs, PDType1Font.HELVETICA, 9, col2x + 5, y - 13, formatEur(e.getValue()));
                    dessinerBordLigne(cs, MARGIN, y - rowH, PAGE_WIDTH - 2 * MARGIN);
                    y -= rowH;
                    alt = !alt;
                }
            }

            // Ligne total depenses
            cs.setNonStrokingColor(BLEU_TOTAL);
            cs.addRect(MARGIN, y - rowH, PAGE_WIDTH - 2 * MARGIN, rowH);
            cs.fill();
            cs.setNonStrokingColor(Color.WHITE);
            dessinerTexteCellule(cs, PDType1Font.HELVETICA_BOLD, 9, MARGIN + 5, y - 14, "TOTAL DEPENSES REELLES");
            dessinerTexteCellule(cs, PDType1Font.HELVETICA_BOLD, 9, col2x + 5,   y - 14, formatEur(depenses));
            y -= rowH + 22;

            // --- Indicateurs financiers ---
            cs.setNonStrokingColor(new Color(40, 40, 40));
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("INDICATEURS FINANCIERS");
            cs.endText();
            y -= 22;

            dessinerLigneIndicateur(cs, y, "Total Recettes calculees", formatEur(recettes), new Color(40, 40, 40));
            y -= 22;
            dessinerLigneIndicateur(cs, y, "Taux de couverture des charges", String.format("%.1f %%", taux), couleurTaux);
            y -= 28;

            // Barre de progression
            float barLargeur = PAGE_WIDTH - 2 * MARGIN - 10;
            float remplissage = (float) Math.min(taux / 100.0, 1.0) * barLargeur;
            cs.setNonStrokingColor(new Color(220, 220, 220));
            cs.addRect(MARGIN, y - 14, barLargeur, 14);
            cs.fill();
            if (remplissage > 0) {
                cs.setNonStrokingColor(couleurTaux);
                cs.addRect(MARGIN, y - 14, remplissage, 14);
                cs.fill();
            }
            cs.setStrokingColor(new Color(180, 180, 180));
            cs.setLineWidth(0.5f);
            cs.addRect(MARGIN, y - 14, barLargeur, 14);
            cs.stroke();
            y -= 30;

            // Interpretation textuelle
            String interpretation;
            if (taux >= 80)      interpretation = "Taux satisfaisant : les recettes couvrent largement les charges du service.";
            else if (taux >= 50) interpretation = "Taux modere : une part significative des charges est financee par la collectivite.";
            else                 interpretation = "Taux faible : les charges sont majoritairement financees par la collectivite.";

            cs.setNonStrokingColor(new Color(90, 90, 90));
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(interpretation);
            cs.endText();

            dessinerPiedDePage(cs, doc.getNumberOfPages());
        }
    }

    // =========================================================
    // TABLEAU RECAPITULATIF GLOBAL (page 8)
    // =========================================================

    private void dessinerTableauRecap(PDDocument doc, Calculateur calc) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            // En-tete
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.addRect(0, PAGE_HEIGHT - 80, PAGE_WIDTH, 80);
            cs.fill();
            cs.setNonStrokingColor(Color.WHITE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 35);
            cs.showText("SYNTHESE GLOBALE - TOUS POLES");
            cs.endText();
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 60);
            cs.showText("Vue consolidee de l'ensemble des services municipaux");
            cs.endText();

            float y = PAGE_HEIGHT - 105;

            cs.setNonStrokingColor(new Color(40, 40, 40));
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("TABLEAU COMPARATIF DES 6 POLES");
            cs.endText();
            y -= 8;

            // Colonnes : Pole, Depenses, Recettes, Taux
            float[] cols = {165f, 115f, 115f, 100f};
            float rowH = 22f;

            // En-tete tableau
            cs.setNonStrokingColor(BLEU_ENTETE);
            cs.addRect(MARGIN, y - rowH, sumArray(cols), rowH);
            cs.fill();
            String[] headers = {"Pole", "Depenses (EUR)", "Recettes (EUR)", "Taux (%)"};
            float xh = MARGIN + 5;
            cs.setNonStrokingColor(Color.WHITE);
            for (int i = 0; i < headers.length; i++) {
                dessinerTexteCellule(cs, PDType1Font.HELVETICA_BOLD, 10, xh, y - 15, headers[i]);
                xh += cols[i];
            }
            y -= rowH;

            double totalDep = 0, totalRec = 0;
            boolean alt = false;

            for (Object[] poleConf : POLES_CONFIG) {
                String nomPole = (String) poleConf[0];
                double mult = (double) poleConf[1];
                double dep = calc.calculerTotalDepenses(nomPole);
                double rec = calc.calculerRecettesAnnuelles(nomPole, mult);
                double taux = (dep > 0) ? (rec / dep * 100) : 0;
                totalDep += dep;
                totalRec += rec;
                Color couleurTaux = couleurPourTaux(taux);

                cs.setNonStrokingColor(alt ? GRIS_CLAIR : Color.WHITE);
                cs.addRect(MARGIN, y - rowH, sumArray(cols), rowH);
                cs.fill();

                String[] vals = {
                    traduitNomPole(nomPole),
                    formatEur(dep),
                    formatEur(rec),
                    String.format("%.1f %%", taux)
                };
                float xv = MARGIN + 5;
                for (int i = 0; i < vals.length; i++) {
                    cs.setNonStrokingColor(i == 3 ? couleurTaux : new Color(45, 45, 45));
                    dessinerTexteCellule(cs,
                        i == 3 ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA,
                        10, xv, y - 15, vals[i]);
                    xv += cols[i];
                }

                dessinerBordLigne(cs, MARGIN, y - rowH, sumArray(cols));
                y -= rowH;
                alt = !alt;
            }

            // Ligne TOTAL GLOBAL
            double tauxGlobal = (totalDep > 0) ? (totalRec / totalDep * 100) : 0;
            cs.setNonStrokingColor(BLEU_TOTAL);
            cs.addRect(MARGIN, y - rowH, sumArray(cols), rowH);
            cs.fill();
            cs.setNonStrokingColor(Color.WHITE);
            String[] totaux = {"TOTAL GLOBAL", formatEur(totalDep), formatEur(totalRec),
                String.format("%.1f %%", tauxGlobal)};
            float xt = MARGIN + 5;
            for (int i = 0; i < totaux.length; i++) {
                dessinerTexteCellule(cs, PDType1Font.HELVETICA_BOLD, 10, xt, y - 15, totaux[i]);
                xt += cols[i];
            }
            y -= rowH + 30;

            // --- Legende ---
            cs.setNonStrokingColor(new Color(40, 40, 40));
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("LEGENDE DU TAUX DE COUVERTURE");
            cs.endText();
            y -= 20;

            String[][] legendeItems = {
                {"≥ 80 %", "Taux satisfaisant : les recettes couvrent largement les charges"},
                {"≥ 50 %", "Taux modere : part significative financee par la collectivite"},
                {"< 50 %", "Taux faible : charges majoritairement financees par la collectivite"}
            };
            Color[] legendeColors = {VERT, ORANGE, ROUGE};
            for (int i = 0; i < legendeItems.length; i++) {
                cs.setNonStrokingColor(legendeColors[i]);
                cs.addRect(MARGIN, y - 10, 12, 12);
                cs.fill();
                cs.setNonStrokingColor(new Color(50, 50, 50));
                dessinerTexteCellule(cs, PDType1Font.HELVETICA_BOLD, 10, MARGIN + 18, y, legendeItems[i][0]);
                dessinerTexteCellule(cs, PDType1Font.HELVETICA, 10, MARGIN + 70, y, legendeItems[i][1]);
                y -= 18;
            }

            dessinerPiedDePage(cs, doc.getNumberOfPages());
        }
    }

    // =========================================================
    // GRILLE TARIFAIRE — PAGE 1 : Services enfants / periscolaire
    // =========================================================

    private void dessinerPageGrille1(PDDocument doc, List<Tarif> grille) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            dessinerEnteteGrille(cs,
                "GRILLE TARIFAIRE 2025 — SERVICES ENFANTS (1/2)",
                "Restauration scolaire, Accueil de loisirs, Periscolaire");

            float y = PAGE_HEIGHT - 110;

            String[] headers = {"Tranche", "QF min", "QF max",
                "Repas", "Loisirs/j", "Loisirs/1/2j", "Peri M+S", "Peri M/S"};
            float[] colWidths = {52f, 52f, 52f, 55f, 60f, 65f, 60f, 55f};
            // null = colonne speciale (tranche, QF)
            String[] serviceKeys = {null, null, null,
                DonneesTarifs.REPAS,
                DonneesTarifs.ACCUEIL_JOURNEE,
                DonneesTarifs.ACCUEIL_DEMI_REPAS,
                DonneesTarifs.PERISCOLAIRE_MATIN_SOIR,
                DonneesTarifs.PERISCOLAIRE_MATIN_OU_SOIR};

            dessinerTableauTarifs(cs, y, headers, colWidths, serviceKeys, grille);
            dessinerPiedDePage(cs, doc.getNumberOfPages());
        }
    }

    // =========================================================
    // GRILLE TARIFAIRE — PAGE 2 : Etudes & Ados
    // =========================================================

    private void dessinerPageGrille2(PDDocument doc, List<Tarif> grille) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            dessinerEnteteGrille(cs,
                "GRILLE TARIFAIRE 2025 — ETUDES & ESPACE ADOS (2/2)",
                "Etudes surveillees, Espace Ados vacances et sorties");

            float y = PAGE_HEIGHT - 110;

            String[] headers = {"Tranche", "Etudes/mois", "Etudes/1/2",
                "Ados vac/j+R", "Ados vac/j", "Ados vac/1/2", "Ados sort/j"};
            float[] colWidths = {55f, 78f, 68f, 78f, 68f, 73f, 68f};
            String[] serviceKeys = {null,
                DonneesTarifs.ETUDES_FORFAIT_MENSUEL,
                DonneesTarifs.ETUDES_DEMI_FORFAIT,
                DonneesTarifs.ADOS_VAC_JOURNEE_REPAS,
                DonneesTarifs.ADOS_VAC_JOURNEE_SANS,
                DonneesTarifs.ADOS_VAC_DEMI_REPAS,
                DonneesTarifs.ADOS_SORTIE_JOURNEE};

            float y2 = dessinerTableauTarifs(cs, y, headers, colWidths, serviceKeys, grille);

            // Note de bas de tableau
            y2 -= 12;
            cs.setNonStrokingColor(new Color(100, 100, 100));
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 8);
            cs.newLineAtOffset(MARGIN, y2);
            cs.showText("* Le QF (Quotient Familial) est calcule sur la base du revenu fiscal de reference annuel de la famille.");
            cs.endText();
            y2 -= 12;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 8);
            cs.newLineAtOffset(MARGIN, y2);
            cs.showText("* Tranche EXT = familles residant hors commune de Crosne (tarif exterieur).");
            cs.endText();
            y2 -= 12;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 8);
            cs.newLineAtOffset(MARGIN, y2);
            cs.showText("* Tranche A = familles de Crosne avec QF ≥ 18 000 EUR.");
            cs.endText();

            dessinerPiedDePage(cs, doc.getNumberOfPages());
        }
    }

    // =========================================================
    // METHODE GENERIQUE — DESSIN D'UN TABLEAU DE TARIFS
    // =========================================================

    private float dessinerTableauTarifs(PDPageContentStream cs, float y,
            String[] headers, float[] colWidths, String[] serviceKeys,
            List<Tarif> grille) throws IOException {

        float rowH = 22f;

        // En-tete
        cs.setNonStrokingColor(BLEU_ENTETE);
        cs.addRect(MARGIN, y - rowH, sumArray(colWidths), rowH);
        cs.fill();
        float xh = MARGIN + 4;
        cs.setNonStrokingColor(Color.WHITE);
        for (int i = 0; i < headers.length; i++) {
            dessinerTexteCellule(cs, PDType1Font.HELVETICA_BOLD, 8, xh, y - 14, headers[i]);
            xh += colWidths[i];
        }
        y -= rowH;

        // Lignes de tarifs
        boolean alt = false;
        for (Tarif t : grille) {
            cs.setNonStrokingColor(alt ? GRIS_CLAIR : Color.WHITE);
            cs.addRect(MARGIN, y - rowH, sumArray(colWidths), rowH);
            cs.fill();

            float xv = MARGIN + 4;
            for (int i = 0; i < serviceKeys.length; i++) {
                String valeur;
                if (serviceKeys[i] == null) {
                    // Colonne speciale : tranche ou QF
                    int nbNulls = 0;
                    for (int k = 0; k <= i; k++) if (serviceKeys[k] == null) nbNulls++;
                    switch (nbNulls) {
                        case 1: valeur = t.getTranche(); break;
                        case 2: valeur = formatQF(t.getQfMin()); break;
                        default: valeur = (t.getQfMax() == Double.MAX_VALUE) ? "  -  " : formatQF(t.getQfMax()); break;
                    }
                } else {
                    valeur = String.format("%.2f EUR", t.getPrix(serviceKeys[i]));
                }

                boolean estTranche = (serviceKeys[i] == null && i == 0);
                cs.setNonStrokingColor(estTranche ? BLEU_MAIRIE : new Color(45, 45, 45));
                dessinerTexteCellule(cs,
                    estTranche ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA,
                    9, xv, y - 14, valeur);
                xv += colWidths[i];
            }

            dessinerBordLigne(cs, MARGIN, y - rowH, sumArray(colWidths));
            y -= rowH;
            alt = !alt;
        }

        return y;
    }

    // =========================================================
    // UTILITAIRES DE DESSIN
    // =========================================================

    private void dessinerEnteteGrille(PDPageContentStream cs, String titre, String sousTitre)
            throws IOException {
        cs.setNonStrokingColor(BLEU_MAIRIE);
        cs.addRect(0, PAGE_HEIGHT - 80, PAGE_WIDTH, 80);
        cs.fill();
        cs.setNonStrokingColor(Color.WHITE);
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
        cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 33);
        cs.showText(titre);
        cs.endText();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 57);
        cs.showText(sousTitre);
        cs.endText();
    }

    private void dessinerLigneIndicateur(PDPageContentStream cs, float y,
            String label, String valeur, Color couleurValeur) throws IOException {
        cs.setNonStrokingColor(new Color(55, 55, 55));
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(label + "  :");
        cs.endText();
        cs.setNonStrokingColor(couleurValeur);
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(MARGIN + 240, y);
        cs.showText(valeur);
        cs.endText();
    }

    private void dessinerTexteCellule(PDPageContentStream cs, PDType1Font font,
            float size, float x, float y, String texte) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(texte);
        cs.endText();
    }

    private void dessinerBordLigne(PDPageContentStream cs, float x, float y, float largeur)
            throws IOException {
        cs.setStrokingColor(new Color(205, 205, 205));
        cs.setLineWidth(0.4f);
        cs.moveTo(x, y);
        cs.lineTo(x + largeur, y);
        cs.stroke();
    }

    private void dessinerPiedDePage(PDPageContentStream cs, int pageNum) throws IOException {
        cs.setStrokingColor(new Color(200, 200, 200));
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, 42);
        cs.lineTo(PAGE_WIDTH - MARGIN, 42);
        cs.stroke();
        cs.setNonStrokingColor(new Color(160, 160, 160));
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 8);
        cs.newLineAtOffset(MARGIN, 28);
        cs.showText("Ville de Crosne - Rapport Financier Municipal 2025 - Document genere automatiquement");
        cs.endText();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 8);
        cs.newLineAtOffset(PAGE_WIDTH - MARGIN - 35, 28);
        cs.showText("Page " + pageNum);
        cs.endText();
    }

    // =========================================================
    // UTILITAIRES METIER
    // =========================================================

    private Color couleurPourTaux(double taux) {
        if (taux >= 80) return VERT;
        if (taux >= 50) return ORANGE;
        return ROUGE;
    }

    private String traduitNomPole(String pole) {
        switch (pole) {
            case "Restauration":        return "Restauration Scolaire";
            case "Accueil de Loisirs":  return "Accueil de Loisirs";
            case "Espace Ados":         return "Espace Ados";
            case "Sejours":             return "Sejours";
            case "Etudes surveillees":  return "Etudes Surveillees";
            case "Accueil periscolaire":return "Accueil Periscolaire";
            default:                    return pole;
        }
    }

    private float sumArray(float[] arr) {
        float s = 0; for (float v : arr) s += v; return s;
    }

    private String formatEur(double montant) {
        if (montant == 0) return "0,00 EUR";
        return String.format("%,.2f EUR", montant).replace(",", " ");
    }

    private String formatQF(double qf) {
        return String.format("%,.0f", qf).replace(",", " ");
    }

    private String truncate(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max - 2) + ".." : (s == null ? "" : s);
    }
}
