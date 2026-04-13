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
     */
    public static void afficherDashboardScolaire(Calculateur calc, Scanner sc) {
        printHeader("TABLEAU DE BORD : SCOLAIRE (source : onglet Simulation)");

        // Lecture unifiee des donnees depuis l onglet Simulation
        Calculateur.SimulationData sim = calc.chargerDonneesSimulation();
        Map<String, Double> effectifs = sim.getEffectifs();
        double recettes              = sim.getRecettesTheoriques();
        double coutRef               = sim.getCoutMoyenReference();

        double totalEnfants = 0;
        for (double v : effectifs.values()) {
            totalEnfants += v;
        }

        double totalRepas  = totalEnfants * 140;
        double depensesRef = totalRepas * coutRef;

        // Depenses reelles (onglet 0) via la methode universelle
        String[] exclusions = {"ADOS", "LOISIRS", "COMMUNAL"};
        double depensesReelles = calc.calculerDepenses("RESTMICH", "2-RE", null, exclusions);

        double coutReel = 0;
        if (totalRepas > 0) {
            coutReel = depensesReelles / totalRepas;
        }

        double tauxCouverture = 0;
        if (depensesRef > 0) {
            tauxCouverture = recettes / depensesRef * 100;
        }
        double ecart = recettes - depensesRef;

        printLine("Effectifs totaux (Simulation)", String.format("%.0f enfants", totalEnfants));
        printLine("Total repas annuels (x140j)", String.format("%.0f repas", totalRepas));
        printSeparator();
        printLine("Cout moyen de reference (mairie)", String.format("%.2f euros", coutRef));
        printLine("Cout reel constate (compta)", String.format("%.2f euros", coutReel));
        printSeparator();
        printLine("Depenses (base ref. 4.42)", String.format("%.2f euros", depensesRef));
        printLine("Recettes theoriques (Simulation)", String.format("%.2f euros", recettes));
        printLine("Taux de couverture", String.format("%.2f %%", tauxCouverture));
        printLine("Ecart (Recettes - Depenses ref.)", String.format("%.2f euros", ecart));

        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        sc.nextLine();
    }

    /**
     * Dashboard generique pour les poles sans Simulation dediee.
     */
    public static void afficherPoleSimple(Calculateur calc, String nom, String antenna,
            String service, String include, String[] exclude, Scanner sc) {
        printHeader("TABLEAU DE BORD : " + nom);

        double depenses = calc.calculerDepenses(antenna, service, include, exclude);

        printLine("Pole", nom);
        printLine("Depenses reelles", String.format("%.2f euros", depenses));
        
        System.out.println("\n   [NOTE] Recettes non calculees (pas d onglet Simulation).");
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

    /**
     * Affiche le dashboard simplifie pour l'Espace Ados (Depenses uniquement).
     */
    public static void afficherDashboardAdos(Calculateur calc, Scanner scanner) {
        System.out.println("\n" + repeat("=", 50));
        System.out.println("   DASHBOARD : ESPACE ADOS (Detaille)");
        System.out.println(repeat("=", 50));

        Map<String, Double> details = calc.getDepensesAdosDetaillees();
        double total = 0;

        System.out.println("\n   DETAIL DES CHARGES REELLES :");
        if (details.isEmpty()) {
            System.out.println("   Aucune donnee de depense trouvee.");
        } else {
            for (Map.Entry<String, Double> entry : details.entrySet()) {
                printLine(entry.getKey(), String.format("%.2f EUR", entry.getValue()));
                total += entry.getValue();
            }
        }

        System.out.println(repeat("-", 50));
        printLine("TOTAL GENERAL CALCULE", String.format("%.2f EUR", total));
        System.out.println(repeat("-", 50));

        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        scanner.nextLine();
    }

    /**
     * Affiche le dashboard pour les Sejours de vacances.
     */
    public static void afficherDashboardSejours(Calculateur calc, Scanner scanner) {
        System.out.println("\n" + repeat("=", 50));
        System.out.println("   DASHBOARD : SEJOURS DE VACANCES");
        System.out.println(repeat("=", 50));

        Map<String, Double> sejours = calc.getDepensesParSejour();
        double totalGénéral = 0;

        System.out.println("\n   DETAIL DES DEPENSES PAR DESTINATION :");
        if (sejours.isEmpty()) {
            System.out.println("   Aucune donnee de sejour trouvee.");
        } else {
            for (Map.Entry<String, Double> entry : sejours.entrySet()) {
                printLine(entry.getKey(), String.format("%.2f EUR", entry.getValue()));
                totalGénéral += entry.getValue();
            }
        }

        System.out.println(repeat("-", 50));
        printLine("TOTAL GENERAL DES SEJOURS", String.format("%.2f EUR", totalGénéral));
        System.out.println(repeat("-", 50));

        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        scanner.nextLine();
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
