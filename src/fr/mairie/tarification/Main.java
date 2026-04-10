package fr.mairie.tarification;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // -----------------------------------------------------------------
        // TEST 1 : Chargement depuis le fichier EXCEL (nouvelle méthode)
        // -----------------------------------------------------------------
        System.out.println("=== [EXCEL] Lecture depuis Classeur1.xlsx ===");
        String cheminExcel = "Donnees/Tableau-grille/Classeur1.xlsx";
        List<Tarif> tarifsExcel = DonneesTarifs.chargerTarifsDepuisExcel(cheminExcel);

        if (tarifsExcel.isEmpty()) {
            System.out.println("Aucune donnée chargée depuis le fichier Excel.");
        } else {
            System.out.printf("%n%-5s | %-10s | %-10s | %-12s%n", "Trnch", "Repas (€)", "Usagers", "Recettes (€)");
            System.out.println("-".repeat(50));
            for (Tarif t : tarifsExcel) {
                System.out.printf("%-5s | %9.2f | %7d   | %12.2f%n",
                        t.getTranche(), t.getRepas(), t.getUsagers(), t.getRecettes());
            }
        }
        System.out.println("=".repeat(50) + "\n");

        // -----------------------------------------------------------------
        // TEST 2 : Chargement depuis le fichier CSV (ancienne méthode)
        // -----------------------------------------------------------------
        List<Tarif> tarifs = DonneesTarifs.chargerTarifs();

        // Afficher les données extraites en console pour vérification
        System.out.println("=== Données extraites du fichier Classeur1.csv (" + tarifs.size() + " tranches chargées) ===");
        for (Tarif t : tarifs) {
            System.out.printf("Tranche %-3s | Prix Repas: %5.2f € | Usagers: %4d | Recettes: %10.2f €%n",
                t.getTranche(), t.getRepas(), t.getUsagers(), t.getRecettes());
        }
        System.out.println("==================================================================\n");

        TarificationService service = new TarificationService();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Calcul de Tarification ===");
            System.out.print("Entrez le Quotient Familial (QF) : ");
            String qfInput = scanner.nextLine();

            // Gestion de l'erreur "texte au lieu d'un nombre"
            double qf = Double.parseDouble(qfInput);

            System.out.println("Activités disponibles :");
            System.out.println("  [Repas]           repas");
            System.out.println("  [Accueil loisirs] accueil-journee | accueil-demi-repas");
            System.out.println("  [Périscolaire]    periscolaire-matin-soir | periscolaire-matin-ou-soir");
            System.out.println("  [Études surv.]    etudes-forfait-mensuel | etudes-demi-forfait");
            System.out.println("  [Espace Ados]     ados-journee-repas | ados-journee-sans | ados-demi-repas | ados-demi-sans | ados-sortie-demi | ados-sortie-journee");
            System.out.print("Entrez l'activité : ");
            String activite = scanner.nextLine();

            // Charger la grille de référence complète (toutes les activités)
            List<Tarif> tarifsRef = DonneesTarifs.chargerTarifsReference();

            // Trouver la tranche correspondant au QF
            Tarif tarif = service.trouverTarif(qf, tarifsRef);

            // Obtenir le prix
            double prix = service.obtenirPrix(tarif, activite);

            System.out.println("\n--- Résultat ---");
            System.out.println("QF : " + qf);
            System.out.println("Tranche : " + tarif.getTranche());
            System.out.println("Activité : " + activite);
            System.out.printf("Tarif applicable : %.2f €%n", prix);

        } catch (NumberFormatException e) {
            System.err.println("Erreur de saisie : Vous devez entrer un nombre valide pour le QF.");
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur métier : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Une erreur inattendue est survenue : " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
