package fr.mairie.tarification;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Point d entree de l application de tarification municipale.
 * Gere le menu interactif et la navigation entre les tableaux de bord.
 */
public class Main {

    public static void main(String[] args) {
        Calculateur calculateur = new Calculateur();
        TarificationService service = new TarificationService();
        Scanner scanner = new Scanner(System.in);
        List<Tarif> grilleRef = DonneesTarifs.chargerTarifsReference();

        boolean continuer = true;

        while (continuer) {
            ConsoleUI.clearConsole();
            ConsoleUI.printLogo();
            System.out.println("   [1] Dashboard SCOLAIRE (Cantine)");
            System.out.println("   [2] Dashboard LOISIRS (Centre de loisirs)");
            System.out.println("   [3] Dashboard ADOS (Espace Ados)");
            System.out.println("   [4] Dashboard SÉJOURS (Vacances)");
            System.out.println("   [5] Dashboard ÉTUDES (Surveillées)");
            System.out.println("   [6] Rapport COMPLET par PÔLE");
            System.out.println("   [7] Consulter un tarif individuel");
            System.out.println("   [8] Quitter");
            System.out.print("\n   Votre choix : ");

            String choix = scanner.nextLine();

            switch (choix) {
                case "1":
                    ConsoleUI.afficherDashboardScolaire(calculateur, scanner);
                    break;
                case "2":
                    afficherDashboardLoisirs(calculateur, scanner);
                    break;
                case "3":
                    ConsoleUI.afficherDashboardAdos(calculateur, scanner);
                    break;
                case "4":
                    ConsoleUI.afficherDashboardSejours(calculateur, scanner);
                    break;
                case "5":
                    ConsoleUI.afficherDashboardEtudes(calculateur, scanner);
                    break;
                case "6":
                    afficherRapportCompletParPole(calculateur, scanner);
                    break;
                case "7":
                    ConsoleUI.consulterTarif(service, grilleRef, scanner);
                    break;
                case "8":
                    continuer = false;
                    System.out.println("\n   Au revoir !");
                    break;
                default:
                    System.out.println("\n   Choix invalide.");
                    scanner.nextLine();
            }
        }
        scanner.close();
    }

    private static void afficherDashboardLoisirs(Calculateur calc, Scanner sc) {
        ConsoleUI.printHeader("TABLEAU DE BORD : LOISIRS (Accueil loisirs)");

        double depensesReelles = calc.calculerTotalDepensesLoisirs();
        Map<String, Double> detailsCategories = calc.getDepensesAccueilLoisirsDetaillees();

        ConsoleUI.printLine("Depenses reelles totales", String.format("%.2f euros", depensesReelles));
        ConsoleUI.printSeparator();
        
        System.out.println("   DETAIL PAR CATEGORIE :\n");
        for (Map.Entry<String, Double> entry : detailsCategories.entrySet()) {
            ConsoleUI.printLine("   " + entry.getKey(), String.format("%.2f euros", entry.getValue()));
        }
        
        System.out.println("\n   [SOURCE] CALC DEP (3).csv (synthetise)");
        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        sc.nextLine();
    }

    private static void afficherRapportCompletParPole(Calculateur calc, Scanner sc) {
        ConsoleUI.printHeader("RAPPORT COMPLET PAR PÔLE");

        String rapport = calc.genererRapportCompletParPole();
        System.out.println(rapport);

        System.out.println("\n   [SOURCE] CALC DEP (3).csv (synthetise)");
        System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
        sc.nextLine();
    }
}