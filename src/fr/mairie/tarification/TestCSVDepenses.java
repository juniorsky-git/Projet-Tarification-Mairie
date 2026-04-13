package fr.mairie.tarification;

import java.util.Map;

public class TestCSVDepenses {
    public static void main(String[] args) {
        Calculateur calc = new Calculateur();
        
        System.out.println("=== Test lecture CSV CALC DEP (3).csv ===\n");
        
        Map<String, Double> totaux = calc.lireTotauxDepensesDepuisCSV();
        
        if (totaux.isEmpty()) {
            System.out.println("ERREUR : Aucune donnee lue du CSV !");
            System.exit(1);
        }
        
        System.out.println("Donnees lues du CSV :");
        System.out.println();
        
        for (Map.Entry<String, Double> entry : totaux.entrySet()) {
            System.out.printf("  %-30s : %15.2f euros%n", entry.getKey(), entry.getValue());
        }
        
        System.out.println("\n=== Test calcul Accueil de Loisirs ===\n");
        
        double totalLoisirs = calc.calculerTotalDepensesLoisirs();
        System.out.printf("Total Accueil de Loisirs : %.2f euros%n", totalLoisirs);
        
        double attendu = 1596116.14;
        if (Math.abs(totalLoisirs - attendu) < 0.01) {
            System.out.println("\n✓ CONFORME AU CSV (1 596 116,14 euros)");
        } else {
            System.out.printf("\n✗ NON CONFORME ! Attendu: %.2f, Obtenu: %.2f%n", attendu, totalLoisirs);
        }
        
        Map<String, Double> segments = calc.calculerDepensesReellesAccueilLoisirsParSegment();
        System.out.println("\nDetail par segment :");
        for (Map.Entry<String, Double> entry : segments.entrySet()) {
            System.out.printf("  %-20s : %15.2f euros%n", entry.getKey(), entry.getValue());
        }
    }
}
