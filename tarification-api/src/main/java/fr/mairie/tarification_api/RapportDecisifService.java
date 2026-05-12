package fr.mairie.tarification_api;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service de génération du Grand Rapport Décisionnel (PDF).
 * Version modernisée pour l'agent municipal.
 */
@Service
public class RapportDecisifService {

    private static final float PAGE_WIDTH  = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 50.0f;

    private static final Color BLEU_MAIRIE = new Color(30, 80, 150);
    private static final Color BLEU_ENTETE = new Color(60, 90, 140);
    private static final Color GRIS_CLAIR  = new Color(245, 245, 245);
    private static final Color VERT        = new Color(0, 150, 80);
    private static final Color ROUGE       = new Color(200, 40, 40);

    public byte[] genererRapportComplet(Calculateur.SyntheseGlobale synthese, Map<String, Double> recettesReelles, List<RapportSemestrielFluide> fluides) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            // 1. Page de Garde
            dessinerPageGarde(doc, dateStr);

            // 2. Synthèse Globale
            dessinerSynthese(doc, synthese, recettesReelles);

            // 3. Détail par Pôle (Top 3 majeurs)
            String[] poles = {"Restauration", "Accueil de Loisirs", "Accueil periscolaire"};
            for (String pole : poles) {
                if (synthese.depenses.containsKey(pole)) {
                    dessinerPagePole(doc, pole, synthese.depenses.get(pole), recettesReelles.getOrDefault(pole, 0.0));
                }
            }

            // 4. Audit des Fluides (La valeur ajoutée pour l'agent)
            dessinerPageFluides(doc, fluides);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private void dessinerPageGarde(PDDocument doc, String dateStr) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            // Bandeau
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.addRect(0, PAGE_HEIGHT - 250, PAGE_WIDTH, 180);
            cs.fill();

            cs.setNonStrokingColor(Color.WHITE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 28);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 160);
            showTextSafe(cs, "VILLE DE CROSNE");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 16);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 195);
            showTextSafe(cs, "RAPPORT DÉCISIONNEL : TARIFICATION 2025");
            cs.endText();

            cs.setNonStrokingColor(Color.BLACK);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(MARGIN, 400);
            showTextSafe(cs, "Document de synthèse budgétaire à l'usage des agents et élus.");
            cs.newLineAtOffset(0, -20);
            showTextSafe(cs, "Généré le : " + dateStr);
            cs.endText();
        }
    }

    private void dessinerSynthese(PDDocument doc, Calculateur.SyntheseGlobale synthese, Map<String, Double> recettesReelles) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 60);
            showTextSafe(cs, "SYNTHÈSE BUDGÉTAIRE CONSOLIDÉE");
            cs.endText();

            float y = PAGE_HEIGHT - 120;
            // Entêtes tableau
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            showTextSafe(cs, "PÔLE");
            cs.newLineAtOffset(180, 0);
            showTextSafe(cs, "DÉPENSES");
            cs.newLineAtOffset(100, 0);
            showTextSafe(cs, "RECETTES RÉELLES");
            cs.newLineAtOffset(120, 0);
            showTextSafe(cs, "COUVERTURE");
            cs.endText();

            y -= 15;
            cs.moveTo(MARGIN, y); cs.lineTo(PAGE_WIDTH - MARGIN, y); cs.stroke();
            y -= 25;

            for (Map.Entry<String, Double> entry : synthese.totauxDepenses.entrySet()) {
                String pole = entry.getKey();
                double dep = entry.getValue();
                double rec = recettesReelles.getOrDefault(pole, 0.0);
                double taux = dep > 0 ? (rec / dep) * 100 : 0;

                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                showTextSafe(cs, pole);
                cs.newLineAtOffset(180, 0);
                showTextSafe(cs, String.format("%,.0f €", dep));
                cs.newLineAtOffset(100, 0);
                showTextSafe(cs, String.format("%,.0f €", rec));
                cs.newLineAtOffset(120, 0);
                cs.setNonStrokingColor(taux > 30 ? VERT : ROUGE);
                showTextSafe(cs, String.format("%.1f %%", taux));
                cs.endText();
                cs.setNonStrokingColor(Color.BLACK);
                y -= 20;
            }
        }
    }

    private void dessinerPagePole(PDDocument doc, String nom, Map<String, Double> charges, double recettes) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 60);
            showTextSafe(cs, "DÉTAIL DU PÔLE : " + nom.toUpperCase());
            cs.endText();

            float y = PAGE_HEIGHT - 120;
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            showTextSafe(cs, "NATURE DE LA CHARGE");
            cs.newLineAtOffset(350, 0);
            showTextSafe(cs, "MONTANT");
            cs.endText();

            y -= 25;
            double totalDep = 0;
            for (Map.Entry<String, Double> c : charges.entrySet()) {
                cs.setFont(PDType1Font.HELVETICA, 9);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                showTextSafe(cs, c.getKey().length() > 60 ? c.getKey().substring(0, 57) + "..." : c.getKey());
                cs.newLineAtOffset(350, 0);
                showTextSafe(cs, String.format("%,.2f €", c.getValue()));
                cs.endText();
                totalDep += c.getValue();
                y -= 18;
                if (y < 100) break;
            }

            y -= 20;
            cs.setNonStrokingColor(BLEU_ENTETE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.newLineAtOffset(MARGIN, y);
            showTextSafe(cs, "RÉSULTAT DU PÔLE");
            cs.endText();
            y -= 20;
            cs.setNonStrokingColor(Color.BLACK);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(MARGIN, y);
            showTextSafe(cs, "Total Charges : " + String.format("%,.0f €", totalDep));
            cs.newLineAtOffset(200, 0);
            showTextSafe(cs, "Recettes Réelles : " + String.format("%,.0f €", recettes));
            cs.endText();
        }
    }

    private void dessinerPageFluides(PDDocument doc, List<RapportSemestrielFluide> fluides) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 60);
            showTextSafe(cs, "AUDIT ÉNERGÉTIQUE (EAU / GAZ / ÉLEC)");
            cs.endText();

            float y = PAGE_HEIGHT - 120;
            cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            showTextSafe(cs, "SITE / BÂTIMENT");
            cs.newLineAtOffset(200, 0);
            showTextSafe(cs, "FLUIDE");
            cs.newLineAtOffset(80, 0);
            showTextSafe(cs, "TOTAL ANNUEL");
            cs.newLineAtOffset(100, 0);
            showTextSafe(cs, "ÉVOL. S1/S2");
            cs.endText();

            y -= 20;
            for (int i = 0; i < Math.min(fluides.size(), 25); i++) {
                RapportSemestrielFluide f = fluides.get(i);
                cs.setFont(PDType1Font.HELVETICA, 8);
                cs.setNonStrokingColor(f.alerte() ? ROUGE : Color.BLACK);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                showTextSafe(cs, f.site().length() > 40 ? f.site().substring(0, 37) + "..." : f.site());
                cs.newLineAtOffset(200, 0);
                showTextSafe(cs, f.fluide());
                cs.newLineAtOffset(80, 0);
                showTextSafe(cs, String.format("%,.0f €", f.reel_Total()));
                cs.newLineAtOffset(100, 0);
                showTextSafe(cs, String.format("%+.1f %%", f.delta_S1_S2_Percent()));
                cs.endText();
                y -= 15;
            }
        }
    }

    /**
     * Ecrit du texte de maniere securisee en remplacant les caracteres non supportes 
     * par la police Helvetica standard (notamment l'espace insecable fin U+202F).
     */
    private void showTextSafe(PDPageContentStream cs, String text) throws java.io.IOException {
        if (text == null) return;
        // Remplacement des espaces insecables (U+00A0 et U+202F) par des espaces standards (U+0020)
        String cleanText = text.replace('\u00A0', ' ').replace('\u202F', ' ');
        cs.showText(cleanText);
    }
}
