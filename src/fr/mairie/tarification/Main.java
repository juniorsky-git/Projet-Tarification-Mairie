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
                    ConsoleUI.afficherDashboardScolaire(calculateur, scanner);
                    break;
                case "2":
                    String[] exclLoisirs = {};
                    ConsoleUI.afficherPoleSimple(calculateur, "Loisirs", "RESTGAV", "2-RE", "LOISIRS", exclLoisirs, scanner);
                    break;
                case "3":
                    String[] exclAdos = {};
                    ConsoleUI.afficherPoleSimple(calculateur, "Ados", "RESTCA", "2-RE", "ADOS", exclAdos, scanner);
                    break;
                case "4":
                    ConsoleUI.consulterTarif(service, grilleRef, scanner);
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
