import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        List<Tarif> tarifs = DonneesTarifs.chargerTarifs();

        // PTM-4 : Afficher les données extraites en console pour vérification
        // PTM-4 : Afficher les données extraites en console pour vérification + l'objectif 6 (Calculs)
        System.out.println("=== Données extraites du fichier Classeur1.xlsx avec Moteur Apache POI (" + tarifs.size() + " tranches) ===");
        for (Tarif t : tarifs) {
            System.out.println(String.format("Tranche %-3s | Prix: %5.2f € | Ecart calculé: %10.2f € | Couverture: %6.2f %%", 
                t.getTranche(), t.getRepas(), Calculator.calculerEcart(t), Calculator.calculerTauxCouverture(t)));
        }
        System.out.println("===================================================================================\n");
        

        // SIMULATION THEORIQUE : Et si le maire décidait d'augmenter le prix de 15 centimes sur chaque repas ?
        System.out.println("=== SIMULATION : Augmentation du prix de 0.15 € sur toute la grille ===");
        double gainTotalVille = 0;
        
        for (Tarif t : tarifs) {
            if (t.getUsagers() == 0) continue; // On ignore les tranches sans enfants ce mois-ci
            
            double nouveauPrix = t.getRepas() + 0.15;
            double nouvelleRecette = Calculator.simulerNouvelleRecette(t, nouveauPrix);
            double gainPourCetteTranche = nouvelleRecette - t.getRecettes();
            
            System.out.println(String.format("Tranche %-3s -> Ancien prix: %.2f € | Nouveau: %.2f € ---> Gain estimé: +%.2f €", 
                t.getTranche(), t.getRepas(), nouveauPrix, gainPourCetteTranche));
                
            gainTotalVille += gainPourCetteTranche;
        }
        
        System.out.println("-----------------------------------------------------------------------");
        System.out.println(String.format("BENEFICE TOTAL DE LA MESURE SUR L'ANNEE POUR LA MAIRIE : +%.2f €", gainTotalVille));
        System.out.println("=======================================================================\n");

        TarificationService service = new TarificationService();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Calcul de Tarification ===");
            System.out.print("Entrez le Quotient Familial (QF) : ");
            String qfInput = scanner.nextLine();
            
            // Gestion de l'erreur "texte au lieu d'un nombre"
            double qf = Double.parseDouble(qfInput);

            System.out.print("Entrez l'activité (repas, journee, demi-journee, matin-et-soir, matin-ou-soir) : ");
            String activite = scanner.nextLine();

            // Gestion "QF négatif" et "aucune tranche trouvée"
            Tarif tarif = service.trouverTarif(qf, tarifs);

            // Gestion "activité inconnue"
            double prix = service.obtenirPrix(tarif, activite);

            System.out.println("\n--- Résultat ---");
            System.out.println("QF : " + qf);
            System.out.println("Tranche : " + tarif.getTranche());
            System.out.println("Activité : " + activite);
            System.out.println("Tarif applicable : " + prix + " €");

        } catch (NumberFormatException e) {
            System.err.println("Erreur de saisie : Vous devez entrer un nombre valide pour le QF (texte reçu au lieu d'un nombre).");
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur métier : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Une erreur inattendue est survenue : " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}