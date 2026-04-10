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
            System.out.println("\n   [1] Dashboard SCOLAIRE (Cantine)");
            System.out.println("   [2] Dashboard LOISIRS (Centre de loisirs)");
            System.out.println("   [3] Dashboard ADOS (Espace Ados)");
            System.out.println("   [4] Consulter un tarif individuel");
            System.out.println("   [5] Quitter");
            System.out.print("\n   Votre choix : ");

            String choix = scanner.nextLine();

            switch (choix) {
                case "1":
                    afficherDashboardScolaire(calculateur, scanner);
                    break;
                case "2":
                    String[] exclLoisirs = {};
                    afficherPoleSimple(calculateur, "Loisirs", "RESTGAV", "2-RE", "LOISIRS", exclLoisirs, scanner);
                    break;
                case "3":
                    String[] exclAdos = {};
                    afficherPoleSimple(calculateur, "Ados", "RESTCA", "2-RE", "ADOS", exclAdos, scanner);
                    break;
                case "4":
                    consulterTarif(service, grilleRef, scanner);
                    break;
                case "5":
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

    /**
     * Dashboard scolaire : utilise l onglet Simulation de CALC DEP.xlsx.
     * Les effectifs et prix factures viennent directement de ce fichier.
     */
    private static void afficherDashboardScolaire(Calculateur calc, Scanner sc) {
        ConsoleUI.printHeader("TABLEAU DE BORD : SCOLAIRE (source : onglet Simulation)");

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
        System.out.println("   " + "-".repeat(48));
        System.out.printf("   Cout moyen de reference (mairie) : %.2f euros%n", coutRef);
        System.out.printf("   Cout reel constate (comptabilite): %.2f euros%n", coutReel);
        System.out.println("   " + "-".repeat(48));
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
    private static void afficherPoleSimple(Calculateur calc, String nom, String antenna,
            String service, String include, String[] exclude, Scanner sc) {
        ConsoleUI.printHeader("TABLEAU DE BORD : " + nom);

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
    private static void consulterTarif(TarificationService service, List<Tarif> grille, Scanner scanner) {
        ConsoleUI.printHeader("Consultation de la grille multi-services");
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
}
