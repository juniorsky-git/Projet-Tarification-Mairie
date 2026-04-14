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
            System.out.println("   [4] Dashboard SEJOURS (Vacances)");
            System.out.println("   [5] Dashboard ETUDES (Surveillees)");
            System.out.println("   [6] Dashboard PERISCOLAIRE");
            System.out.println("   [7] Consulter un tarif individuel");
            System.out.println("   [9] Exporter le rapport PDF complet");
            System.out.println("   [0] Quitter");
            System.out.print("\n   Votre choix : ");

            String choix = scanner.nextLine();

            switch (choix) {
                case "1":
                    ConsoleUI.afficherDashboardScolaire(calculateur, scanner);
                    break;
                case "2":
                    ConsoleUI.afficherDashboardLoisirs(calculateur, scanner);
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
                    ConsoleUI.afficherDashboardPeriscolaire(calculateur, scanner);
                    break;
                case "7":
                    ConsoleUI.consulterTarif(service, grilleRef, scanner);
                    break;
                case "9":
                    System.out.println("\n   Generation du rapport PDF en cours...");
                    try {
                        PdfExportService pdfService = new PdfExportService();
                        String chemin = pdfService.genererRapport(calculateur, grilleRef);
                        System.out.println("   Rapport genere avec succes !");
                        System.out.println("   Fichier : " + chemin);
                    } catch (Exception e) {
                        System.out.println("   Erreur lors de la generation PDF : " + e.getMessage());
                    }
                    System.out.println("\n   Appuyez sur Entree pour revenir au menu.");
                    scanner.nextLine();
                    break;
                case "0":
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
}