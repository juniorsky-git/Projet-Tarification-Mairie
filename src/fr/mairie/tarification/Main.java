package fr.mairie.tarification;

import java.util.List;
import java.util.Scanner;

/**
 * Point d'entree principal de l'application de tarification municipale.
 * 
 * Cette classe initialise les services de calcul et de tarification, 
 * charge la grille de reference, et gere la boucle principale du menu 
 * interactif.
 * 
 * Elle permet a l'utilisateur de naviguer entre les 6 pôles de dashboards 
 * (Scolaire, Loisirs, Ados, Sejours, Etudes, Periscolaire), l'outil de 
 * consultation tarifaire et l'exportation PDF.
 * 
 * @author Séri-khane YOLOU (Crosne 2025)
 * @version 1.2
 */
public class Main {

    /**
     * Methode principale executee au lancement de l'application.
     * 
     * @param args Arguments de la ligne de commande (non utilises).
     */
    public static void main(String[] args) {
        // Initialisation des composants core
        Calculateur calculateur = new Calculateur();
        TarificationService service = new TarificationService();
        Scanner scanner = new Scanner(System.in);
        
        // Chargement des tarifs de reference depuis DonneesTarifs
        List<Tarif> grilleRef = DonneesTarifs.chargerTarifsReference();

        boolean continuer = true;

        // Boucle principale du menu
        while (continuer) {
            ConsoleUI.clearConsole();
            ConsoleUI.printLogo();
            
            // Affichage des options numerotees
            System.out.println("   [1] Dashboard SCOLAIRE (Cantine)");
            System.out.println("   [2] Dashboard LOISIRS (Centre de loisirs)");
            System.out.println("   [3] Dashboard ADOS (Espace Ados)");
            System.out.println("   [4] Dashboard SÉJOURS (Vacances)");
            System.out.println("   [5] Dashboard ÉTUDES (Surveillées)");
            System.out.println("   [6] Dashboard PÉRISCOLAIRE");
            System.out.println("   [7] Consulter un tarif individuel");
            System.out.println("   [8] Générer le rapport PDF complet");
            System.out.println("   [9] Quitter");
            System.out.print("\n   Votre choix : ");

            String choix = scanner.nextLine();

            // Gestion des choix utilisateur selon une structure switch/case
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
                    ConsoleUI.consulterTarif(service, scanner);
                    break;
                case "8":
                    ConsoleUI.gererExportPDF(calculateur, grilleRef, scanner);
                    break;
                case "9":
                    continuer = false;
                    System.out.println("\n   Au revoir !");
                    break;
                default:
                    System.out.println("\n   Choix invalide. Veuillez saisir un chiffre entre 1 et 9.");
                    System.out.println("   Appuyez sur Entree pour continuer.");
                    scanner.nextLine();
                    break;
            }
        }
        
        // Fermeture propre des ressources
        scanner.close();
    }
}