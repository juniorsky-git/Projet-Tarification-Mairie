package fr.mairie.tarification;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Utilitaires pour l'interface utilisateur en console.
 * Gère les couleurs, les titres, les menus et les séparateurs.
 */
public class ConsoleUI {
    // --- Codes de couleurs ANSI pour un terminal moderne ---
    public static final String RESET = "\u001B[0;1m";
    public static final String CYAN  = "\u001B[36m";
    public static final String BOLD  = "\033[0;1m";

    /**
     * Nettoie l'écran de la console (compatible Windows/PowerShell).
     */
    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {}
    }

    /**
     * Affiche le titre principal de l'application.
     */
    public static void printLogo() {
        System.out.println("\n" + CYAN + BOLD + "   TARIFICATION MUNICIPALE - VERSION 2025" + RESET);
        System.out.println("   --------------------------------------");
    }

    /**
     * Affiche les options du menu principal.
     */
    public static void printMenu() {
        System.out.println("\n   [1] Tableau de bord financier (Michali)");
        System.out.println("   [2] Consulter un tarif (Saisie QF)");
        System.out.println("   [3] Afficher la grille de référence");
        System.out.println("   [4] Quitter");
        System.out.print("\n   Votre choix : ");
    }

    /**
     * Affiche un en-tête de section stylisé.
     * @param title Le texte du titre à afficher.
     */
    public static void printHeader(String title) {
        System.out.println("\n " + BOLD + title.toUpperCase() + RESET);
        System.out.println(" " + repeat("=", title.length()));
    }

    /**
     * Affiche un séparateur horizontal.
     */
    public static void printSeparator() {
        System.out.println("   " + repeat("-", 50));
    }

    /**
     * Consultation individuelle du tarif selon le Quotient Familial saisi.
     */
    public static void consulterTarif(TarificationService service, List<Tarif> grille, Scanner scanner) {
        printHeader("Consultation de la grille multi-services");
        System.out.print("\n   Entrez le Quotient Familial : ");
        String qfStr = scanner.nextLine();

        try {
            double qf = Double.parseDouble(qfStr);
            Tarif t = service.trouverTarif(qf, grille);

            System.out.println("\n   Tranche : " + t.getTranche());
            System.out.printf("   Repas scolaire    : %.2f euros%n", t.getPrix(DonneesTarifs.REPAS));
            System.out.printf("   Loisirs journee   : %.2f euros%n", t.getPrix(DonneesTarifs.ACCUEIL_JOURNEE));
            System.out.printf("   Ados vacances     : %.2f euros%n", t.getPrix(DonneesTarifs.ADOS_VAC_JOURNEE_REPAS));
            System.out.printf("   Etudes mensuel    : %.2f euros%n", t.getPrix(DonneesTarifs.ETUDES_FORFAIT_MENSUEL));

        } catch (NumberFormatException e) {
            System.out.println("   Erreur : Entrez un nombre valide.");
        } catch (Exception e) {
            System.out.println("   Erreur : " + e.getMessage());
        }

        System.out.println("\n   Appuyez sur Entree.");
        scanner.nextLine();
    }

    /**
     * Dashboard générique utilisé pour tous les pôles.
     * Affiche les dépenses par nature, les recettes et le taux de couverture.
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
        
        double taux = (depensesTotales > 0) ? (recettesTotales / depensesTotales * 100) : 0;
        printLine("TAUX DE COUVERTURE", String.format("%.2f %%", taux));
        printSeparator();

        System.out.println("\n   [PARAMETRE] Recettes calculees sur " + (multiplicateur > 1 ? (int)multiplicateur + " unites annuelles" : "base forfaitaire"));
        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        scanner.nextLine();
    }

    public static void afficherDashboardScolaire(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Restauration", 140);
    }

    public static void afficherDashboardLoisirs(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Accueil de Loisirs", 1);
    }

    public static void afficherDashboardAdos(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Espace Ados", 1);
    }

    public static void afficherDashboardSejours(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Sejours", 1);
    }

    public static void afficherDashboardEtudes(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Etudes surveillees", 10);
    }

    public static void afficherDashboardPeriscolaire(Calculateur calc, Scanner sc) {
        afficherDashboardPole(calc, sc, "Accueil periscolaire", 10);
    }


    /**
     * Affiche une ligne de donnees alignee.
     */
    public static void printLine(String label, String value) {
        System.out.printf("   %-32s : %s%n", label, value);
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
