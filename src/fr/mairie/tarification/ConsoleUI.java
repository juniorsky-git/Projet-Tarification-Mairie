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
     * Dashboard scolaire : utilise l onglet Simulation de CALC DEP.xlsx.
     * Les effectifs et prix factures viennent directement de ce fichier.
     */
    public static void afficherDashboardScolaire(Calculateur calc, Scanner sc) {
        printHeader("TABLEAU DE BORD : SCOLAIRE (source : onglet Simulation)");

        // Lecture des effectifs et prix depuis l onglet Simulation
        Map<String, Double> effectifs = calc.chargerEffectifsDepuisSimulation();
        double recettes              = calc.calculerRecettesDepuisSimulation();
        double coutRef               = calc.getCoutMoyenReference();

        double totalEnfants = 0;
        for (double v : effectifs.values()) {
            totalEnfants += v;
        }

        double totalRepas        = totalEnfants * 140;
        double depensesRef       = totalRepas * coutRef;

        // Depenses reelles depuis les factures comptables (onglet 0)
        String[] exclusions = {"ADOS", "LOISIRS", "COMMUNAL"};
        double depensesReelles = calc.calculerDepensesPole("RESTMICH", "2-RE", null, exclusions);

        double coutReel = (totalRepas > 0) ? (depensesReelles / totalRepas) : 0;
        double tauxCouverture = (depensesRef > 0) ? (recettes / depensesRef * 100) : 0;
        double ecart = recettes - depensesRef;

        System.out.printf("\n   Effectifs totaux (Simulation)    : %.0f enfants%n", totalEnfants);
        System.out.printf("   Total repas annuels (x140j)      : %.0f repas%n", totalRepas);
        System.out.println("   " + repeat("-", 48));
        System.out.printf("   Cout moyen de reference (mairie) : %.2f euros%n", coutRef);
        System.out.printf("   Cout reel constate (comptabilite): %.2f euros%n", coutReel);
        System.out.println("   " + repeat("-", 48));
        System.out.printf("   Depenses (base ref. 4.42)        : %.2f euros%n", depensesRef);
        System.out.printf("   Recettes theoriques (Simulation) : %.2f euros%n", recettes);
        System.out.printf("   Taux de couverture               : %.2f %%%n", tauxCouverture);
        System.out.printf("   Ecart (Recettes - Depenses ref.) : %.2f euros%n", ecart);

        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        sc.nextLine();
    }

    /**
     * Dashboard generique pour les poles sans tableau de Simulation dedie.
     * Affiche les depenses reelles uniquement.
     */
    public static void afficherPoleSimple(Calculateur calc, String nom, String antenna,
            String service, String include, String[] exclude, Scanner sc) {
        printHeader("TABLEAU DE BORD : " + nom);

        double depenses = calc.calculerDepensesPole(antenna, service, include, exclude);

        System.out.printf("\n   Pole          : %s%n", nom);
        System.out.printf("   Depenses      : %.2f euros%n", depenses);
        System.out.println("\n   [NOTE] Recettes non calculees : pas de tableau de Simulation");
        System.out.println("   pour ce pole dans CALC DEP.xlsx.");

        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        sc.nextLine();
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

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }
}
