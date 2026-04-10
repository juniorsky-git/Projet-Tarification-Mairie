package fr.mairie.tarification;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Application Multi-Pôles : Gère les finances de tous les services municipaux.
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
            System.out.println("\n   [1] Dashboard SCOLAIRE (L. Michel)");
            System.out.println("   [2] Dashboard LOISIRS (C. de Loisirs)");
            System.out.println("   [3] Dashboard ADOS (Espace Ados)");
            System.out.println("   [4] Consulter un tarif individuel");
            System.out.println("   [5] Quitter");
            System.out.print("\n   Votre choix : ");

            String choix = scanner.nextLine();

            switch (choix) {
                case "1":
                    // Scolaire : Antenne RESTMICH, Service 2-RE, Exclure ADOS, LOISIRS, COMMUNAL
                    String[] exclScol = {"ADOS", "LOISIRS", "COMMUNAL"};
                    afficherPole(calculateur, "Scolaire", "RESTMICH", "2-RE", null, exclScol, 4, DonneesTarifs.REPAS, 140, scanner);
                    break;
                case "2":
                    // Loisirs : Antenne RESTGAV, Inclure LOISIRS
                    afficherPole(calculateur, "Loisirs", "RESTGAV", "2-RE", "LOISIRS", null, 39, DonneesTarifs.ACCUEIL_JOURNEE, 1, scanner);
                    break;
                case "3":
                    // Ados : Antenne RESTCA, Inclure ADOS
                    afficherPole(calculateur, "Ados", "RESTCA", "2-RE", "ADOS", null, 72, DonneesTarifs.ADOS_VAC_JOURNEE_REPAS, 1, scanner);
                    break;
                case "4":
                    consulterTarif(service, grilleRef, scanner);
                    break;
                case "5":
                    continuer = false;
                    System.out.println("\n   Merci de votre visite. Au revoir !");
                    break;
                default:
                    System.out.println("\n   Choix invalide.");
                    scanner.nextLine();
            }
        }
        scanner.close();
    }

    private static void afficherPole(Calculateur calc, String nom, String antenna, String service, String include, String[] exclude, int rowExcel, String tarifKey, double factor, Scanner sc) {
        ConsoleUI.printHeader("TABLEAU DE BORD : " + nom);
        
        double depenses = calc.calculerDepensesPole(antenna, service, include, exclude);
        Map<String, Double> effectifs = calc.chargerEffectifs(rowExcel - 1);
        double recettes = calc.calculerRecettes(effectifs, tarifKey, factor);
        
        double totalUsagers = 0;
        for (double v : effectifs.values()) {
            totalUsagers += v;
        }

        double taux = 0;
        if (depenses > 0) {
            taux = (recettes / depenses * 100);
        }

        System.out.printf("\n   Pôle          : %s%n", nom);
        System.out.printf("   Effectifs     : %.0f usagers%n", totalUsagers);
        System.out.printf("   Dépenses      : %.2f euros%n", depenses);
        System.out.printf("   Recettes (th) : %.2f euros%n", recettes);
        System.out.println("   " + "-".repeat(40));
        System.out.printf("   TAUX DE COUVERTURE : %.2f %%%n", taux);
        
        if (totalUsagers == 0) {
            System.out.println("\n   [NOTE] Données théoriques à compléter (Effectifs non trouvés dans le pôle).");
        }

        System.out.println("\n   Appuyez sur Entrée pour revenir au menu.");
        sc.nextLine();
    }

    private static void consulterTarif(TarificationService service, List<Tarif> grille, Scanner scanner) {
        ConsoleUI.printHeader("Consultation de la grille multi-services");
        System.out.print("\n   Entrez le Quotient Familial : ");
        String qfStr = scanner.nextLine();
        try {
            double qf = Double.parseDouble(qfStr);
            Tarif t = service.trouverTarif(qf, grille);
            System.out.println("\n   RÉSULTATS POUR LE QF " + qf + " (Tranche " + t.getTranche() + ") :");
            System.out.printf("   - Repas Scolaire  : %.2f euros%n", t.getPrix(DonneesTarifs.REPAS));
            System.out.printf("   - Loisirs Journée : %.2f euros%n", t.getPrix(DonneesTarifs.ACCUEIL_JOURNEE));
            System.out.printf("   - Ados Vacances   : %.2f euros%n", t.getPrix(DonneesTarifs.ADOS_VAC_JOURNEE_REPAS));
        } catch (Exception e) {
            System.out.println("   Erreur : " + e.getMessage());
        }
        System.out.println("\n   Appuyez sur Entrée.");
        scanner.nextLine();
    }
}
