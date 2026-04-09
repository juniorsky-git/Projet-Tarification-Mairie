import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DonneesTarifs {

    public static List<Tarif> chargerTarifs() {
        List<Tarif> tarifs = new ArrayList<>();
        String cheminFichier = "Donnees/Tableau-grille/Classeur1.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            int numLigne = 0;

            while ((ligne = br.readLine()) != null) {
                numLigne++;
                
                // Ignorer les 3 premières lignes d'en-tête
                if (numLigne < 4) continue;
                
                // Si on a atteint la ligne Total, on arrête la lecture de ce csv
                if (ligne.startsWith("Total") || ligne.startsWith(";;") || ligne.trim().isEmpty()) {
                    break;
                }

                String[] valeurs = ligne.split(";", -1);

                if (valeurs.length >= 7) {
                    // La tranche est dans la colonne B (index 1), sauf pour EXT qui est en A (index 0)
                    String tranche = valeurs[1].trim();
                    if (tranche.isEmpty()) {
                        tranche = valeurs[0].trim();
                    }

                    // On lit les nouvelles données PTM-2
                    double repas = parseValeurNumerique(valeurs[2]);
                    int usagers = (int) parseValeurNumerique(valeurs[3]);
                    double recettes = parseValeurNumerique(valeurs[6]);

                    // Comme le CSV n'a pas les QFs, on les déduit de la tranche avec une méthode
                    double qfMin = getQfMin(tranche);
                    double qfMax = getQfMax(tranche);

                    // On met 0 aux autres activités car Classeur1.csv ne les contient pas
                    tarifs.add(new Tarif(tranche, qfMin, qfMax, repas, 0, 0, 0, 0, usagers, recettes));
                }
            }
        } catch (IOException e) {
            System.err.println("Avertissement : Erreur de lecture du fichier " + cheminFichier + " (" + e.getMessage() + ")");
        } catch (Exception e) {
            System.err.println("Avertissement : Erreur de format dans le fichier CSV (" + e.getMessage() + ")");
        }

        return tarifs;
    }

    // Méthode utilitaire pour nettoyer les devises (retirer €, caractères bizarres, gérer la virgule)
    private static double parseValeurNumerique(String valeur) {
        if (valeur == null || valeur.trim().isEmpty()) return 0.0;
        // Remplace les virgules par des points et supprime tous les espaces, symboles euros ou autres
        String clean = valeur.replaceAll("[^0-9,\\.-]", "");
        clean = clean.replace(',', '.');
        if (clean.isEmpty()) return 0.0;
        return Double.parseDouble(clean);
    }

    // Méthodes pour restaurer logicement les QF (car absents en tant que nombre pur dans Classeur1.csv)
    private static double getQfMin(String tranche) {
        switch (tranche) {
            case "EXT": case "A": return 18000;
            case "B": return 15000;
            case "B2": return 13000;
            case "C": return 11000;
            case "D": return 9000;
            case "E": return 7000;
            case "F": return 5000;
            case "F2": return 3000;
            case "G": return 0;
            default: return 0;
        }
    }

    private static double getQfMax(String tranche) {
        switch (tranche) {
            case "EXT": case "A": return 999999;
            case "B": return 17999.99;
            case "B2": return 14999.99;
            case "C": return 12999.99;
            case "D": return 10999.99;
            case "E": return 8999.99;
            case "F": return 6999.99;
            case "F2": return 4999.99;
            case "G": return 2999.99;
            default: return 0;
        }
    }
}