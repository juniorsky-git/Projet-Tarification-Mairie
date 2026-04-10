package fr.mairie.tarification;

import java.util.List;

/**
 * Utilitaire pour l'affichage épuré en console (version compatible Windows).
 */
public class ConsoleUI {

    private static final String RESET = "\u001B[0m";
    private static final String CYAN  = "\u001B[36m";
    private static final String BOLD  = "\u001B[1m";

    public static void printLogo() {
        System.out.println("\n" + CYAN + BOLD + "   TARIFICATION MUNICIPALE - VERSION 2025" + RESET);
        System.out.println("   --------------------------------------");
    }

    public static void printHeader(String title) {
        System.out.println("\n " + BOLD + title.toUpperCase() + RESET);
        System.out.println(" " + "=".repeat(title.length()));
    }

    public static void printMenu() {
        System.out.println("\n   [1] Tableau de bord financier (Michali)");
        System.out.println("   [2] Consulter un tarif (Saisie QF)");
        System.out.println("   [3] Afficher la grille de référence");
        System.out.println("   [4] Quitter");
        System.out.print("\n   Votre choix : ");
    }

    public static void printSeparator() {
        System.out.println("\n   " + "-".repeat(50));
    }

    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for(int i=0; i<5; i++) System.out.println();
        }
    }
}
