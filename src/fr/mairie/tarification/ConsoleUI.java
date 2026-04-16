package fr.mairie.tarification;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Utilitaires pour l'interface utilisateur en console (ConsoleUI).
 * 
 * Cette classe gere tout l'affichage visuel de l'application :
 * - Les menus interactifs.
 * - Les tableaux de bord financiers (Dashboards).
 * - Les outils de consultation des tarifs individuels.
 * 
 * Elle utilise des codes ANSI pour ameliorer la lisibilite dans un terminal moderne.
 * 
 * @author Séri-khane YOLOU
 * @version 1.2
 */
public class ConsoleUI {
    
    /** Code ANSI pour reinitialiser le style. */
    public static final String RESET = "\u001B[0;1m";
    
    /** Code ANSI pour la couleur Cyan. */
    public static final String CYAN  = "\u001B[36m";
    
    /** Code ANSI pour le texte en Gras. */
    public static final String BOLD  = "\033[0;1m";

    /**
     * Nettoie l'ecran de la console de maniere compatible Windows et Linux.
     */
    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Silence en cas d'erreur de console
        }
    }

    /**
     * Affiche le logo et l'en-tete de l'application.
     */
    public static void printLogo() {
        System.out.println("\n" + CYAN + BOLD + "   TARIFICATION MUNICIPALE - VERSION 2025" + RESET);
        System.out.println("   --------------------------------------");
    }

    /**
     * Affiche un en-tete de section stylise.
     * 
     * @param title Le titre de la section.
     */
    public static void printHeader(String title) {
        System.out.println("\n " + BOLD + title.toUpperCase() + RESET);
        System.out.println(" " + repeat("=", title.length()));
    }

    /**
     * Affiche un séparateur horizontal simple.
     */
    public static void printSeparator() {
        System.out.println("   " + repeat("-", 50));
    }

    /**
     * Affiche une ligne de donnees alignee avec libelle et valeur.
     * 
     * @param label Le libelle de la donnee.
     * @param value La valeur a afficher.
     */
    public static void printLine(String label, String value) {
        System.out.printf("   %-32s : %s%n", label, value);
    }

    /**
     * Dashboard générique utilisé pour tous les pôles municipaux.
     * 
     * Cette méthode centralise l'affichage :
     * 1. Extrait les dépenses détaillées par nature depuis le calculateur.
     * 2. Calcule les recettes théoriques annuelles.
     * 3. Déduit le taux de couverture du service.
     * 
     * @param calc L'instance du calculateur.
     * @param scanner Le scanner pour attendre l'utilisateur.
     * @param pole Le nom du pôle (ex: "Espace Ados").
     * @param multiplicateur Le volume annuel (140j, 10m, etc.).
     */
    public static void afficherDashboardPole(Calculateur calc, Scanner scanner, String pole, double multiplicateur) {
        printHeader("TABLEAU DE BORD : " + pole.toUpperCase());
        
        double depensesTotales = calc.calculerTotalDepenses(pole);
        double recettesTotales = calc.calculerRecettesAnnuelles(pole, multiplicateur);
        Map<String, Double> details = calc.getDepensesDetaillees(pole);

        System.out.println("\n   DETAIL DES CHARGES REELLES (PAR NATURE) :");
        
        if (details.isEmpty()) {
            System.out.println("   Aucune donnee de depense trouvee.");
        } else {
            for (Map.Entry<String, Double> entry : details.entrySet()) {
                printLine(entry.getKey(), String.format("%.2f EUR", entry.getValue()));
            }
        }

        printSeparator();
        printLine("TOTAL DEPENSES REELLES", String.format("%.2f EUR", depensesTotales));
        printLine("TOTAL RECETTES CALCULEES", String.format("%.2f EUR", recettesTotales));
        
        // Calcul du taux de couverture (Recettes / Depenses)
        double taux = 0;
        if (depensesTotales > 0) {
            taux = (recettesTotales / depensesTotales * 100);
        }
        printLine("TAUX DE COUVERTURE", String.format("%.2f %%", taux));
        printSeparator();

        // Information sur le parametre de calcul
        if (multiplicateur > 1) {
            System.out.println("\n   [PARAMETRE] Recettes calculees sur " + (int)multiplicateur + " unites annuelles.");
        } else {
            System.out.println("\n   [PARAMETRE] Recettes calculees sur base forfaitaire.");
        }
        
        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        scanner.nextLine();
    }

    /** Dashboard Scolaire (x140 jours). @param calc Calculateur, @param sc Scanner. */
    public static void afficherDashboardScolaire(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Restauration", 140);
    }

    /** Dashboard Loisirs (x1 forage forfaitaire). @param calc Calculateur, @param sc Scanner. */
    public static void afficherDashboardLoisirs(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Accueil de Loisirs", 1);
    }

    /** Dashboard Ados (x1 forage forfaitaire). @param calc Calculateur, @param sc Scanner. */
    public static void afficherDashboardAdos(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Espace Ados", 1);
    }

    /** Dashboard Sejours (x1 forage forfaitaire). @param calc Calculateur, @param sc Scanner. */
    public static void afficherDashboardSejours(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Sejours", 1);
    }

    /** Dashboard Etudes (x10 mois). @param calc Calculateur, @param sc Scanner. */
    public static void afficherDashboardEtudes(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Etudes surveillees", 10);
    }

    /** Dashboard Periscolaire (x10 mois). @param calc Calculateur, @param sc Scanner. */
    public static void afficherDashboardPeriscolaire(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Accueil periscolaire", 10);
    }

    /**
     * Gère la consultation individuelle du tarif selon le Quotient Familial saisi.
     * Permet desormais de choisir entre la grille 2025 ou un fichier externe.
     * 
     * @param service Le service de tarification.
     * @param scanner Le scanner pour les entrees utilisateur.
     */
    public static void consulterTarif(TarificationService service, Scanner scanner) {
        printHeader("Consultation des tarifs (Simulation)");
        
        System.out.println("\n   Quelle grille voulez-vous utiliser ?");
        System.out.println("   [1] Grille 2025 par defaut (interne)");
        System.out.println("   [2] Charger une autre grille (Excel externe)");
        System.out.print("\n   Votre choix : ");
        
        List<Tarif> grille;
        String choixGrille = scanner.nextLine();
        
        if ("2".equals(choixGrille)) {
            System.out.print("   Entrez le chemin du fichier (ex: Donnees/Grille-tarifaire-2024-(1).xlsx) : ");
            String chemin = scanner.nextLine();
            grille = DonneesTarifs.chargerGrilleStandard(chemin);
            if (grille.isEmpty()) {
                System.out.println("   " + ROUGE_TEXT + "ERREUR : Impossible de charger la grille externe." + RESET);
                return;
            }
        } else {
            grille = DonneesTarifs.chargerTarifsReference();
        }

        System.out.print("\n   Entrez le Quotient Familial (QF) : ");
        String qfStr = scanner.nextLine();

        try {
            double qf = Double.parseDouble(qfStr);
            Tarif t = service.trouverTarif(qf, grille);

            if (t != null) {
                System.out.println("\n   " + VERT_TEXT + "Tranche detectee : " + t.getTranche() + RESET);
                System.out.println("   " + repeat("-", 50));
                
                // Restauration
                System.out.printf("   [RESTAURATION]   Repas scolaire           : %.2f euros%n", t.getPrix(DonneesTarifs.REPAS));
                
                // Loisirs
                System.out.printf("   [COL / LOISIRS]  Journee complete         : %.2f euros%n", t.getPrix(DonneesTarifs.ACCUEIL_JOURNEE));
                System.out.printf("   [COL / LOISIRS]  1/2 Journee (+ repas)    : %.2f euros%n", t.getPrix(DonneesTarifs.ACCUEIL_DEMI_REPAS));
                
                // Periscolaire
                System.out.printf("   [PERISCOLAIRE]   Matin ET Soir            : %.2f euros%n", t.getPrix(DonneesTarifs.PERISCOLAIRE_MATIN_SOIR));
                System.out.printf("   [PERISCOLAIRE]   Matin OU Soir            : %.2f euros%n", t.getPrix(DonneesTarifs.PERISCOLAIRE_MATIN_OU_SOIR));
                
                // Etudes
                System.out.printf("   [ETUDES]         Forfait mensuel          : %.2f euros%n", t.getPrix(DonneesTarifs.ETUDES_FORFAIT_MENSUEL));
                System.out.printf("   [ETUDES]         1/2 Forfait              : %.2f euros%n", t.getPrix(DonneesTarifs.ETUDES_DEMI_FORFAIT));
                System.out.printf("   [POST ETUDES]    Sans gouter              : %.2f euros%n", t.getPrix(DonneesTarifs.TARIF_POST_ETUDES));
                
                System.out.println("   " + repeat("-", 50));
            } else {
                System.out.println("   " + ROUGE_TEXT + "Aucun tarif trouve pour ce QF." + RESET);
            }

        } catch (NumberFormatException e) {
            System.out.println("   Erreur : Entrez un nombre valide.");
        } catch (Exception e) {
            System.out.println("   Erreur : " + e.getMessage());
        }

        System.out.println("\n   Appuyez sur Entree.");
        scanner.nextLine();
    }

    /**
     * Gère le processus d'exportation du rapport financier au format PDF.
     * 
     * Cette méthode instancie le service d'exportation, génère le fichier
     * et informe l'utilisateur du succès ou de l'échec de l'opération.
     * 
     * @param calc    L'instance du calculateur pour les données financières.
     * @param grille  La grille tarifaire de référence.
     * @param scanner Le scanner pour les entrées utilisateur.
     */
    public static void gererExportPDF(Calculateur calc, List<Tarif> grille, Scanner scanner) {
        printHeader("Génération du Rapport PDF");
        System.out.println("\n   Veuillez patienter pendant la génération du document...");

        PdfExportService pdfService = new PdfExportService();
        try {
            String cheminAbsolu = pdfService.genererRapport(calc, grille);
            System.out.println("\n   " + VERT_TEXT + "SUCCÈS :" + RESET);
            System.out.println("   Le rapport a été généré avec succès.");
            System.out.println("   Emplacement : " + cheminAbsolu);
        } catch (Exception e) {
            System.out.println("\n   " + ROUGE_TEXT + "ERREUR :" + RESET);
            System.out.println("   Impossible de générer le rapport PDF.");
            System.out.println("   Détail : " + e.getMessage());
            LogService.error("Erreur lors de l'export PDF", e);
        }

        System.out.println("\n   Appuyez sur Entrée pour continuer.");
        scanner.nextLine();
    }

    /** Code ANSI pour le texte Vert. */
    private static final String VERT_TEXT = "\u001B[32m";
    /** Code ANSI pour le texte Rouge. */
    private static final String ROUGE_TEXT = "\u001B[31m";

    /**
     * Utilitaire pour repeter une chaine plusieurs fois.
     * 
     * @param s Chaine a repeter.
     * @param n Nombre de fois.
     * @return Resultat concatene.
     */
    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
