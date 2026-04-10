package fr.mairie.tarification;

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
        System.out.println(" " + "=".repeat(title.length()));
    }

    /**
     * Affiche un séparateur horizontal.
     */
    public static void printSeparator() {
        System.out.println("   " + "-".repeat(50));
    }
}
