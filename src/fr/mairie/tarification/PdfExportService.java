package fr.mairie.tarification;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service d'export PDF pour le rapport financier municipal.
 * 
 * Cette classe permet de generer un document PDF professionnel et structure :
 * - Page de garde avec les coordonnees de la mairie de Crosne.
 * - Tableaux de bord detaillés pour chaque pole municipal (6 poles).
 * - Indicateurs de performance (taux de couverture) avec codes couleurs.
 * - Grille tarifaire exhaustive par Quotient Familial (QF).
 * 
 * Logiciel requis : Apache PDFBox 2.x
 * 
 * @author Séri-khane YOLOU
 * @version 1.2
 */
public class PdfExportService {

    /** Largeur d'une page A4 en points PDF. */
    private static final float PAGE_WIDTH  = PDRectangle.A4.getWidth();
    
    /** Hauteur d'une page A4 en points PDF. */
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    
    /** Marge standard pour le document. */
    private static final float MARGIN = 50.0f;

    // --- Palette de couleurs institutionnelle ---
    private static final Color BLEU_MAIRIE = new Color(30, 80, 150);
    private static final Color BLEU_ENTETE = new Color(60, 90, 140);
    private static final Color BLEU_TOTAL  = new Color(40, 70, 130);
    private static final Color GRIS_CLAIR  = new Color(245, 245, 245);
    private static final Color VERT        = new Color(0, 150, 80);
    private static final Color ORANGE      = new Color(230, 120, 0);
    private static final Color ROUGE       = new Color(200, 40, 40);

    /** Configuration des poles avec leurs noms Excel et multiplicateurs annuels. */
    private static final Object[][] POLES_CONFIG = {
        {"Restauration", 140.0},
        {"Accueil de Loisirs", 1.0},
        {"Espace Ados", 1.0},
        {"Sejours", 1.0},
        {"Etudes surveillees", 10.0},
        {"Accueil periscolaire", 10.0}
    };

    /**
     * Genere le rapport PDF complet et retourne le chemin absolu du fichier cree.
     *
     * @param calc   L'instance du calculateur contenant les donnees financieres.
     * @param grille La grille tarifaire de reference (DonneesTarifs).
     * @return Le chemin absolu vers le fichier PDF de sortie.
     * @throws IOException En cas d'erreur de creation ou d'ecriture du fichier.
     */
    public String genererRapport(Calculateur calc, List<Tarif> grille) throws IOException {
        File dossier = new File("rapports");
        if (!dossier.exists()) {
            dossier.mkdirs();
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = LocalDate.now().format(dtf);
        String chemin = "rapports/rapport_financier_" + date + ".pdf";

        try (PDDocument doc = new PDDocument()) {
            // 1. Page de garde
            this.dessinerPageGarde(doc, date);

            // 2. Sections de poles (6 pages)
            for (Object[] poleConf : POLES_CONFIG) {
                String nomPole = (String) poleConf[0];
                double multi = (double) poleConf[1];
                this.dessinerSectionPole(doc, nomPole, multi, calc);
            }

            // 3. Synthese et Grilles
            this.dessinerTableauRecap(doc, calc);
            this.dessinerPageGrille1(doc, grille);
            this.dessinerPageGrille2(doc, grille);

            doc.save(chemin);
        }

        return new File(chemin).getAbsolutePath();
    }

    /**
     * Cree la page de garde du rapport.
     * 
     * @param doc Le document PDF en cours de construction.
     * @param dateStr La date a afficher sur la garde.
     */
    private void dessinerPageGarde(PDDocument doc, String dateStr) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            // Ajout du logo en haut à gauche sur fond blanc (si le fichier existe)
            float logoHeight = 0;
            try {
                File logoFile = new File("Crosne-LOGO.png");
                if (logoFile.exists()) {
                    PDImageXObject logo = PDImageXObject.createFromFile("Crosne-LOGO.png", doc);
                    float scale = 80f / logo.getWidth();
                    logoHeight = logo.getHeight() * scale;
                    // Positionnement en haut à gauche
                    cs.drawImage(logo, MARGIN, PAGE_HEIGHT - MARGIN - logoHeight, 80, logoHeight);
                }
            } catch (Exception e) {
                LogService.error("Logo introuvable ou illisible, passage outre.", e);
            }

            // Bandeau bleu décalé vers le bas pour laisser le logo sur fond blanc
            float bandeauY = PAGE_HEIGHT - 210 - (logoHeight > 0 ? 50 : 0);
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.addRect(0, bandeauY, PAGE_WIDTH, 160);
            cs.fill();

            // Titres et Identification (décalés dans le bandeau bleu)
            cs.setNonStrokingColor(Color.WHITE);
            
            float titreY = bandeauY + 110;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 26);
            cs.newLineAtOffset(MARGIN, titreY);
            cs.showText("VILLE DE CROSNE");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 13);
            cs.newLineAtOffset(MARGIN, titreY - 25);
            cs.showText("Direction des Services aux Familles");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(MARGIN, titreY - 65);
            cs.showText("Rapport Financier - Services Municipaux 2025");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(MARGIN, titreY - 95);
            cs.showText("Généré le : " + dateStr);
            cs.endText();

            // Pied de page de garde (Section explicative)
            this.dessinerContenuGarde(cs);
            this.dessinerPiedDePage(cs, 1);
        }
    }

    /**
     * Dessine le texte informatif sur la page de garde.
     */
    private void dessinerContenuGarde(PDPageContentStream cs) throws IOException {
        // On descend y de 300 points (au lieu de 270) pour laisser plus d'espace avec le bandeau bleu
        float y = PAGE_HEIGHT - 300;
        cs.setNonStrokingColor(BLEU_MAIRIE);
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("1. Tableaux de bord financiers (6 poles)");
        cs.endText();
        
        y -= 22.0f;
        String[] detailsPoles = {
            "  Restauration scolaire         (multiplicateur : 140 jours/an)",
            "  Accueil de loisirs            (forfait annuel)",
            "  Espace Ados                   (forfait annuel)",
            "  Sejours                       (forfait par sejour)",
            "  Etudes surveillees            (x10 mois/an)",
            "  Accueil periscolaire          (x10 mois/an)"
        };
        
        for (String ligne : detailsPoles) {
            cs.setNonStrokingColor(new Color(55, 55, 55));
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(ligne);
            cs.endText();
            y -= 18.0f;
        }
    }

    /**
     * Dessine une page de detail pour un pole specifique.
     */
    private void dessinerSectionPole(PDDocument doc, String pole, double multi, Calculateur calc) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        double depenses = calc.calculerTotalDepenses(pole);
        double recettes = calc.calculerRecettesAnnuelles(pole, multi);
        Map<String, Double> details = calc.getDepensesDetaillees(pole);
        
        double taux = 0.0;
        if (depenses > 0) {
            taux = (recettes / depenses * 100.0);
        }
        
        Color couleurTaux = this.couleurPourTaux(taux);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            // En-tete de page
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.addRect(0, PAGE_HEIGHT - 80, PAGE_WIDTH, 80);
            cs.fill();

            cs.setNonStrokingColor(Color.WHITE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 15);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 33);
            cs.showText("TABLEAU DE BORD : " + pole.toUpperCase());
            cs.endText();

            this.dessinerTableauDepenses(cs, details, depenses);
            this.dessinerIndicateurs(cs, recettes, taux, couleurTaux);
            this.dessinerPiedDePage(cs, doc.getNumberOfPages());
        }
    }

    /**
     * Dessine les indicateurs financiers et la barre de progression.
     */
    private void dessinerIndicateurs(PDPageContentStream cs, double recettes, double taux, Color couleurTaux) throws IOException {
        float y = 250.0f; // Position arbitraire basse pour les indicateurs
        
        this.dessinerLigneIndicateur(cs, y, "Total Recettes calculees", this.formatEur(recettes), BLEU_MAIRIE);
        y -= 25.0f;
        this.dessinerLigneIndicateur(cs, y, "Taux de couverture des charges", String.format("%.1f %%", taux), couleurTaux);
        
        // Barre de progression
        y -= 30.0f;
        float barWidth = PAGE_WIDTH - (2 * MARGIN);
        float fillWidth = (float)(taux / 100.0) * barWidth;
        if (fillWidth > barWidth) {
            fillWidth = barWidth;
        }

        cs.setNonStrokingColor(new Color(230, 230, 230));
        cs.addRect(MARGIN, y, barWidth, 15);
        cs.fill();
        
        if (fillWidth > 0) {
            cs.setNonStrokingColor(couleurTaux);
            cs.addRect(MARGIN, y, fillWidth, 15);
            cs.fill();
        }
    }

    /**
     * Dessine le tableau des depenses.
     */
    private void dessinerTableauDepenses(PDPageContentStream cs, Map<String, Double> details, double total) throws IOException {
        float y = PAGE_HEIGHT - 120.0f;
        float rowHeight = 20.0f;
        float colNatureWidth = 350.0f;
        
        // Entete
        cs.setNonStrokingColor(BLEU_ENTETE);
        cs.addRect(MARGIN, y, PAGE_WIDTH - (2 * MARGIN), rowHeight);
        cs.fill();
        
        cs.setNonStrokingColor(Color.WHITE);
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
        cs.newLineAtOffset(MARGIN + 5, y + 5);
        cs.showText("Nature de la depense");
        cs.endText();

        y -= rowHeight;
        
        boolean alternate = false;
        for (Map.Entry<String, Double> entry : details.entrySet()) {
            if (alternate) {
                cs.setNonStrokingColor(GRIS_CLAIR);
            } else {
                cs.setNonStrokingColor(Color.WHITE);
            }
            cs.addRect(MARGIN, y, PAGE_WIDTH - (2 * MARGIN), rowHeight);
            cs.fill();
            
            cs.setNonStrokingColor(Color.BLACK);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 8);
            cs.newLineAtOffset(MARGIN + 5, y + 5);
            cs.showText(this.tronquerTexte(entry.getKey(), 80));
            cs.endText();
            
            cs.beginText();
            cs.newLineAtOffset(MARGIN + colNatureWidth + 40, y + 5);
            cs.showText(this.formatEur(entry.getValue()));
            cs.endText();

            y -= rowHeight;
            alternate = !alternate;
            
            if (y < 100) { break; } // Limite simple pour une page
        }
        
        // Ligne Total
        cs.setNonStrokingColor(BLEU_TOTAL);
        cs.addRect(MARGIN, y, PAGE_WIDTH - (2 * MARGIN), rowHeight);
        cs.fill();
        cs.setNonStrokingColor(Color.WHITE);
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
        cs.newLineAtOffset(MARGIN + 5, y + 5);
        cs.showText("TOTAL DES CHARGES DIRECTES");
        cs.endText();
        cs.beginText();
        cs.newLineAtOffset(MARGIN + colNatureWidth + 40, y + 5);
        cs.showText(this.formatEur(total));
        cs.endText();
    }

    /** Dashboard recapitulatif (Synthese). */
    private void dessinerTableauRecap(PDDocument doc, Calculateur calc) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.setNonStrokingColor(BLEU_MAIRIE);
            cs.addRect(0, PAGE_HEIGHT - 80, PAGE_WIDTH, 80);
            cs.fill();
            cs.setNonStrokingColor(Color.WHITE);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 45);
            cs.showText("SYNTHESE FINANCIERE GLOBALE 2025");
            cs.endText();
            this.dessinerPiedDePage(cs, doc.getNumberOfPages());
        }
    }

    /** Grille tarifaire page 1. */
    private void dessinerPageGrille1(PDDocument doc, List<Tarif> grille) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 50);
            cs.showText("Grille Tarifaire (1/2)");
            cs.endText();
            this.dessinerPiedDePage(cs, doc.getNumberOfPages());
        }
    }

    /** Grille tarifaire page 2. */
    private void dessinerPageGrille2(PDDocument doc, List<Tarif> grille) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 50);
            cs.showText("Grille Tarifaire (2/2)");
            cs.endText();
            this.dessinerPiedDePage(cs, doc.getNumberOfPages());
        }
    }

    // --- Methodes Utilitaires de Formatage et Dessin ---

    private void dessinerLigneIndicateur(PDPageContentStream cs, float y, String label, String value, Color color) throws IOException {
        cs.setNonStrokingColor(Color.BLACK);
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(label + " :");
        cs.endText();
        
        cs.setNonStrokingColor(color);
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(MARGIN + 250, y);
        cs.showText(value);
        cs.endText();
    }

    private void dessinerPiedDePage(PDPageContentStream cs, int num) throws IOException {
        cs.setNonStrokingColor(new Color(150, 150, 150));
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 8);
        cs.newLineAtOffset(PAGE_WIDTH / 2 - 20, 30);
        cs.showText("Page " + num);
        cs.endText();
    }

    private Color couleurPourTaux(double taux) {
        if (taux >= 80) {
            return VERT;
        } else if (taux >= 50) {
            return ORANGE;
        } else {
            return ROUGE;
        }
    }

    private String formatEur(double solde) {
        return String.format("%.2f EUR", solde);
    }

    private String tronquerTexte(String texte, int max) {
        if (texte == null) {
            return "";
        }
        if (texte.length() <= max) {
            return texte;
        } else {
            return texte.substring(0, max - 3) + "...";
        }
    }
}
