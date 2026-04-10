package fr.mairie.tarification;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Point d'entrée de l'application de tarification municipale.
 * Gère la boucle principale du menu interactif et la navigation.
 */
public class Main {
    
    /**
     * Méthode principale qui initialise les services et lance le menu.
     * @param args Arguments ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        Calculateur calculateur = new Calculateur();
        TarificationService service = new TarificationService();
        Scanner scanner = new Scanner(System.in);
        List<Tarif> grilleRef = DonneesTarifs.chargerTarifsReference();

        boolean continuer = true;

        while (continuer) {
            ConsoleUI.clearConsole();
            ConsoleUI.printLogo();
            ConsoleUI.printMenu();

            String choix = scanner.nextLine();

            switch (choix) {
                case "1":
                    afficherDashboard(calculateur, scanner);
                    break;
                case "2":
                    consulterTarif(service, grilleRef, scanner);
                    break;
                case "3":
                    afficherGrilleReference(grilleRef, scanner);
                    break;
                case "4":
                    continuer = false;
                    System.out.println("\n   Merci de votre visite. Au revoir !");
                    break;
                default:
                    System.out.println("\n   Choix invalide. Appuyez sur Entrée pour continuer.");
                    scanner.nextLine();
            }
        }
        scanner.close();
    }

    /**
     * Option [1] : Traite et affiche le tableau de bord financier.
     * Calcule les indicateurs basés sur les fichiers Excel réels.
     */
    private static void afficherDashboard(Calculateur calculateur, Scanner scanner) {
        ConsoleUI.printHeader("Tableau de bord financier (MICHALI)");
        
        double depensesTotales = calculateur.calculerTotalDepenses("CLMICH");
        Map<String, Double> effectifs = calculateur.chargerEffectifsParTranche();
        double recettesTheoriques = calculateur.calculerRecettesTheoriques(effectifs);
        
        double totalEnfants = 0;
        for (double nb : effectifs.values()) {
            totalEnfants += nb;
        }
        double totalRepas = totalEnfants * 140;

        // Utilisation du coût de référence (4.42 euros)
        double coutMoyenRef = calculateur.getCoutMoyenReference();
        double depensesTheoriques = totalRepas * coutMoyenRef;
        double tauxCouverture = (depensesTheoriques > 0) ? (recettesTheoriques / depensesTheoriques * 100) : 0;

        // Analyse de l'écart budgétaire
        double coutMoyenReel = (totalRepas > 0) ? (depensesTotales / totalRepas) : 0;
        double ecartTotal = depensesTotales - depensesTheoriques;

        System.out.printf("\n   - Dépenses (Base 4.42 euros) : %10.2f euros%n", depensesTheoriques);
        System.out.printf("   - Recettes prévisionnelles   : %10.2f euros%n", recettesTheoriques);
        System.out.printf("   - Nombre total d'enfants     : %10.0f%n", totalEnfants);
        System.out.printf("   - Nombre total de repas      : %10.0f (140j)%n", totalRepas);
        System.out.println("   " + "-".repeat(45));
        System.out.printf("   > COÛT MOYEN (RÉFÉRENCE)     : %10.2f euros%n", coutMoyenRef);
        System.out.printf("   > TAUX DE COUVERTURE         : %10.2f %%%n", tauxCouverture);
        System.out.println("   " + "-".repeat(45));
        System.out.printf("   [ANALYSE] Coût réel constaté : %10.2f euros%n", coutMoyenReel);
        System.out.printf("   [ANALYSE] Écart budgétaire   : %10.2f euros%n", ecartTotal);
        
        System.out.println("\n   Appuyez sur Entrée pour revenir au menu.");
        scanner.nextLine();
    }

    /**
     * Option [2] : Calcule le tarif individuel selon le QF saisi.
     */
    private static void consulterTarif(TarificationService service, List<Tarif> grilleRef, Scanner scanner) {
        ConsoleUI.printHeader("Calculateur individuel");
        System.out.print("\n   Entrez le Quotient Familial : ");
        String qfStr = scanner.nextLine();
        
        try {
            double qf = Double.parseDouble(qfStr);
            System.out.print("   Entrez l'activité (ex: repas) : ");
            String activite = scanner.nextLine();

            Tarif t = service.trouverTarif(qf, grilleRef);
            double prix = service.obtenirPrix(t, activite);

            ConsoleUI.printSeparator();
            System.out.println("   RÉSULTAT POUR LE QF " + qf);
            System.out.println("   Tranche  : " + t.getTranche());
            System.out.println("   Activité : " + activite);
            System.out.printf("   Tarif    : %.2f euros%n", prix);
            ConsoleUI.printSeparator();
        } catch (NumberFormatException e) {
            System.out.println("   Erreur : Veuillez entrer un nombre valide.");
        } catch (Exception e) {
            System.out.println("   Erreur : " + e.getMessage());
        }

        System.out.println("\n   Appuyez sur Entrée pour continuer.");
        scanner.nextLine();
    }

    /**
     * Option [3] : Affiche la grille tarifaire complète de 2025.
     */
    private static void afficherGrilleReference(List<Tarif> grille, Scanner scanner) {
        ConsoleUI.printHeader("Grille tarifaire de référence 2025");
        
        System.out.printf("\n   %-5s | %-10s | %-10s | %-8s%n", "TR.", "QF MIN", "QF MAX", "REPAS");
        System.out.println("   " + "-".repeat(45));
        
        for (Tarif t : grille) {
            String qfMaxStr = (t.getQfMax() > 1000000) ? "SANS LIMITE" : String.format("%.0f", t.getQfMax());
            System.out.printf("   %-5s | %10.0f | %11s | %-8.2f euros%n", 
                t.getTranche(), t.getQfMin(), qfMaxStr, t.getRepas());
        }
        
        System.out.println("\n   Appuyez sur Entrée pour revenir au menu.");
        scanner.nextLine();
    }
}
