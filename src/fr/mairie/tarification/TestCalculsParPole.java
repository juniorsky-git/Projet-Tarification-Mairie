package fr.mairie.tarification;

public class TestCalculsParPole {
    public static void main(String[] args) {
        Calculateur calc = new Calculateur();

        System.out.println("=== TEST CALCULS PAR POLE ===\n");

        // Test des dépenses par pôle
        System.out.println("DEPENSES PAR POLE:");
        System.out.printf("  Restauration: %.2f €\n", calc.calculerDepensesRestauration());
        System.out.printf("  Accueil Loisirs: %.2f €\n", calc.calculerDepensesAccueilLoisirs());
        System.out.printf("  Accueil Périscolaire: %.2f €\n", calc.calculerDepensesAccueilPeriscolaire());
        System.out.printf("  Études Surveillées: %.2f €\n", calc.calculerDepensesEtudesSurveillees());
        System.out.printf("  Espace Ados: %.2f €\n", calc.calculerDepensesEspaceAdos());
        System.out.printf("  Séjours: %.2f €\n", calc.calculerDepensesSejours());
        System.out.printf("  TOTAL GENERAL: %.2f €\n", calc.calculerTotalDepensesGenerales());

        System.out.println("\nRECETTES PAR POLE (théoriques):");
        System.out.printf("  Restauration: %.2f €\n", calc.calculerRecettesRestauration());
        // Les autres sont à 0.0 pour l'instant car non implémentées

        System.out.println("\nRAPPORT COMPLET:");
        System.out.println(calc.genererRapportCompletParPole());

        System.out.println("=== TEST TERMINE ===");
    }
}